package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.UserProfileEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCreatorScreen(
    profile: UserProfileEntity?,
    onSaveProfile: (name: String, age: Int, city: String, goal: String, calories: Int, protein: Int) -> Unit,
    onClearData: () -> Unit
) {
    var editName by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var editAge by remember(profile) { mutableIntStateOf(profile?.age ?: 18) }
    var editCity by remember(profile) { mutableStateOf(profile?.cityRegion ?: "Solan, Himachal Pradesh") }
    var editGoal by remember(profile) { mutableStateOf(profile?.weightGoal ?: "Maintain Weight & Energy") }
    var editCalories by remember(profile) { mutableFloatStateOf(profile?.calorieTarget?.toFloat() ?: 2000f) }
    var editProtein by remember(profile) { mutableFloatStateOf(profile?.proteinTarget?.toFloat() ?: 90f) }

    var saveSuccessMessage by remember { mutableStateOf(false) }

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
            text = "👤 Profile & Creator Showcase",
            style = MaterialTheme.typography.headlineMedium,
            color = PulseEmerald
        )

        // Creator Attribution Showcase Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, PulseEmerald, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🏆 Master Software Architecture & Creator",
                    color = PulseEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Creator Photo
                Image(
                    painter = painterResource(id = R.drawable.creator_ayush_kumar),
                    contentDescription = "Ayush Kumar - Lead Creator & Developer",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, CyberCyan, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = "Ayush Kumar",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Text(
                    text = "Lead Architect, AI Engineer & Developer",
                    color = CyberCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianCardElevated, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• AI Engine: Google Gemini 1.5 Flash / 3.8 Flash Multimodal Vision", color = TextSecondary, fontSize = 11.sp)
                        Text("• Backend Architecture: 3-Tier Fail-Safe Resilient AI Engine", color = TextSecondary, fontSize = 11.sp)
                        Text("• Data Layer: Local Room SQLite Database Persistence", color = TextSecondary, fontSize = 11.sp)
                        Text("• Regional Intelligence: Indian Culinary Context & GI Parser", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // User Settings Form
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("⚙️ Edit Medical & Regional Parameters", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Full Name", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseEmerald, unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                )

                OutlinedTextField(
                    value = editCity,
                    onValueChange = { editCity = it },
                    label = { Text("City / Region", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan, unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_city_input")
                )

                Text("Daily Calorie Goal: ${editCalories.toInt()} kcal", color = TextPrimary, fontSize = 13.sp)
                Slider(
                    value = editCalories,
                    onValueChange = { editCalories = it },
                    valueRange = 1200f..4000f,
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                )

                Text("Daily Protein Goal: ${editProtein.toInt()} g", color = TextPrimary, fontSize = 13.sp)
                Slider(
                    value = editProtein,
                    onValueChange = { editProtein = it },
                    valueRange = 40f..200f,
                    colors = SliderDefaults.colors(thumbColor = PulseEmerald, activeTrackColor = PulseEmerald)
                )

                if (saveSuccessMessage) {
                    Text("Profile updated successfully!", color = PulseEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSaveProfile(editName, editAge, editCity, editGoal, editCalories.toInt(), editProtein.toInt())
                        saveSuccessMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_profile_updates_btn")
                ) {
                    Text("Save Profile Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Data Reset Section
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AlertCoral.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚠️ Data Reset & Clearing", color = AlertCoral, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Clear all recorded meal logs, hydration history, and local database entries.", color = TextSecondary, fontSize = 11.sp)

                OutlinedButton(
                    onClick = onClearData,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCoral),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AlertCoral)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("clear_all_data_btn")
                ) {
                    Text("Reset Entire App Data", color = AlertCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
