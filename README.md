# JARVIS AI - Personal Android Assistant

JARVIS AI is a voice-first, sci-fi-inspired personal assistant for Android smartphones. Built natively with Kotlin and Jetpack Compose, it features a dual-layer cognitive architecture combining instant offline deterministic intent execution with cloud model routing, real phone control integrations, persistent memory vault (Room DB), dynamic self-improving plugin skills, and a futuristic holographic heads-up display (HUD).

---

## Key System Architecture

```
com.example/
├── core/
│   ├── action/              # Subsystem action contract & security registry
│   │   ├── ActionContract.kt (Risk tiers: SAFE, CONFIRMATION_REQUIRED, HIGH_RISK)
│   │   ├── ActionRegistry.kt (Concurrency-safe registration & dispatch)
│   │   └── SystemActions.kt  (Real BatteryManager, StatFs, App Launcher, Settings)
│   ├── ai/                  # Cognitive engine & multi-lingual router
│   │   └── ModelRouter.kt   (Hindi, Hinglish, and English deterministic + cloud intent parsing)
│   ├── voice/               # Bidirectional voice pipeline
│   │   ├── VoiceEngine.kt   (Android SpeechRecognizer + TextToSpeech)
│   │   └── VoiceEngineState.kt (Idle, Listening, Processing, Speaking, Disrupted)
│   ├── skills/              # Self-improvement & dynamic plugin architecture
│   │   ├── SkillManifest.kt (JSON manifest schemas)
│   │   └── DynamicSkillEngine.kt (Proposing, sandbox testing, and installing skills)
│   └── accessibility/       # Safe on-screen automation
│       └── JarvisAccessibilityService.kt (User-authorized node interactions)
├── data/
│   ├── local/               # Room SQLite persistence
│   │   ├── JarvisDatabase.kt
│   │   ├── entity/Entities.kt (Memories, Task History, Installed Skills)
│   │   └── dao/Daos.kt
│   └── repository/JarvisRepository.kt
└── ui/
    ├── MainActivity.kt      # Edge-to-edge layout & NavigationBar
    ├── MainViewModel.kt     # Reactive StateFlow hub
    ├── theme/               # Holographic Cyan/Neon, Deep Navy theme
    ├── components/
    │   ├── HolographicReactor.kt (Canvas-drawn animated arc reactor)
    │   ├── AudioWaveformVisualizer.kt (Live audio equalizer)
    │   ├── HudTerminal.kt   (Live monospace telemetry feed)
    │   ├── ActionConfirmationDialog.kt (Intercepts risky actions)
    │   ├── AddSkillDialog.kt (Interactive skill synthesizer)
    │   └── AddMemoryDialog.kt
    └── screens/
        ├── HudScreen.kt     (Main console: Voice mic, text input, suggestion chips)
        ├── ActionsScreen.kt (Live inspection & test execution of phone actions)
        ├── MemoryScreen.kt  (Persistent user facts, preferences, routines)
        ├── SkillsScreen.kt  (Installed & dynamic skills engine)
        └── DiagnosticsScreen.kt (Battery %, Storage GB, CPU ABI, permissions checklist, kill-switch)
```

---

## Supported Voice & Text Commands

### 1. Device Diagnostics & Telemetry
- **English**: `"Check my battery status"`, `"How much storage is free?"`, `"Device info"`, `"Network status"`
- **Hindi / Hinglish**: `"Battery check karo"`, `"Phone ka battery kitna hai"`, `"Storage kitna hai"`, `"Internet check karo"`

### 2. Application & System Management
- `"WhatsApp kholo"` &rarr; Launches WhatsApp via package manager
- `"YouTube open karo"` &rarr; Launches YouTube
- `"Settings kholo"` &rarr; Opens Android system settings
- `"Wi-Fi settings open karo"` &rarr; Navigates directly to Wi-Fi settings
- `"Camera open karo"` &rarr; Launches camera viewfinder

### 3. Arithmetic & Utility
- `"Calculate 45 * 12"` or `"What is 1500 / 3"` &rarr; Solves arithmetic expressions
- `"Remind me at 8"` or `"Kal subah 7 baje yaad dilana"` &rarr; Dispatches alarm/reminder

### 4. Dynamic Skills & Plugins
- `"Daily briefing"` &rarr; Generates summary of device health and date
- `"Weather report"` &rarr; Atmospheric condition and forecast
- `"Add expense ₹250 for lunch"` &rarr; Logs expense into budget
- `"Jarvis, create a new skill for summarizing grocery expenses"` &rarr; Synthesizes, verifies in sandbox, and installs a custom skill

---

## Security & Privacy Model

1. **Explicit Risk Tiers**:
   - `SAFE`: Read-only queries (battery, storage, network info) execute immediately.
   - `CONFIRMATION_REQUIRED`: App launches, alarms, reminders prompt the user with a modal confirmation before execution.
   - `HIGH_RISK`: Sensitive changes require explicit user authorization.
2. **Private Local Vault**: Memories and task logs are stored locally on the device using SQLite/Room; no private data is transmitted without consent.
3. **Accessibility Safeguards**: Password fields, financial screens, and background screen scraping are explicitly blocked by `JarvisAccessibilityService`.
4. **Emergency Kill-Switch**: Located on the Diagnostics screen to immediately abort all automated tasks.

---

## Build & Installation Instructions

### Building Debug APK via Gradle
```bash
# Compile and run unit tests
gradle :app:testDebugUnitTest

# Build Debug APK
gradle :app:assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Installing on Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Optional FastAPI Backend (Cloud Reasoning Uplink)
```bash
cd backend
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Start uplink
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
