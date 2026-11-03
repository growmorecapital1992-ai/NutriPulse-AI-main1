package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MealRecordEntity
import com.example.data.db.UserProfileEntity
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    profile: UserProfileEntity?,
    allMeals: List<MealRecordEntity>,
    onOpenExportModal: () -> Unit
) {
    val totalCaloriesToday = allMeals.sumOf { it.calories }
    val totalProteinToday = allMeals.sumOf { it.protein }
    val totalCarbsToday = allMeals.sumOf { it.carbs }
    val totalFatToday = allMeals.sumOf { it.fat }

    val lowGiCount = allMeals.count { it.giCategory.uppercase() == "LOW" }
    val medGiCount = allMeals.count { it.giCategory.uppercase() == "MEDIUM" }
    val highGiCount = allMeals.count { it.giCategory.uppercase() == "HIGH" }
    val totalGiCount = (lowGiCount + medGiCount + highGiCount).coerceAtLeast(1)

    // Simulated 7-day trend values with real today data
    val weeklyData = remember(allMeals) {
        listOf<Int>(1850, 1920, 2100, 1780, 1950, 2050, totalCaloriesToday.coerceAtLeast(400))
    }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today")
    val calTarget = profile?.calorieTarget ?: 2000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BioDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📊 Weekly Trends & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PulseEmerald
                )
                Text(
                    text = "Long-term metabolic consistency & macro distributions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // 7-Day Calorie Canvas Bar Chart Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("7-Day Calorie Intake", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Target: ${calTarget} kcal", color = CyberCyan, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = 24.dp.toPx()
                    val spacing = (canvasWidth - (barWidth * weeklyData.size)) / (weeklyData.size + 1)
                    val maxVal = 3000f

                    // Target Line
                    val targetY = canvasHeight - ((calTarget / maxVal) * canvasHeight)
                    drawLine(
                        color = CyberCyan.copy(alpha = 0.6f),
                        start = Offset(0f, targetY),
                        end = Offset(canvasWidth, targetY),
                        strokeWidth = 2.dp.toPx()
                    )

                    weeklyData.forEachIndexed { index, cal ->
                        val barHeight = ((cal / maxVal) * canvasHeight).coerceIn(10f, canvasHeight)
                        val x = spacing + index * (barWidth + spacing)
                        val y = canvasHeight - barHeight

                        val barColor = if (index == 6) PulseEmerald else ObsidianCardElevated

                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEach { day ->
                        Text(day, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Glycemic Index Distribution Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Glycemic Load & Index Breakdown", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LOW GI", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("$lowGiCount meals", color = TextPrimary, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(AlertAmber.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MEDIUM GI", color = AlertAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("$medGiCount meals", color = TextPrimary, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(AlertCoral.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HIGH GI", color = AlertCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("$highGiCount meals", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Clinical Export Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PulseEmerald, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PulseEmerald, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Export Clinical Summary Report", color = PulseEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Branded summary report for your doctor or dietitian.", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onOpenExportModal,
                    colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("export_report_analytics_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate & View Clinical Report", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
