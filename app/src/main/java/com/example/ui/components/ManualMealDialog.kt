package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ManualMealDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, portion: String, calories: Int, protein: Int, carbs: Int, fat: Int, gi: String) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("1 Bowl / Serving") }
    var calories by remember { mutableStateOf("350") }
    var protein by remember { mutableStateOf("15") }
    var carbs by remember { mutableStateOf("45") }
    var fat by remember { mutableStateOf("12") }
    var selectedGi by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✍️ Manual Meal Entry",
                    color = PulseEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Log meal details manually if camera scanning is unavailable or offline.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal / Dish Name", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseEmerald,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("manual_meal_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories (kcal)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("manual_calories_input")
                    )

                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PulseEmerald,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("manual_protein_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fats (g)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlertAmber,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Glycemic Index Category:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { gi ->
                        val isSelected = selectedGi == gi
                        val btnColor = when (gi) {
                            "LOW" -> SuccessGreen
                            "MEDIUM" -> AlertAmber
                            else -> AlertCoral
                        }
                        Button(
                            onClick = { selectedGi = gi },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) btnColor else ObsidianCardElevated,
                                contentColor = if (isSelected) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text(text = gi, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val calVal = calories.toIntOrNull() ?: 300
                    val protVal = protein.toIntOrNull() ?: 12
                    val carbVal = carbs.toIntOrNull() ?: 40
                    val fatVal = fat.toIntOrNull() ?: 10
                    onSubmit(mealName, portion, calVal, protVal, carbVal, fatVal, selectedGi)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_manual_meal_btn")
            ) {
                Text("Save Meal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
