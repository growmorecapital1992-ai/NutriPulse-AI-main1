package com.example.data.repository

import com.example.data.db.HydrationEntity
import com.example.data.db.MealRecordEntity
import com.example.data.db.NutriPulseDao
import com.example.data.db.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NutriPulseRepository(private val dao: NutriPulseDao) {

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val allMeals: Flow<List<MealRecordEntity>> = dao.getAllMeals()

    fun getMealsForToday(): Flow<List<MealRecordEntity>> {
        val today = getTodayDateString()
        return dao.getMealsForDate(today)
    }

    fun getHydrationForToday(): Flow<List<HydrationEntity>> {
        val today = getTodayDateString()
        return dao.getHydrationForDate(today)
    }

    suspend fun saveProfile(profile: UserProfileEntity) {
        val today = getTodayDateString()
        val existing = dao.getUserProfileDirect()
        val streak = if (existing != null) {
            if (existing.lastActiveDate == today) existing.streakDays
            else existing.streakDays + 1
        } else 1
        dao.saveUserProfile(profile.copy(lastActiveDate = today, streakDays = streak))
    }

    suspend fun updateWaterIntake(amountMl: Int) {
        val today = getTodayDateString()
        val currentProfile = dao.getUserProfileDirect()
        if (currentProfile != null) {
            val newWater = (currentProfile.currentWaterMl + amountMl).coerceAtLeast(0)
            dao.saveUserProfile(currentProfile.copy(currentWaterMl = newWater))
            if (amountMl > 0) {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                dao.insertHydration(HydrationEntity(amountMl = amountMl, dateString = today, timestamp = time))
            }
        }
    }

    suspend fun resetWaterIntake() {
        val today = getTodayDateString()
        val currentProfile = dao.getUserProfileDirect()
        if (currentProfile != null) {
            dao.saveUserProfile(currentProfile.copy(currentWaterMl = 0))
            dao.resetHydrationForDate(today)
        }
    }

    suspend fun addMeal(meal: MealRecordEntity) {
        dao.insertMeal(meal)
    }

    suspend fun deleteMeal(mealId: Long) {
        dao.deleteMeal(mealId)
    }

    suspend fun clearAllData() {
        dao.clearAllMeals()
    }

    companion object {
        fun getTodayDateString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
