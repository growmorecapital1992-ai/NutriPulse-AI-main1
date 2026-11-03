package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MealRecordEntity
import com.example.data.db.UserProfileEntity
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    profile: UserProfileEntity?,
    meals: List<MealRecordEntity>,
    onNavigateToScan: () -> Unit,
    onOpenManualLog: () -> Unit,
    onOpenExportModal: () -> Unit,
    onDeleteMeal: (Long) -> Unit
) {
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.protein }
    val totalCarbs = meals.sumOf { it.carbs }
    val totalFat = meals.sumOf { it.fat }

    val calTarget = profile?.calorieTarget ?: 2000
    val protTarget = profile?.proteinTarget ?: 90

    val calProgress = (totalCalories.toFloat() / calTarget.toFloat()).coerceIn(0f, 1f)
    val protProgress = (totalProtein.toFloat() / protTarget.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BioDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, ${profile?.fullName ?: "User"} 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "📍 ${profile?.cityRegion ?: "India"} | 🔥 Streak: ${profile?.streakDays ?: 1} Days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberCyan
                )
            }

            IconButton(
                onClick = onOpenExportModal,
                modifier = Modifier
                    .background(ObsidianCardElevated, RoundedCornerShape(10.dp))
                    .border(1.dp, PulseEmeraldDark, RoundedCornerShape(10.dp))
                    .testTag("export_report_icon_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export Clinical Summary", tint = PulseEmerald)
            }
        }

        // AI Regional Health Nudge Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianCardElevated, RoundedCornerShape(12.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = PulseEmerald, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "🧠 NutriPulse Engine Active",
                        color = PulseEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Calibrated for ${profile?.healthConditions ?: "General Wellness"} in ${profile?.cityRegion ?: "your region"}. Regional GI and sodium trackers are ready.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Daily Progress Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Calories Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Calorie Budget", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalCalories / $calTarget",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text("kcal consumed", color = TextMuted, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { calProgress },
                        color = if (calProgress > 0.9f) AlertAmber else CyberCyan,
                        trackColor = ObsidianCardElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }
            }

            // Protein Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Protein Goal", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalProtein / ${protTarget}g",
                        color = PulseEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text("target active", color = TextMuted, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { protProgress },
                        color = PulseEmerald,
                        trackColor = ObsidianCardElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToScan,
                colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("scan_meal_dash_btn")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Food Scanner", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = onOpenManualLog,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCyan)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("manual_log_dash_btn")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Manual Log", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        Text(
            text = "🕒 Today's Logged Meals (${meals.size})",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        // Meal Timeline List
        if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ObsidianCard, RoundedCornerShape(14.dp))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No meals logged yet today", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Tap 'AI Food Scanner' to scan a meal photo or 'Manual Log' to type details.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(meals, key = { it.id }) { meal ->
                    MealTimelineItem(meal = meal, onDelete = { onDeleteMeal(meal.id) })
                }
            }
        }
    }
}

@Composable
fun MealTimelineItem(meal: MealRecordEntity, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val giColor = when (meal.giCategory.uppercase()) {
        "LOW" -> SuccessGreen
        "MEDIUM" -> AlertAmber
        else -> AlertCoral
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${meal.calories} kcal • ${meal.portion} @ ${meal.timestamp}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = giColor.copy(alpha = 0.2f),
                        contentColor = giColor
                    ) {
                        Text("GI: ${meal.giCategory}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(ObsidianCardElevated, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Macronutrients: Protein ${meal.protein}g | Carbs ${meal.carbs}g | Fat ${meal.fat}g | Fiber ${meal.fiber}g",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Sodium Index: ${meal.sodium} mg",
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "AI Clinical Feedback: ${meal.clinicalFeedback}",
                        color = PulseEmerald,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
