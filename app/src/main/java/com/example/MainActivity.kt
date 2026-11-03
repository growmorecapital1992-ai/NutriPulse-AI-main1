package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.config.FirebaseAppCheckConfig
import com.example.ui.NutriPulseViewModel
import com.example.ui.components.ClinicalExportModal
import com.example.ui.components.ManualMealDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class NutriTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    SCANNER("AI Vision", Icons.Default.CameraAlt, "tab_scanner"),
    HYDRATION("Hydration", Icons.Default.WaterDrop, "tab_hydration"),
    ANALYTICS("Analytics", Icons.Default.BarChart, "tab_analytics"),
    PROFILE("Creator", Icons.Default.Person, "tab_profile")
}

class MainActivity : ComponentActivity() {

    private val viewModel: NutriPulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // STEP 1: App Check initialization with launch intent via FirebaseAppCheckConfig
        FirebaseAppCheckConfig.configureAppCheck(this, intent)

        // STEP 2: UI Setup
        setContent {
            NutriPulseTheme {
                val userProfile by viewModel.userProfile.collectAsState()
                val todayMeals by viewModel.todayMeals.collectAsState()
                val allMeals by viewModel.allMeals.collectAsState()
                val aiScanState by viewModel.aiScanState.collectAsState()
                val isManualDialogVisible by viewModel.isManualDialogVisible.collectAsState()
                val isExportModalVisible by viewModel.isExportModalVisible.collectAsState()

                var currentTab by remember { mutableStateOf(NutriTab.DASHBOARD) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BioDarkBg
                ) {
                    if (userProfile == null) {
                        OnboardingScreen(
                            onComplete = { name, age, occ, city, diet, protein, cond, goal, cal, prot ->
                                viewModel.saveOnboardingProfile(name, age, occ, city, diet, protein, cond, goal, cal, prot)
                            }
                        )
                    } else {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = ObsidianCard,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("main_navigation_bar")
                                ) {
                                    NutriTab.entries.forEach { tab ->
                                        val isSelected = currentTab == tab
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentTab = tab },
                                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                                            label = { Text(tab.title) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = BioDarkBg,
                                                selectedTextColor = PulseEmerald,
                                                indicatorColor = PulseEmerald,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted
                                            ),
                                            modifier = Modifier.testTag(tab.tag)
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .background(BioDarkBg)
                            ) {
                                when (currentTab) {
                                    NutriTab.DASHBOARD -> DashboardScreen(
                                        profile = userProfile,
                                        meals = todayMeals,
                                        onNavigateToScan = { currentTab = NutriTab.SCANNER },
                                        onOpenManualLog = { viewModel.setManualDialogVisible(true) },
                                        onOpenExportModal = { viewModel.setExportModalVisible(true) },
                                        onDeleteMeal = { viewModel.deleteMeal(it) }
                                    )

                                    NutriTab.SCANNER -> AiScannerScreen(
                                        profile = userProfile,
                                        scanState = aiScanState,
                                        onRunScan = { bmp, hint, key -> viewModel.scanMeal(bmp, hint, key) },
                                        onAddMealToTimeline = { viewModel.addScannedMealToTimeline(it) },
                                        onOpenManualLog = { viewModel.setManualDialogVisible(true) },
                                        onResetScanState = { viewModel.resetAiScanState() }
                                    )

                                    NutriTab.HYDRATION -> HydrationScreen(
                                        profile = userProfile,
                                        onAddWater = { viewModel.addWaterIntake(it) },
                                        onResetWater = { viewModel.resetWaterIntake() },
                                        onTriggerNudge = { viewModel.triggerMealReminderNotification() }
                                    )

                                    NutriTab.ANALYTICS -> AnalyticsScreen(
                                        profile = userProfile,
                                        allMeals = allMeals,
                                        onOpenExportModal = { viewModel.setExportModalVisible(true) }
                                    )

                                    NutriTab.PROFILE -> ProfileCreatorScreen(
                                        profile = userProfile,
                                        onSaveProfile = { name, age, city, goal, cal, prot ->
                                            viewModel.saveOnboardingProfile(
                                                fullName = name,
                                                age = age,
                                                occupation = userProfile?.occupation ?: "Student",
                                                cityRegion = city,
                                                dietaryCategory = userProfile?.dietaryCategory ?: "Vegetarian",
                                                proteinPreferences = userProfile?.proteinPreferences?.split(", ") ?: emptyList(),
                                                healthCondition = userProfile?.healthConditions ?: "General Wellness",
                                                weightGoal = goal,
                                                calorieTarget = cal,
                                                proteinTarget = prot
                                            )
                                        },
                                        onClearData = { viewModel.clearAllUserData() }
                                    )
                                }

                                // Dialog Modals
                                if (isManualDialogVisible) {
                                    ManualMealDialog(
                                        onDismiss = { viewModel.setManualDialogVisible(false) },
                                        onSubmit = { name, portion, cal, prot, carb, fat, gi ->
                                            viewModel.addManualMeal(name, portion, cal, prot, carb, fat, gi)
                                        }
                                    )
                                }

                                if (isExportModalVisible) {
                                    ClinicalExportModal(
                                        profile = userProfile,
                                        meals = todayMeals,
                                        onDismiss = { viewModel.setExportModalVisible(false) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
