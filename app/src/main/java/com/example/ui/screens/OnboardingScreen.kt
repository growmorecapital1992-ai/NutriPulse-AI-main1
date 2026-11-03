package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.*

private val INDIAN_CITIES = listOf(
    "Solan, Himachal Pradesh",
    "Shimla, Himachal Pradesh",
    "Delhi NCR, India",
    "Chandigarh, India",
    "Mumbai, Maharashtra",
    "Bengaluru, Karnataka",
    "Kolkata, West Bengal",
    "Chennai, Tamil Nadu",
    "Hyderabad, Telangana",
    "Pune, Maharashtra",
    "Jaipur, Rajasthan",
    "Ahmedabad, Gujarat",
    "Lucknow, Uttar Pradesh",
    "Other City / Region"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        name: String, age: Int, occupation: String, city: String,
        dietCat: String, proteinPrefs: List<String>, condition: String,
        goal: String, calories: Int, protein: Int
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(18) }
    var occupation by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf(INDIAN_CITIES[0]) }
    var cityExpanded by remember { mutableStateOf(false) }

    var dietaryCategory by remember { mutableStateOf("Vegetarian 🌱") }
    val selectedProteinSources = remember { mutableStateListOf("Paneer / Tofu", "Lentils & Dals") }

    var healthCondition by remember { mutableStateOf("General Wellness & Fitness") }
    var weightGoal by remember { mutableStateOf("Maintain Weight & Energy") }

    var calorieTarget by remember { mutableFloatStateOf(2000f) }
    var proteinTarget by remember { mutableFloatStateOf(90f) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BioDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Logo & Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nutripulse_logo),
                    contentDescription = "NutriPulse AI Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, PulseEmerald, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "NutriPulse AI",
                        style = MaterialTheme.typography.headlineLarge,
                        color = PulseEmerald
                    )
                    Text(
                        text = "Eat Smart. Live Better. • By Ayush Kumar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberCyan
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianCardElevated, RoundedCornerShape(12.dp))
                    .border(1.dp, PulseEmeraldDark, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Welcome! Establish your personalized medical and regional profile to calibrate the Gemini 3.8 Flash Multimodal AI Vision Engine.",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            // Section 1: Identification & Location
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "1. Personal Identification & Location", color = PulseEmerald, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PulseEmerald, unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Age: $age years", color = TextSecondary, fontSize = 12.sp)
                            Slider(
                                value = age.toFloat(),
                                onValueChange = { age = it.toInt() },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(thumbColor = PulseEmerald, activeTrackColor = PulseEmerald)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation / Routine", color = TextSecondary) },
                        placeholder = { Text("e.g., Engineering Student", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan, unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "City / Region (For Regional Culinary Calibration):", color = TextPrimary, fontSize = 13.sp)

                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { cityExpanded = !cityExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCity,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PulseEmerald, unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false },
                            modifier = Modifier.background(ObsidianCardElevated)
                        ) {
                            INDIAN_CITIES.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city, color = TextPrimary) },
                                    onClick = {
                                        selectedCity = city
                                        cityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Dietary Preferences
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "2. Dietary & Protein Preferences", color = PulseEmerald, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Vegetarian 🌱", "Eggitarian 🥚", "Non-Vegetarian 🍗").forEach { cat ->
                            val isSel = dietaryCategory == cat
                            Button(
                                onClick = { dietaryCategory = cat },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) PulseEmerald else ObsidianCardElevated,
                                    contentColor = if (isSel) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text(cat.take(12), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Preferred Protein Sources:", color = TextSecondary, fontSize = 12.sp)
                    val proteinList = listOf("Paneer / Tofu", "Lentils & Dals", "Chickpeas & Sprouts", "Eggs", "Chicken Breast", "Fish", "Soy Chunks")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        proteinList.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowItems.forEach { protein ->
                                    val checked = selectedProteinSources.contains(protein)
                                    FilterChip(
                                        selected = checked,
                                        onClick = {
                                            if (checked) selectedProteinSources.remove(protein)
                                            else selectedProteinSources.add(protein)
                                        },
                                        label = { Text(protein, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyberCyan,
                                            selectedLabelColor = Color.Black,
                                            containerColor = ObsidianCardElevated,
                                            labelColor = TextPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Clinical & Targets
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "3. Medical Focus & Daily Targets", color = PulseEmerald, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Text("Primary Medical Focus:", color = TextSecondary, fontSize = 12.sp)
                    val conditions = listOf("General Wellness & Fitness", "Diabetes / Sugar Control", "Hypertension (High BP)", "Weight Loss & Fat Burn")
                    conditions.forEach { cond ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { healthCondition = cond }
                        ) {
                            RadioButton(
                                selected = healthCondition == cond,
                                onClick = { healthCondition = cond },
                                colors = RadioButtonDefaults.colors(selectedColor = PulseEmerald)
                            )
                            Text(cond, color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Daily Calorie Target: ${calorieTarget.toInt()} kcal", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = calorieTarget,
                        onValueChange = { calorieTarget = it },
                        valueRange = 1200f..4000f,
                        steps = 56,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )

                    Text("Daily Protein Target: ${proteinTarget.toInt()} g", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = proteinTarget,
                        onValueChange = { proteinTarget = it },
                        valueRange = 40f..200f,
                        steps = 32,
                        colors = SliderDefaults.colors(thumbColor = PulseEmerald, activeTrackColor = PulseEmerald)
                    )
                }
            }

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = AlertCoral, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "Please enter your full name to proceed."
                    } else {
                        onComplete(
                            fullName, age, occupation.ifBlank { "Student / Professional" },
                            selectedCity, dietaryCategory, selectedProteinSources.toList(),
                            healthCondition, weightGoal, calorieTarget.toInt(), proteinTarget.toInt()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("initialize_nutripulse_btn")
            ) {
                Text("Initialize NutriPulse AI Engine ✨", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
