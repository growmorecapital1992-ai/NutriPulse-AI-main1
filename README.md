# NutriPulse AI — Clinical AI Nutrition, Regional Food Vision & Metabolic Tracker

**NutriPulse AI** (*"Eat Smart. Live Better."*) is a clinical-grade artificial intelligence nutrition, metabolic health, and food vision application conceptualized, architected, and engineered by **Ayush Kumar**.

The platform bridges Google Gemini 1.5 Flash / 3.8 Flash multimodal computer vision with personalized clinical profiles to analyze meal photos, estimate macronutrients, compute glycemic load, detect hidden sugars and sodium, track hydration, and offer adaptive regional nutritional guidance.

---

## 🌟 Key Application Features

### 1. 📷 Gemini Multimodal Food Vision Scanner
- **Live AI Processing**: Real-time analysis of meal photos using Google's Gemini Vision models (`gemini-3.8-flash`, `gemini-2.5-flash`, `gemini-1.5-flash`).
- **Strict Visual Food & Non-Food Validation**: Detects whether an image contains edible food or non-food objects (e.g. human face, person, selfie, clothing, furniture, or blurry photos) and issues a friendly warning:
  > ⚠️ *"Please resend the photo or a clear photo, not able to recognize."*
- **User Quantity & Dish Context**: Optional text input field allowing users to specify exact dish names and quantities (e.g., *"2 rotis with 1 bowl pahadi dal, 200g paneer"*).
- **Accurate Nutritional Breakdown**:
  - Calories (kcal), Protein (g), Carbohydrates (g), Fats (g), Dietary Fiber (g)
  - Micronutrients: Key Vitamins & Minerals (e.g., Vitamin C, B12, Calcium, Iron)
  - Glycemic Index Category (LOW, MEDIUM, HIGH) & Sodium Load (mg)
  - **Healthy Alternatives Box**: Tailored food swap suggestions based on user medical condition.

### 2. ⚡ 3-Tier Resilient AI Engine (Zero-Downtime / VPN Safe)
- **Tier 1 (Firebase AI Logic)**: Production `Firebase.ai` generative model execution with structured `Schema.obj(...)` JSON.
- **Tier 2 (Direct Gemini REST Fallback)**: Direct HTTPS call to Google's Gemini REST endpoints using `BuildConfig.GEMINI_API_KEY` (or custom user API key) to bypass App Check attestation issues, non-production networks, or VPNs.
- **Tier 3 (Clinical Engine Fallback)**: Dynamic local dish dictionary and clinical estimation fallback for complete offline resilience.

### 3. 💾 Offline Local Persistence (Android Room SQLite)
- Full Room database implementation (`NutriPulseDatabase`, `NutriPulseDao`, `NutriPulseRepository`).
- Stores user clinical profiles, daily meal logs with macro breakdowns, hydration entries, and active streaks locally on-device.

### 4. 📍 Regional Indian Culinary Intelligence
- Calibrated for Indian culinary practices (e.g., Solan, Shimla, Delhi, Mumbai, Bengaluru, Chandigarh) adjusting for hidden fats, ghee/mustard oil usage, traditional breads (Roti, Naan, Siddu, Dosa), and spice blends.

### 5. 💧 Advanced Hydration Management System
- Interactive fluid dial with daily target tracking (e.g., 3000 ml).
- Quick-add buttons (+250 ml glass, +500 ml bottle, +1000 ml liter, reset intake).
- Local background notification nudges (`NotificationHelper`).

### 6. 📊 Weekly Trends & Macro Analytics Visuals
- 7-Day calorie consistency bar chart rendered using custom **Jetpack Compose Canvas**.
- Glycemic Index distribution breakdown and average sodium index.

### 7. 📋 Branded Clinical Summary Export
- One-click report export modal compiling patient profile details, nutritional totals, and meal logs into a shareable clinical summary branded with **Engineered by Ayush Kumar**.

---

## 🏗️ Technical Architecture & Project Structure

```
com.example/
├── MainActivity.kt                      # Main Activity, App Check Init & Navigation
├── config/
│   └── FirebaseAppCheckConfig.kt        # App Check configuration & Debug token injection
├── data/
│   ├── ai/
│   │   └── NutriPulseAiEngine.kt        # 3-Tier Resilient AI Engine (Gemini 3.8 / REST / Fallback)
│   ├── db/
│   │   ├── Entities.kt                  # Room UserProfile, MealRecord, Hydration entities
│   │   ├── NutriPulseDao.kt             # Room Data Access Object (DAO)
│   │   └── NutriPulseDatabase.kt        # Room SQLite Database Singleton
│   └── repository/
│       └── NutriPulseRepository.kt      # Repository coordinating Room DB & Business Logic
├── notifications/
│   └── NotificationHelper.kt            # Local Android Notifications for hydration/meals
└── ui/
    ├── NutriPulseViewModel.kt           # Central Android MVVM ViewModel
    ├── components/
    │   ├── ClinicalExportModal.kt       # Shareable report modal
    │   └── ManualMealDialog.kt          # Manual meal entry fallback dialog
    ├── screens/
    │   ├── AiScannerScreen.kt           # Camera/Gallery scanner + Live Gemini Vision
    │   ├── AnalyticsScreen.kt           # 7-Day Canvas charts & Macro trends
    │   ├── DashboardScreen.kt           # Calorie progress ring, daily timeline & quick actions
    │   ├── HydrationScreen.kt           # Water dial & quick fluid log buttons
    │   ├── OnboardingScreen.kt          # 4-Step clinical profiler wizard
    │   └── ProfileCreatorScreen.kt      # User settings & Ayush Kumar Creator Showcase
    └── theme/
        ├── Color.kt                     # Neon Cyber-Dark Palette tokens
        ├── Theme.kt                     # Jetpack Compose Material 3 Theme setup
        └── Type.kt                      # Typography hierarchy
```

---

## 🚀 How to Run & Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/NutriPulse-AI.git
   ```
2. **Open in Android Studio**:
   - Open Android Studio (Hedgehog or newer recommended).
   - Select **Open an existing project** and navigate to the `NutriPulse-AI` directory.
3. **Configure Gemini API Key**:
   - Create or edit `.env` file in project root:
     ```env
     GEMINI_API_KEY=your_actual_google_ai_studio_api_key
     ```
   - Alternatively, use the in-app **🔑 API Key** configuration bar on the scanner screen.
4. **Build & Run**:
   - Select an Android Virtual Device (Emulator) or connect a physical Android device.
   - Click **Run (Shift + F10)**.

---

## 🏆 Lead Creator & Developer Attribution

- **Architect & Lead Developer**: **Ayush Kumar**
- **AI Platform**: Google Gemini 1.5 Flash / 3.8 Flash Multimodal Vision
- **Framework**: Android Kotlin, Jetpack Compose, Material Design 3, Room DB
