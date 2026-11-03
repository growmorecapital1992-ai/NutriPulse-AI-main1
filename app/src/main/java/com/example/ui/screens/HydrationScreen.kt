package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserProfileEntity
import com.example.ui.theme.*

@Composable
fun HydrationScreen(
    profile: UserProfileEntity?,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    onTriggerNudge: () -> Unit
) {
    val currentWater = profile?.currentWaterMl ?: 1000
    val targetWater = profile?.waterTargetMl ?: 3000
    val progress = (currentWater.toFloat() / targetWater.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

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

        Text(
            text = "💧 Hydration Management System",
            style = MaterialTheme.typography.headlineMedium,
            color = CyberCyan
        )
        Text(
            text = "Cellular hydration speeds up metabolism, balances electrolytes, and maintains cognitive focus.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Main Hydration Dial Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(ObsidianCardElevated)
                        .border(4.dp, CyberCyan, CircleShape)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(36.dp))
                        Text(
                            text = "$percentage%",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Text(
                            text = "$currentWater / $targetWater ml",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    color = CyberCyan,
                    trackColor = ObsidianCardElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
            }
        }

        Text("Quick Log Fluid Intake:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        // Quick Add Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onAddWater(250) },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianCardElevated),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("add_250ml_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+250 ml", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Glass", color = TextMuted, fontSize = 9.sp)
                }
            }

            Button(
                onClick = { onAddWater(500) },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianCardElevated),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("add_500ml_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+500 ml", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Bottle", color = TextMuted, fontSize = 9.sp)
                }
            }

            Button(
                onClick = { onAddWater(1000) },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianCardElevated),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("add_1000ml_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+1000 ml", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Liter", color = TextMuted, fontSize = 9.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onResetWater,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCoral),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AlertCoral)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("reset_water_btn")
            ) {
                Text("Reset Intake", color = AlertCoral, fontSize = 12.sp)
            }

            Button(
                onClick = onTriggerNudge,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("test_nudge_btn")
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Test Reminder", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Hydration Tip Box
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "💧 Clinical Hydration Nudge: Drinking 500ml water 30 minutes before meals improves digestive enzyme activation and reduces spikes in glycemic response.",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
