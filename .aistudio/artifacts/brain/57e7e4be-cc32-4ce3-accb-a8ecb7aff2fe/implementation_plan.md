# NutriPulse AI — Master Production Implementation Plan (v5.1 Resilient Edition)

**NutriPulse AI** (*"Eat Smart. Live Better."*) is a clinical-grade artificial intelligence nutrition, metabolic health, and food vision platform conceptualized, architected, and engineered by **Ayush Kumar**. 

The application bridges Google Gemini multimodal computer vision with personalized clinical profiles to analyze meal photos, estimate macronutrients, compute glycemic load, detect hidden sugars and sodium, track hydration, and offer adaptive regional nutritional guidance.

---

## Critical App Check & Network Resiliency Strategy

> [!IMPORTANT]
> **Addressing Firebase App Check Token Errors (VPN / Regional / Network Failures)**:
> To eliminate "Firebase App Check token is invalid" errors (which occur when switching networks, using VPNs like Germany VPNs, or running on emulators), NutriPulse AI implements a **3-Tier Fail-Safe AI Engine**:

1. **Tier 1: Firebase AI Logic with Debug App Check Provider**:
   - `configureAppCheck(this, intent)` initialized in `MainActivity.onCreate()` using `.commit()` synchronous token persistence.
   - Handles standard attestation for Firebase AI Logic.
2. **Tier 2: Direct Gemini API Fallback (`BuildConfig.GEMINI_API_KEY`)**:
   - `GEMINI_API_KEY` uncommented in `.env.example` so AI Studio injects user runtime secrets into `BuildConfig.GEMINI_API_KEY`.
   - If `Firebase.ai` encounters App Check token invalidation (HTTP 403 / attestation failure / VPN IP block), the AI Engine automatically seamlessly fails over to direct Gemini REST/SDK inference using `BuildConfig.GEMINI_API_KEY`.
3. **Tier 3: Instant Offline Manual Meal Logging Fallback**:
   - If all network calls fail or the user is completely offline, an interactive manual meal entry dialog opens instantly with instant macro/calorie estimation and saving to the local Room database.

---

## 1. Overview & Core Concept

NutriPulse AI transforms food logging from tedious manual counting into an intelligent, one-tap visual diagnosis. Designed for students, professionals, and individuals managing conditions like diabetes, hypertension, high cholesterol, or weight goals.

### Key Value Pillars
1. **Clinical Profile Calibration**: Captures individual diet style (Vegetarian, Eggitarian, Non-Vegetarian), protein preferences, clinical conditions, and daily caloric/protein targets.
2. **Multimodal Food Vision Scanner**: Inspects meal photos to identify food items, calculate portion sizes, estimate macronutrients (protein, carbs, fats, fiber), compute Glycemic Index (GI), and detect hidden sodium or oils.
3. **Regional Culinary Intelligence**: Calibrated for regional Indian diets (e.g., Solan, Shimla, Delhi, Mumbai, Bengaluru, Chandigarh) adjusting for hidden fats, traditional spices, and preparation styles.
4. **Adaptive Clinical Feedback**: Recommends personalized healthy swaps and dietary cautions specific to the user's recorded medical condition.
5. **Hydration Engine**: Interactive fluid tracker with animated goal rings, quick-add volumes (+250ml, +500ml, +1000ml), and local background reminders.
6. **Analytics & Clinical Export**: Visual progress trends and one-click PDF/report export for doctors and dietitians (*Engineered by Ayush Kumar*).

---

## 2. User Experience & Visual Design

### Navigation Hierarchy

```
┌──────────────────────────────────────────────────────────────────┐
│                          NutriPulse AI                           │
└──────────────────────────────────────────────────────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
         [First Launch]                  [Returning User]
      Deep Onboarding Wizard             NutriPulse Main Shell
    (4-Step Clinical Profiler)                    │
                 │                                │
                 └───────────────┬────────────────┘
                                 ▼
      ┌─────────────┬────────────┼────────────┬─────────────┐
      ▼             ▼            ▼            ▼             ▼
 [Dashboard]   [AI Scanner] [Hydration]  [Analytics]  [Profile/Creator]
• Calorie Ring • Photo/Camera• Fluid Gauge • 7-Day Graphs• Ayush Kumar Bio
• Macro Bars   • Fallback Log• Quick Add   • Macro Stats • Settings/Reset
• Meal Logs    • Regional Prompt           • PDF Export  • Local Storage
```

### Visual Identity Tokens
- **Primary / Pulse**: Glowing Emerald `#00E676` (pulse waveform, active indicators, health confirmations)
- **Secondary / Cyber**: Vibrant Cyan `#00B4D8` (neural circuit highlights, hydration level)
- **Surface & Cards**: Deep Obsidian Slate `#101C28` with subtle border stroke `#1C3247`
- **Background**: Bio Dark `#080E14` with subtle radial aura
- **Text & Accents**: Crisp White `#F0FDF4`, Muted Mint `#94A3B8`, Alert Amber `#F59E0B`, Coral Red `#EF4444`

---

## 3. Technical Architecture & Data Strategy

### 3-Tier AI System Architecture

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                              UI Layer (Jetpack Compose)                              │
│  ┌────────────────────┬────────────────────┬───────────────────┬──────────────────┐  │
│  │  OnboardingWizard  │   DashboardScreen  │  AiScannerScreen  │ HydrationScreen  │  │
│  ├────────────────────┼────────────────────┼───────────────────┼──────────────────┤  │
│  │  AnalyticsScreen   │ ClinicalExportView │ ProfileCreatorView│ FallbackDialog   │  │
│  └────────────────────┴────────────────────┴───────────────────┴──────────────────┘  │
└──────────────────────────────────────────┬───────────────────────────────────────────┘
                                           │ StateFlow / Actions
┌──────────────────────────────────────────▼───────────────────────────────────────────┐
│                           ViewModel Layer (MVVM)                                     │
│     NutriPulseViewModel (Profile, Meals, Hydration, AI, Analytics, Export, Nudges)   │
└──────────────────────┬───────────────────────────────────┬───────────────────────────┘
                       │                                   │
┌──────────────────────▼──────────┐       ┌────────────────▼───────────────────────────┐
│     3-Tier Resilient AI Engine  │       │        Local Room Database Layer          │
│ • Tier 1: Firebase.ai + AppCheck│       │ • UserProfileDao                          │
│ • Tier 2: Direct BuildConfig Key│       │ • MealRecordDao                           │
│ • Tier 3: Manual Logging Form   │       │ • HydrationDao                            │
│ • Regional Context Injection    │       │ • NutriPulseDatabase (Room Persistence)   │
└─────────────────────────────────┘       └───────────────────────────────────────────┘
```

---

## 4. Execution Steps

1. **Uncomment `GEMINI_API_KEY`** in `.env.example` to allow direct API key fallback.
2. **App Check Setup**: Implement `configureAppCheck` in `MainActivity.kt` using synchronous `.commit()` for debug token persistence.
3. **Room Database**: Create entities, DAOs, and database for offline local storage.
4. **3-Tier AI Service**: Build `NutriPulseAiRepository` combining Firebase AI Logic with automatic failover to direct `BuildConfig.GEMINI_API_KEY` REST API when App Check attestation fails or network issues occur.
5. **Compose UI Screens**:
   - Deep Onboarding Wizard
   - Dashboard & Timeline
   - AI Vision Scanner + Fallback Entry Form
   - Hydration Manager & Notifications
   - Analytics & 7-Day Canvas Graphs + PDF Clinical Summary Export
   - Profile & Creator Attribution (Ayush Kumar photo & bio)
6. **Verification**: Run `compile_applet` to verify compilation.
