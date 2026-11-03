package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiMealAnalysisResult
import com.example.data.ai.NutriPulseAiEngine
import com.example.data.db.MealRecordEntity
import com.example.data.db.NutriPulseDatabase
import com.example.data.db.UserProfileEntity
import com.example.data.repository.NutriPulseRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface AiScanState {
    data object Idle : AiScanState
    data class Loading(val message: String = "Analyzing with NutriPulse Regional AI...") : AiScanState
    data class Success(val result: AiMealAnalysisResult) : AiScanState
    data class Error(val message: String) : AiScanState
}

class NutriPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NutriPulseRepository
    private val aiEngine = NutriPulseAiEngine()
    private val notificationHelper = NotificationHelper(application)

    init {
        val dao = NutriPulseDatabase.getDatabase(application).dao()
        repository = NutriPulseRepository(dao)
    }

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayMeals: StateFlow<List<MealRecordEntity>> = repository.getMealsForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMeals: StateFlow<List<MealRecordEntity>> = repository.allMeals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHydration: StateFlow<List<com.example.data.db.HydrationEntity>> = repository.getHydrationForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiScanState = MutableStateFlow<AiScanState>(AiScanState.Idle)
    val aiScanState: StateFlow<AiScanState> = _aiScanState.asStateFlow()

    private val _isManualDialogVisible = MutableStateFlow(false)
    val isManualDialogVisible: StateFlow<Boolean> = _isManualDialogVisible.asStateFlow()

    private val _isExportModalVisible = MutableStateFlow(false)
    val isExportModalVisible: StateFlow<Boolean> = _isExportModalVisible.asStateFlow()

    fun saveOnboardingProfile(
        fullName: String,
        age: Int,
        occupation: String,
        cityRegion: String,
        dietaryCategory: String,
        proteinPreferences: List<String>,
        healthCondition: String,
        weightGoal: String,
        calorieTarget: Int,
        proteinTarget: Int
    ) {
        viewModelScope.launch {
            val profile = UserProfileEntity(
                id = 1,
                fullName = fullName,
                age = age,
                occupation = occupation,
                cityRegion = cityRegion,
                dietaryCategory = dietaryCategory,
                proteinPreferences = proteinPreferences.joinToString(", "),
                healthConditions = healthCondition,
                weightGoal = weightGoal,
                calorieTarget = calorieTarget,
                proteinTarget = proteinTarget,
                waterTargetMl = 3000,
                currentWaterMl = 1000
            )
            repository.saveProfile(profile)
        }
    }

    fun scanMeal(imageBitmap: Bitmap?, portionHint: String?, customApiKey: String? = null) {
        val currentProfile = userProfile.value ?: return
        viewModelScope.launch {
            _aiScanState.value = AiScanState.Loading()
            try {
                val result = aiEngine.analyzeMeal(
                    imageBitmap = imageBitmap,
                    textInput = portionHint,
                    cityRegion = currentProfile.cityRegion,
                    dietaryCategory = currentProfile.dietaryCategory,
                    healthCondition = currentProfile.healthConditions,
                    weightGoal = currentProfile.weightGoal,
                    customApiKey = customApiKey
                )
                _aiScanState.value = AiScanState.Success(result)
            } catch (e: Exception) {
                _aiScanState.value = AiScanState.Error("AI Scan encountered an error: ${e.localizedMessage}")
            }
        }
    }

    fun addScannedMealToTimeline(result: AiMealAnalysisResult) {
        viewModelScope.launch {
            val now = Date()
            val dateStr = NutriPulseRepository.getTodayDateString()
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

            val meal = MealRecordEntity(
                name = result.name,
                portion = result.portion,
                weightGrams = result.weightGrams,
                calories = result.calories,
                protein = result.protein,
                carbs = result.carbs,
                fat = result.fat,
                fiber = result.fiber,
                sodium = result.sodium,
                giCategory = result.giCategory,
                clinicalFeedback = result.clinicalFeedback,
                dateString = dateStr,
                timestamp = timeStr
            )
            repository.addMeal(meal)
            _aiScanState.value = AiScanState.Idle
        }
    }

    fun addManualMeal(
        name: String,
        portion: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        giCategory: String
    ) {
        viewModelScope.launch {
            val now = Date()
            val dateStr = NutriPulseRepository.getTodayDateString()
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

            val meal = MealRecordEntity(
                name = name.ifBlank { "Logged Meal" },
                portion = portion.ifBlank { "1 Portion" },
                weightGrams = 200,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                fiber = 5,
                sodium = 250,
                giCategory = giCategory,
                clinicalFeedback = "Manually logged meal. Fits your daily macro calculations.",
                dateString = dateStr,
                timestamp = timeStr
            )
            repository.addMeal(meal)
            _isManualDialogVisible.value = false
        }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
        }
    }

    fun addWaterIntake(amountMl: Int) {
        viewModelScope.launch {
            repository.updateWaterIntake(amountMl)
            val current = (userProfile.value?.currentWaterMl ?: 0) + amountMl
            val target = userProfile.value?.waterTargetMl ?: 3000
            notificationHelper.showHydrationNudge(current, target)
        }
    }

    fun resetWaterIntake() {
        viewModelScope.launch {
            repository.resetWaterIntake()
        }
    }

    fun triggerMealReminderNotification() {
        notificationHelper.showMealReminderNudge()
    }

    fun resetAiScanState() {
        _aiScanState.value = AiScanState.Idle
    }

    fun setManualDialogVisible(visible: Boolean) {
        _isManualDialogVisible.value = visible
    }

    fun setExportModalVisible(visible: Boolean) {
        _isExportModalVisible.value = visible
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.resetWaterIntake()
        }
    }
}
