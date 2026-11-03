package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClinicalExportModal(
    profile: UserProfileEntity?,
    meals: List<MealRecordEntity>,
    onDismiss: () -> Unit
) {
    val currentDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.protein }
    val totalCarbs = meals.sumOf { it.carbs }
    val totalFat = meals.sumOf { it.fat }
    val avgSodium = if (meals.isNotEmpty()) meals.sumOf { it.sodium } / meals.size else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📋 Clinical Summary Report",
                        color = PulseEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "Generated for Doctor / Dietitian Review",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(BioDarkBg, RoundedCornerShape(10.dp))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Attribution
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseEmeraldDark.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "NutriPulse AI Clinical Diagnostics v5.1",
                            color = PulseEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Lead Architect & Developer: Ayush Kumar",
                            color = CyberCyan,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Report Date: $currentDate",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                HorizontalDivider(color = ObsidianBorder)

                // Patient Profile Section
                Text(
                    text = "PATIENT MEDICAL PROFILE",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "Name: ${profile?.fullName ?: "N/A"} | Age: ${profile?.age ?: 18}\n" +
                           "Location: ${profile?.cityRegion ?: "India"}\n" +
                           "Clinical Focus: ${profile?.healthConditions ?: "General Wellness"}\n" +
                           "Dietary Category: ${profile?.dietaryCategory ?: "Vegetarian"}\n" +
                           "Fitness Goal: ${profile?.weightGoal ?: "Maintain"}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                HorizontalDivider(color = ObsidianBorder)

                // Nutritional Totals
                Text(
                    text = "NUTRITIONAL TOTALS & SODIUM INDEX",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Energy Intake: ${totalCalories} / ${profile?.calorieTarget ?: 2000} kcal", color = TextPrimary, fontSize = 12.sp)
                    Text("Protein: ${totalProtein}g / ${profile?.proteinTarget ?: 90}g", color = PulseEmerald, fontSize = 12.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Carbohydrates: ${totalCarbs}g", color = CyberCyan, fontSize = 12.sp)
                    Text("Fats: ${totalFat}g", color = AlertAmber, fontSize = 12.sp)
                }
                Text("Avg Sodium Load: ${avgSodium} mg/meal", color = TextSecondary, fontSize = 12.sp)

                HorizontalDivider(color = ObsidianBorder)

                // Meal Timeline Details
                Text(
                    text = "MEAL LOG TIMELINE (${meals.size} Meals Logged Today)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                if (meals.isEmpty()) {
                    Text("No meals recorded for this date.", color = TextMuted, fontSize = 11.sp)
                } else {
                    meals.forEach { meal ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ObsidianCardElevated, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "• ${meal.name} (${meal.calories} kcal) @ ${meal.timestamp}",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "P: ${meal.protein}g | C: ${meal.carbs}g | F: ${meal.fat}g | GI: ${meal.giCategory}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "AI Note: ${meal.clinicalFeedback}",
                                color = CyberCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Note: Generated for clinical evaluation only. Not a medical prescription.",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("close_export_modal_btn")
            ) {
                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
