package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ai.AiMealAnalysisResult
import com.example.data.db.UserProfileEntity
import com.example.ui.AiScanState
import com.example.ui.theme.*

@Composable
fun AiScannerScreen(
    profile: UserProfileEntity?,
    scanState: AiScanState,
    onRunScan: (image: Bitmap?, portionHint: String, customApiKey: String?) -> Unit,
    onAddMealToTimeline: (AiMealAnalysisResult) -> Unit,
    onOpenManualLog: () -> Unit,
    onResetScanState: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var portionHint by remember { mutableStateOf("") }
    var userApiKey by remember { mutableStateOf("") }
    var showApiKeyConfig by remember { mutableStateOf(false) }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedBitmap = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) { null }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            selectedImageUri = null
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Log.e("AiScannerScreen", "Camera launch failed", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BioDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📷 Gemini AI Food Vision",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PulseEmerald
                )
                Text(
                    text = "Live Google Gemini Vision inspection for your meal photos & text.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            TextButton(onClick = { showApiKeyConfig = !showApiKeyConfig }) {
                Text("🔑 API Key", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Optional Live Gemini API Key Config Bar
        AnimatedVisibility(visible = showApiKeyConfig) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔑 Enter Google AI Studio Gemini API Key:", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = userApiKey,
                        onValueChange = { userApiKey = it },
                        placeholder = { Text("Paste AI Studio API Key (AIzaSy...)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan, unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Note: Key is processed directly on Google Gemini servers.", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        // Image Preview & Capture Box
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedBitmap != null) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Meal Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, PulseEmerald, RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(ObsidianCardElevated, RoundedCornerShape(10.dp))
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Snap or select a food photo", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    Log.e("AiScannerScreen", "Camera launch failed", e)
                                }
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianCardElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("take_photo_btn")
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = PulseEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera", color = TextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianCardElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("pick_gallery_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Portion Hint Input
        OutlinedTextField(
            value = portionHint,
            onValueChange = { portionHint = it },
            label = { Text("Dish Name & Portion Size (e.g. 1 Apple / 2 Rotis with Paneer)", color = TextSecondary) },
            placeholder = { Text("e.g. 2 rotis with pahadi dal, 1 bowl rice, 200g paneer", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PulseEmerald,
                unfocusedBorderColor = ObsidianBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth().testTag("portion_hint_input")
        )

        // Run Analysis Button
        Button(
            onClick = { onRunScan(selectedBitmap, portionHint, userApiKey.ifBlank { null }) },
            enabled = scanState !is AiScanState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("run_ai_analysis_btn")
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("🚀 Run Live Gemini AI Vision Analysis", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        // State Handlers: Loading, Error, Success
        when (scanState) {
            is AiScanState.Loading -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = PulseEmerald, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(scanState.message, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Calibrating for ${profile?.cityRegion ?: "regional diet"}...", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            is AiScanState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, AlertCoral, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AlertCoral)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Scan Exception Handled", color = AlertCoral, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(scanState.message, color = TextSecondary, fontSize = 12.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onRunScan(selectedBitmap, portionHint, userApiKey.ifBlank { null }) },
                                colors = ButtonDefaults.buttonColors(containerColor = AlertCoral),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retry Scan", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = onOpenManualLog,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Use Manual Log", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            is AiScanState.Success -> {
                val res = scanState.result
                val giColor = when (res.giCategory.uppercase()) {
                    "LOW" -> SuccessGreen
                    "MEDIUM" -> AlertAmber
                    else -> AlertCoral
                }

                if (!res.isFood) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, AlertAmber, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Image Recognition Alert", color = AlertAmber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Text(
                                text = res.nonFoodMessage.ifBlank { "Please resend the photo or a clear photo, not able to recognize." },
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onResetScanState,
                                    colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Try Again", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = onOpenManualLog,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Manual Entry", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, PulseEmerald, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("📋 Live Gemini AI Evaluation", color = PulseEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(res.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Badge(containerColor = ObsidianCardElevated) {
                                    Text(res.sourceTier, color = CyberCyan, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                                }
                            }

                            Text("Estimated Portion: ${res.portion} (${res.weightGrams}g)", color = TextSecondary, fontSize = 12.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Calories: ${res.calories} kcal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Protein: ${res.protein}g", color = PulseEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Carbs: ${res.carbs}g | Fat: ${res.fat}g | Fiber: ${res.fiber}g", color = TextSecondary, fontSize = 12.sp)
                                Text("GI: ${res.giCategory}", color = giColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Text("Vitamins & Minerals: ${res.vitamins}", color = PulseEmerald, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Sodium Load: ${res.sodium} mg", color = CyberCyan, fontSize = 12.sp)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ObsidianCardElevated, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("🥗 Healthy Alternative Suggestions:", color = AlertAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(res.healthyAlternatives, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ObsidianCardElevated, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text("Clinical AI Guidance: ${res.clinicalFeedback}", color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                            }

                            Button(
                                onClick = { onAddMealToTimeline(res) },
                                colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("add_scanned_meal_to_log_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("➕ Add Scanned Meal to Today's Timeline", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            else -> {}
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
