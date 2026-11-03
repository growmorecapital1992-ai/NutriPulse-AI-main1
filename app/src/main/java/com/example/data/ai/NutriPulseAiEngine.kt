package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

private const val TAG = "NutriPulseAiEngine"

data class AiMealAnalysisResult(
    val isFood: Boolean = true,
    val nonFoodMessage: String = "",
    val name: String,
    val portion: String,
    val weightGrams: Int,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val fiber: Int,
    val vitamins: String,
    val sodium: Int,
    val giCategory: String, // LOW, MEDIUM, HIGH
    val healthyAlternatives: String,
    val clinicalFeedback: String,
    val sourceTier: String // "Tier 1: Firebase AI", "Tier 2: Direct Gemini REST", "Tier 3: Clinical Fallback"
)

class NutriPulseAiEngine {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // JSON Schema for Firebase AI Logic
    private val mealAnalysisSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "isFood" to Schema.boolean(description = "Whether the image/description actually contains food"),
                "nonFoodMessage" to Schema.string(description = "User warning message if not food or unrecognizable"),
                "foodName" to Schema.string(description = "Identified dish or meal name"),
                "portion" to Schema.string(description = "Estimated portion description"),
                "weightGrams" to Schema.integer(description = "Weight in grams"),
                "calories" to Schema.integer(description = "Caloric content in kcal"),
                "protein" to Schema.integer(description = "Protein in grams"),
                "carbs" to Schema.integer(description = "Carbohydrates in grams"),
                "fat" to Schema.integer(description = "Fat content in grams"),
                "fiber" to Schema.integer(description = "Dietary fiber in grams"),
                "vitamins" to Schema.string(description = "Key vitamins and minerals present"),
                "sodium" to Schema.integer(description = "Sodium content in mg"),
                "giCategory" to Schema.enumeration(
                    values = listOf("LOW", "MEDIUM", "HIGH"),
                    description = "Glycemic Index category"
                ),
                "healthyAlternatives" to Schema.string(description = "Healthy alternative suggestions tailored to user health profile"),
                "clinicalFeedback" to Schema.string(description = "Clinical advice tailored to condition and regional diet")
            )
        )
    }

    private val firebaseAiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = mealAnalysisSchema
            }
        )
    }

    suspend fun analyzeMeal(
        imageBitmap: Bitmap?,
        textInput: String?,
        cityRegion: String,
        dietaryCategory: String,
        healthCondition: String,
        weightGoal: String,
        customApiKey: String? = null
    ): AiMealAnalysisResult = withContext(Dispatchers.IO) {

        val regionalPrompt = buildRegionalPrompt(
            textInput = textInput,
            cityRegion = cityRegion,
            dietaryCategory = dietaryCategory,
            healthCondition = healthCondition,
            weightGoal = weightGoal
        )

        // Prioritize Custom User API Key if provided
        if (!customApiKey.isNullOrBlank()) {
            try {
                Log.d(TAG, "Attempting Live Gemini REST API with custom key...")
                val customResult = withTimeout(45_000L) {
                    executeDirectGeminiRest(
                        apiKey = customApiKey.trim(),
                        imageBitmap = imageBitmap,
                        prompt = regionalPrompt
                    )
                }
                if (customResult != null) return@withContext customResult
            } catch (e: Exception) {
                Log.w(TAG, "Custom Gemini API call failed: ${e.message}")
            }
        }

        // Tier 1: Try Firebase AI Logic
        try {
            Log.d(TAG, "Attempting Tier 1: Firebase AI Logic...")
            val result = withTimeout(45_000L) {
                if (imageBitmap != null) {
                    val response = firebaseAiModel.generateContent(
                        com.google.firebase.ai.type.content {
                            image(imageBitmap)
                            text(regionalPrompt)
                        }
                    )
                    parseStructuredJson(response.text.orEmpty(), "Live Firebase AI Logic")
                } else {
                    val response = firebaseAiModel.generateContent(regionalPrompt)
                    parseStructuredJson(response.text.orEmpty(), "Live Firebase AI Logic")
                }
            }
            if (result != null) return@withContext result
        } catch (e: Exception) {
            Log.w(TAG, "Tier 1 (Firebase AI) failed: ${e.message}. Falling back to Direct Gemini REST...")
        }

        // Tier 2: Direct Gemini REST Call with BuildConfig.GEMINI_API_KEY
        val directApiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (directApiKey.isNotBlank()) {
            try {
                Log.d(TAG, "Attempting Direct Gemini REST API with BuildConfig key...")
                val restResult = withTimeout(45_000L) {
                    executeDirectGeminiRest(
                        apiKey = directApiKey,
                        imageBitmap = imageBitmap,
                        prompt = regionalPrompt
                    )
                }
                if (restResult != null) return@withContext restResult
            } catch (e: Exception) {
                Log.w(TAG, "Direct Gemini REST API call exception: ${e.message}")
            }
        }

        // Tier 3: Dynamic Clinical Engine Fallback
        Log.d(TAG, "Using Tier 3: Dynamic Clinical Engine Fallback")
        return@withContext generateFallbackAnalysis(textInput, dietaryCategory, healthCondition)
    }

    private fun buildRegionalPrompt(
        textInput: String?,
        cityRegion: String,
        dietaryCategory: String,
        healthCondition: String,
        weightGoal: String
    ): String {
        return """
            You are NutriPulse AI, an advanced clinical and regional nutrition engine created by Ayush Kumar.
            
            STEP 1: STRICT VISUAL & TEXT FOOD VALIDATION
            Inspect the photo pixels and text hint very carefully:
            - DOES THE IMAGE CONTAIN A HUMAN FACE, PERSON, SELFIE, CLOTHING, ANIMAL, VEHICLE, BUILDING, FURNITURE, ELECTRONIC DEVICE, OR NON-FOOD OBJECT?
            - IS THE IMAGE BLURRY, DARK, OR UNRECOGNIZABLE AS FOOD?
            - IS THE TEXT HINT NON-FOOD OR GIBBERISH (e.g., person, face, selfie, car, phone, chair, 12345, asdfghjkl)?
            
            IF YES to any of the above, return JSON strictly with:
            {
              "isFood": false,
              "nonFoodMessage": "Please resend the photo or a clear photo, not able to recognize."
            }
            
            STEP 2: ACCURATE DISH NUTRITIONAL EVALUATION (If valid food photo/text)
            Calculate precise dish-specific macronutrients (NEVER use static defaults!):
            - Paneer dishes: HIGH Protein (~20-24g), HIGH Calcium, LOW Carbs (~6-8g), LOW GI.
            - Samosa / Fried snacks: LOW Protein (~3-4g), HIGH Fat (~16-20g), HIGH Carbs (~38-45g), HIGH GI.
            - Fruits (Apple/Orange/Banana): Accurate low fat (~0g), high fiber/vitamins, low/medium GI.
            - Regional Location: $cityRegion
            - Health Condition: $healthCondition
            - Dish/Quantity Hint: ${textInput.orEmpty().ifBlank { "Visual inspection" }}
            
            Return JSON with strictly these fields:
            {
              "isFood": true,
              "nonFoodMessage": "",
              "foodName": "Exact dish/fruit name",
              "portion": "Specific portion size (e.g. 1 Samosa / 200g Paneer Bhurji / 1 Apple)",
              "weightGrams": 200,
              "calories": 310,
              "protein": 22,
              "carbs": 6,
              "fat": 24,
              "fiber": 2,
              "vitamins": "Calcium, Vitamin B12, Phosphorus, Zinc",
              "sodium": 360,
              "giCategory": "LOW" | "MEDIUM" | "HIGH",
              "healthyAlternatives": "Custom healthy swap for $healthCondition",
              "clinicalFeedback": "Clinical advice for $healthCondition"
            }
        """.trimIndent()
    }

    private fun parseStructuredJson(rawJson: String, tierName: String): AiMealAnalysisResult? {
        return try {
            val json = JSONObject(rawJson)
            val isFood = json.optBoolean("isFood", true)
            val nonFoodMessage = json.optString("nonFoodMessage", "")

            AiMealAnalysisResult(
                isFood = isFood,
                nonFoodMessage = if (!isFood && nonFoodMessage.isBlank()) "Please resend the photo or a clear photo, not able to recognize." else nonFoodMessage,
                name = json.optString("foodName", "NutriPulse Analyzed Meal"),
                portion = json.optString("portion", "Standard Serving"),
                weightGrams = json.optInt("weightGrams", 250),
                calories = json.optInt("calories", 380),
                protein = json.optInt("protein", 15),
                carbs = json.optInt("carbs", 45),
                fat = json.optInt("fat", 12),
                fiber = json.optInt("fiber", 6),
                vitamins = json.optString("vitamins", "Vitamins B, C, Iron, Calcium"),
                sodium = json.optInt("sodium", 320),
                giCategory = json.optString("giCategory", "MEDIUM").uppercase(),
                healthyAlternatives = json.optString("healthyAlternatives", "Replace refined grains with whole millets or brown rice to regulate blood sugar."),
                clinicalFeedback = json.optString("clinicalFeedback", "Meal evaluated successfully by NutriPulse AI."),
                sourceTier = tierName
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error in $tierName: ${e.message}", e)
            null
        }
    }

    private fun executeDirectGeminiRest(
        apiKey: String,
        imageBitmap: Bitmap?,
        prompt: String
    ): AiMealAnalysisResult? {
        val modelsToTry = listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash")

        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", prompt))

        if (imageBitmap != null) {
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val inlineData = JSONObject()
                .put("mime_type", "image/jpeg")
                .put("data", base64Image)

            partsArray.put(JSONObject().put("inline_data", inlineData))
        }

        val contentsArray = JSONArray().put(JSONObject().put("parts", partsArray))
        val requestBodyJson = JSONObject()
            .put("contents", contentsArray)
            .put("generationConfig", JSONObject().put("response_mime_type", "application/json"))

        for (modelName in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: continue

                val jsonResponse = JSONObject(responseBody)
                if (jsonResponse.has("error")) {
                    val errMsg = jsonResponse.getJSONObject("error").optString("message", "API Error")
                    Log.w(TAG, "Direct Gemini REST ($modelName) returned error: $errMsg")
                    continue
                }

                if (jsonResponse.has("hasCandidates") || jsonResponse.has("candidates")) {
                    val candidates = jsonResponse.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        if (candidate.has("content")) {
                            val content = candidate.getJSONObject("content")
                            if (content.has("parts")) {
                                val contentParts = content.getJSONArray("parts")
                                if (contentParts.length() > 0) {
                                    var textOutput = contentParts.getJSONObject(0).getString("text")
                                    textOutput = textOutput.trim()
                                        .removePrefix("```json")
                                        .removePrefix("```")
                                        .removeSuffix("```")
                                        .trim()

                                    val result = parseStructuredJson(textOutput, "Tier 2: Direct Gemini REST ($modelName)")
                                    if (result != null) return result
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct Gemini REST ($modelName) execution exception: ${e.message}")
            }
        }
        return null
    }

    private fun generateFallbackAnalysis(
        textInput: String?,
        dietaryCategory: String,
        healthCondition: String
    ): AiMealAnalysisResult {
        val input = textInput.orEmpty().trim().lowercase()

        // Non-food & Face/Human detection keywords
        val nonFoodKeywords = listOf(
            "face", "person", "human", "selfie", "man", "woman", "boy", "girl", "head", "body", "eyes",
            "car", "phone", "building", "shoe", "chair", "table", "dog", "cat", "asdf", "qwerty", "1234", "test", "wall", "room"
        )
        val isExplicitNonFood = input.isBlank() || nonFoodKeywords.any { input.contains(it) }

        if (isExplicitNonFood) {
            return AiMealAnalysisResult(
                isFood = false,
                nonFoodMessage = "Please resend the photo or a clear photo, not able to recognize.",
                name = "Unrecognized Item",
                portion = "N/A",
                weightGrams = 0,
                calories = 0,
                protein = 0,
                carbs = 0,
                fat = 0,
                fiber = 0,
                vitamins = "N/A",
                sodium = 0,
                giCategory = "LOW",
                healthyAlternatives = "N/A",
                clinicalFeedback = "Item not recognized as food.",
                sourceTier = "Tier 3: Clinical Engine"
            )
        }

        // Dynamic dish & fruit lookup dictionary
        return when {
            input.contains("samosa") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Samosa (Fried Potato Snack)", portion = "1 Piece (100g)",
                weightGrams = 100, calories = 280, protein = 4, carbs = 38, fat = 16, fiber = 3,
                vitamins = "Vitamin C, Iron, B-Vitamins", sodium = 420, giCategory = "HIGH",
                healthyAlternatives = "Swap fried samosa with baked samosa or roasted makhana to reduce fat and high GI spike.",
                clinicalFeedback = "High fat and refined flour content. Consume sparingly for $healthCondition.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("paneer") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Paneer (Cottage Cheese) Dish", portion = "1 Bowl (200g)",
                weightGrams = 200, calories = 320, protein = 22, carbs = 6, fat = 24, fiber = 2,
                vitamins = "Calcium, Vitamin B12, Phosphorus, Zinc", sodium = 360, giCategory = "LOW",
                healthyAlternatives = "High in protein and calcium. Excellent for blood sugar management.",
                clinicalFeedback = "Low GI and high protein, great choice for $healthCondition.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("apple") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Fresh Red Apple", portion = "1 Medium Apple (182g)",
                weightGrams = 182, calories = 95, protein = 1, carbs = 25, fat = 0, fiber = 4,
                vitamins = "Vitamin C, Potassium, Antioxidants, Quercetin", sodium = 2, giCategory = "LOW",
                healthyAlternatives = "Apples are high in pectin fiber. Great for blood sugar control.",
                clinicalFeedback = "Excellent low-GI fruit option for $healthCondition.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("banana") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Fresh Yellow Banana", portion = "1 Medium Banana (118g)",
                weightGrams = 118, calories = 105, protein = 1, carbs = 27, fat = 0, fiber = 3,
                vitamins = "Vitamin B6, Vitamin C, Potassium, Magnesium", sodium = 1, giCategory = "MEDIUM",
                healthyAlternatives = "Pair banana with almonds or peanut butter to blunt postprandial glucose spike.",
                clinicalFeedback = "Rich in electrolytes. Good for pre-workout or quick energy.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("orange") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Juicy Orange", portion = "1 Whole Orange (140g)",
                weightGrams = 140, calories = 62, protein = 1, carbs = 15, fat = 0, fiber = 3,
                vitamins = "Vitamin C (100% DV), Folate, Potassium", sodium = 0, giCategory = "LOW",
                healthyAlternatives = "Eat whole fruit rather than fruit juice to retain dietary fiber.",
                clinicalFeedback = "High Vitamin C boosts immunity and vascular health for $healthCondition.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("mango") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Sweet Ripe Mango", portion = "1 Cup Slices (165g)",
                weightGrams = 165, calories = 99, protein = 1, carbs = 25, fat = 1, fiber = 3,
                vitamins = "Vitamin A, Vitamin C, Folate, Copper", sodium = 2, giCategory = "MEDIUM",
                healthyAlternatives = "Enjoy in controlled portion size (1/2 cup) to maintain target glycemic index.",
                clinicalFeedback = "Rich in beta-carotene and polyphenols.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("paneer") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Paneer Bhurji / Curry", portion = "1 Bowl (200g)",
                weightGrams = 200, calories = 310, protein = 18, carbs = 7, fat = 24, fiber = 2,
                vitamins = "Calcium, Vitamin B12, Phosphorus, Zinc", sodium = 380, giCategory = "LOW",
                healthyAlternatives = "Use low-fat cottage cheese/paneer or tofu to reduce saturated fat.",
                clinicalFeedback = "High protein and low GI, ideal for blood sugar management.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("chicken") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Chicken Curry / Breast", portion = "1 Serving (220g)",
                weightGrams = 220, calories = 340, protein = 32, carbs = 6, fat = 18, fiber = 1,
                vitamins = "Vitamin B12, Niacin, Selenium, Iron", sodium = 420, giCategory = "LOW",
                healthyAlternatives = "Grill or bake chicken with olive oil instead of heavy cream butter curry.",
                clinicalFeedback = "Lean protein source supporting muscle hypertrophy and satiety.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("roti") || input.contains("chapati") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Whole Wheat Roti / Chapati", portion = "2 Rotis (80g)",
                weightGrams = 80, calories = 180, protein = 6, carbs = 36, fat = 2, fiber = 5,
                vitamins = "B-Vitamins, Magnesium, Iron", sodium = 120, giCategory = "MEDIUM",
                healthyAlternatives = "Mix 25% chana flour (besan) or multigrain flour into wheat dough for extra protein.",
                clinicalFeedback = "Complex carbs provide sustained energy.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("dal") || input.contains("pulse") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Pahadi / Yellow Dal Tadka", portion = "1 Bowl (220g)",
                weightGrams = 220, calories = 210, protein = 12, carbs = 32, fat = 5, fiber = 7,
                vitamins = "Folate, Iron, Potassium, Magnesium", sodium = 310, giCategory = "LOW",
                healthyAlternatives = "Use minimal ghee in tadka and add spinach/methi for extra fiber.",
                clinicalFeedback = "High fiber and plant protein, excellent for heart health.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("rice") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Steamed Rice", portion = "1 Bowl (180g)",
                weightGrams = 180, calories = 240, protein = 4, carbs = 52, fat = 1, fiber = 1,
                vitamins = "Niacin, Thiamine, Iron", sodium = 5, giCategory = "HIGH",
                healthyAlternatives = "Swap white rice with brown rice, brown millets, or quinoa.",
                clinicalFeedback = "High glycemic index. Pair with vegetables and lentils.", sourceTier = "Tier 3: Clinical Engine"
            )
            input.contains("pizza") || input.contains("burger") -> AiMealAnalysisResult(
                isFood = true, nonFoodMessage = "", name = "Fast Food Slice / Burger", portion = "1 Serving (220g)",
                weightGrams = 220, calories = 480, protein = 16, carbs = 54, fat = 22, fiber = 3,
                vitamins = "Calcium, Sodium, Iron", sodium = 720, giCategory = "HIGH",
                healthyAlternatives = "Opt for whole wheat crust, extra vegetable toppings, and reduced cheese.",
                clinicalFeedback = "High calorie and sodium density. Consume in moderation for $healthCondition.", sourceTier = "Tier 3: Clinical Engine"
            )
            else -> {
                val capitalized = input.ifBlank { "Regional Healthy Meal" }.replaceFirstChar { it.uppercase() }
                AiMealAnalysisResult(
                    isFood = true,
                    nonFoodMessage = "",
                    name = capitalized,
                    portion = "1 Serving (220g)",
                    weightGrams = 220,
                    calories = 290,
                    protein = 13,
                    carbs = 38,
                    fat = 10,
                    fiber = 5,
                    vitamins = "Vitamin C, B Complex, Potassium, Calcium",
                    sodium = 280,
                    giCategory = "MEDIUM",
                    healthyAlternatives = "Pair this meal with fresh green salad or curd to boost dietary fiber.",
                    clinicalFeedback = "Evaluated via NutriPulse Clinical Baseline Engine for $healthCondition.",
                    sourceTier = "Tier 3: Clinical Engine"
                )
            }
        }
    }
}
