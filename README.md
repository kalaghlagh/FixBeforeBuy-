# Fix Before Buy 🔧⚡🌱

**Fix Before Buy** is a production-grade Android application designed to champion the **Right to Repair** and reduce electronic waste. It empowers users to diagnose common issues across household appliances, computers, peripherals, and everyday devices, follow safe step-by-step repair guides, and mathematically compare estimated repair costs with replacement costs before purchasing new products.

---

## 📸 Screenshots

| Home & Diagnosis Input | Diagnostic Wizard & Checklist | Fix vs Replace Calculator | Repair History |
| :---: | :---: | :---: | :---: |
| *(Screenshot Placeholder: Home Screen)* | *(Screenshot Placeholder: Wizard Screen)* | *(Screenshot Placeholder: Calculator)* | *(Screenshot Placeholder: History)* |

---

## ✨ Features

- **📱 Smart Product Diagnostics**:
  - Support for 4 core categories: **Household Appliances**, **Computers & Laptops**, **Computer Peripherals & Accessories**, and **Everyday Products**.
  - Photo attachment via modern zero-permission Android Photo Picker and camera capture.
  - Symptom suggestion chips for rapid problem descriptions.
  - Optional brand, model, age, and warranty input.

- **🛡️ Deterministic Safety Enforcement**:
  - Independent safety rule engine that analyzes both user inputs and AI instructions.
  - Automatically intercepts and blocks dangerous actions:
    - Live AC mains electricity (120V / 230V)
    - Microwave high-voltage capacitors (2,000V–5,000V lethal charge retention)
    - Swollen, bloated, or punctured lithium batteries
    - Gas lines, valves, and gas burner assemblies
    - Water immersion with electrical circuits
    - Bypassing safety fuses or thermal interlocks
  - Halts DIY workflows and provides certified technician guidance when hazards are detected.

- **🤖 Dual-Mode AI Diagnostics**:
  - **Online AI Integration**: Direct integration with Google Gemini (`gemini-2.5-flash`, `gemini-1.5-flash`) and OpenAI-compatible endpoints.
  - **Vision / Multimodal**: Transmits compressed device photos directly to the model for visual symptom inspection.
  - **Offline Heuristic Mode**: Fully operational troubleshooting tree when offline or without an API key.

- **💰 Fix vs. Replace Cost Calculator**:
  - Immediate savings formula: `Total Replacement Cost - Total Repair Cost`.
  - Side-by-side financial breakdown (parts, labor, extra expenses vs. new product price and setup fees).
  - Annualized useful-life estimate comparison ($/year).
  - Immediate visual verdict badge ("Repair is Financially Favorable!" vs "Replacement is More Economical").
  - Direct save action linking calculations to the diagnostic session.

- **💾 Local Persistence (Room Database)**:
  - SQLite/Room database storing diagnostic sessions, chat history, repair checklists, and cost evaluations.
  - Search, filter by category, resume in-progress sessions, and delete or clear history.

- **🌐 Internationalization (i18n)**:
  - English (`en`)
  - Arabic (`ar`) with full Right-to-Left (RTL) layout support
  - French (`fr`)

---

## 🛠️ Tech Stack & Architecture

- **Platform**: Native Android (API 24+)
- **Language**: Kotlin 2.2
- **UI Toolkit**: Jetpack Compose (Material Design 3)
- **Local Persistence**: Android Jetpack Room with Moshi JSON TypeConverters
- **Network & API**: OkHttp 4, Retrofit 2, Moshi Kotlin
- **Image Loading**: Coil Compose
- **State Management**: MVVM Architecture with `StateFlow` and Coroutines
- **Asynchronous**: Kotlin Coroutines & Flow
- **Testing**: JUnit 4, Robolectric 4.16

---

## 🚀 Getting Started & Local Setup

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Android SDK**: Build Tools 36.0.0, Platform SDK 36, Min SDK 24
- **Git**

### Clone & Build

```bash
# Clone repository
git clone https://github.com/your-username/fix-before-buy.git
cd fix-before-buy

# Run unit tests
gradle testDebugUnitTest

# Assemble Debug APK
gradle assembleDebug

# Assemble Release APK
gradle assembleRelease
```

*For Windows PowerShell:*
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleRelease
```

---

## 🔑 AI Provider Configuration

1. Launch the app and navigate to the **Settings** tab.
2. Select your provider:
   - **Google Gemini API**: Enter your Gemini API key from [Google AI Studio](https://aistudio.google.com/).
     - Default model: `gemini-2.5-flash`
   - **OpenAI-Compatible / Custom Endpoint**: Enter your API key, custom base URL (e.g. `https://api.openai.com/v1/chat/completions` or local Ollama `http://10.0.2.2:11434/v1/chat/completions`), and model name.
3. Tap **Test Connection** to execute a live ping request. A green confirmation badge indicates operational status.
4. Tap **Save Settings**.

> **Security Note**: In this client application, API keys are stored in encrypted Android app preferences (`DataStore`). For high-volume public commercial releases, routing requests through a secure backend proxy server is recommended to protect your API credentials.

---

## 🧪 Running Automated Tests

```bash
# Run all unit tests & Robolectric JVM tests
gradle testDebugUnitTest
```

Covered test suites:
- `FixBeforeBuyUnitTest.kt`: Cost calculation formulas, edge cases (repair exceeds replacement), SafetyManager deterministic hazard detection, AIResponseParser schema validation and markdown extraction, and OfflineDiagnosticFallback across all 4 categories.
- `ExampleRobolectricTest.kt`: Android resource verification and context integration.

---

## 📦 GitHub Actions CI/CD Workflow

The repository includes an automated GitHub Actions workflow at `.github/workflows/build-apk.yml`.

### Workflow Triggers:
- Pushes to `main` or `master` branches
- Version tags matching `v*` (e.g., `v1.0.0`)
- Manual trigger via `workflow_dispatch`

### What the Workflow Does:
1. Sets up JDK 17 and Android SDK tooling.
2. Caches Gradle build dependencies.
3. Runs all unit and Robolectric tests.
4. Assembles the Release APK (`assembleRelease`).
5. Validates that the APK exists and is non-zero in size.
6. Uploads the build artifact as `fix-before-buy-release-apk` (retained for 30 days).
7. Automatically drafts a GitHub Release and attaches the APK whenever a version tag (`v*`) is pushed.

---

## 📄 License

This project is licensed under the Apache 2.0 License - see the `LICENSE` file for details.
