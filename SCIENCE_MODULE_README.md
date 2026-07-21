# AI Study Mentor — Reviewed Science Module

This project contains a **Science-only extension** for the existing Java Android application. The module covers Physics, Chemistry and Biology while keeping the rest of the team project unchanged.

## Scope boundary

The implemented source is isolated under `com.example.aimentor.science`, with Science-owned layouts, drawables, colors, assets and a separate `science_ai.db` database.

Outside the Science module, only the minimum project integration files were changed:

- `AndroidManifest.xml`: registers Science activities, Internet/Camera permissions and the Science `FileProvider`.
- `app/build.gradle` and `gradle/libs.versions.toml`: add RecyclerView, which is required by the chat UI.
- `values-night/themes.xml`: preserves the original app dark-theme definition and appends only the Science dark-theme override.

No existing Login, Registration, Home, Category, Quiz, Settings or Menu feature logic was rewritten. The machine-specific `local.properties` file is intentionally omitted; Android Studio recreates it for the local SDK path, and the Gemini key is entered inside Science Settings. A team member can connect an existing screen to `ScienceActivity` using the optional snippet in `docs/SCIENCE_INTEGRATION_SNIPPET.md`.

## Implemented Science features

### ChatGPT-style Science chat

- Message list with separate user and AI messages.
- Composer fixed at the bottom, text submission, gallery image and camera capture.
- Copy and bookmark actions on AI answers.
- Conversation context for follow-up questions.
- Three independent active conversations:
  - **Physics AI Tutor** — SI units, formulas, vectors, forces, energy, circuits and numerical steps.
  - **Chemistry AI Tutor** — formulas, subscripts/superscripts, balanced equations, states, moles and laboratory safety.
  - **Biology AI Tutor** — structure–function relationships, ordered biological processes, diagrams, ethics and educational health boundaries.
- Each subject has its own prompt strategy and its own in-memory conversation context, so Physics, Chemistry and Biology messages are not mixed.

### In-app API settings

- Gemini API key and model are entered from **Science → Settings**.
- No source-code or `local.properties` edit is required.
- The saved key is encrypted using Android Keystore.
- The user can test the connection, replace the key, remove it, or change the model.
- Chat, adaptive quiz and AI study planning share one defensive Gemini HTTP client.

> Security note: storing an encrypted key on the device is suitable for coursework/demo use, but a production mobile app should send AI requests through an authenticated backend and use a restricted key.

### Adaptive Science quiz

- Generates 3–5 questions for the selected subject, topic and education level.
- The selected level is never automatically changed.
- Recent results adjust the challenge **within the same level**:
  - below 50% → Support mode;
  - 50–79% → On-level mode;
  - 80% or above → Challenge mode.
- A new variation seed and recent-question exclusion list reduce repetition.
- AI generation is used when configured; otherwise a shuffled local bank for each subject and level is used.
- The local fallback also adapts its question mix: Support favours short confidence-building items, while Challenge favours matching and multi-step multiple-choice items without changing level.
- Supports multiple choice, true/false, fill-in-the-blank and matching.
- Quiz content, answers and submitted feedback survive configuration changes.

### AI Science study planner

- Builds a 3, 7, 14, 21 or 30-day plan.
- Uses subject, education level, goal and available minutes per day.
- The first session begins at the **exact timestamp captured when Generate is pressed**.
- Later sessions continue daily at the same clock time.
- AI creates the learning content when available; local code controls and validates dates, duration and level.
- A local fallback plan is created when the API key, network or model is unavailable.
- The latest plan and completion checkboxes are encrypted and saved per user.

### Other retained Science tools

- Searchable and filterable encrypted question history.
- Bookmark library.
- Chemistry formula formatting.
- Related experiments/applications/follow-up suggestions through the subject prompt.
- Interactive simple series/parallel circuit diagram.
- Searchable periodic-table dataset containing 118 elements.
- Science-only progress summary.
- Exact normalized-question cache to reduce repeated API calls.

## UI and accessibility decisions

- A lightweight animated background appears **only on the Science chat screen**.
- Animation stops when the Activity is not visible, when disabled in Settings, or when Android Battery Saver is active.
- Other Science screens use static high-contrast cards for readability and performance.
- Light and dark palettes use explicit foreground/background pairs.
- Buttons, checkboxes, radio options, spinners and editable controls use visible borders/states and approximately 48dp minimum touch targets.
- Selection is not communicated by color alone: checked controls also change border/background and native checked state.

The visual direction takes inspiration from learning apps such as Duolingo only at the UX-principle level: consistent hierarchy and spacing, while keeping clarity and each tool's purpose.

## Architecture

```text
com.example.aimentor.science
├── domain
│   ├── model
│   ├── policy
│   ├── repository
│   ├── service
│   └── usecase
├── data
│   ├── local
│   ├── remote
│   └── repository
├── di
├── presentation
│   ├── adapter
│   ├── chat
│   └── view
└── util
```

`ScienceModule` is the Science composition root. Activities depend on use cases and interfaces rather than creating SQLite or Gemini implementations directly.

## Open the module

The existing team UI is intentionally not modified. Connect the appropriate team-owned button/card to:

```java
startActivity(new Intent(requireContext(), ScienceActivity.class));
```

Use `this` instead of `requireContext()` when launching from an Activity. Full examples are in `docs/SCIENCE_INTEGRATION_SNIPPET.md`.

## Configure Gemini from the app

1. Open the Science module.
2. Tap **Settings · API needed**.
3. Paste a Gemini API key.
4. Keep or change the model name.
5. Tap **Test connection**.
6. Tap **Save**.

Without a valid key, the module remains usable through local fallback answers, local adaptive quizzes and local study plans. Image understanding requires the remote multimodal model.

## Verification

See:

- `docs/VALIDATION_REPORT.md`
- `docs/TEST_PLAN.md`
- `docs/SCIENCE_REQUIREMENTS_TRACEABILITY.md`
- `docs/SOLID_DESIGN.md`
- `docs/UX_AND_ACCESSIBILITY_REFERENCES.md`
- `docs/HUONG_DAN_CHAY_VA_CHUP_MINH_CHUNG.md`

A full APK was not produced in the packaging environment because it does not contain Android SDK 36 and cannot download the required Gradle distribution. Open the project in Android Studio, complete Gradle Sync, then run `testDebugUnitTest` and `assembleDebug`.
