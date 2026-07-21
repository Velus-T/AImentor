# Validation Report — Reviewed Science Module

Validation date: 21 July 2026.

## Checks completed in the packaging environment

| Check | Result |
|---|---|
| Java syntax parsing | Passed: 118 Java source/test files, 0 parser errors |
| Android XML well-formedness | Passed: 67 manifest/resource XML files, 0 errors |
| Internal project import resolution | Passed: 0 unresolved `com.example.aimentor` imports |
| Android resource-reference consistency | Passed: 0 missing project layouts, IDs, colors, drawables, styles, menus or XML resources |
| Periodic-table dataset | Passed: 118 records and 118 unique atomic numbers (1–118) |
| Hard-coded Gemini key scan | Passed: no `AIza...` key-like value or assigned `GEMINI_API_KEY` secret found |
| Study-plan start rule | Passed by source check and unit test: no +10-minute offset; first session equals `createdAt` |
| Pure-Java production/test compilation | Passed for the domain/policy/prompt/planner subset used by the included unit tests |
| Pure-Java test execution | Passed: 7 tests, 0 failures; output saved in `PURE_JAVA_TEST_OUTPUT.txt` |
| Light/dark contrast calculation | Passed for the main audited pairs; all listed ratios exceed 4.5:1 |
| Original-project scope audit | Passed: 82 original source/config files remain byte-for-byte unchanged; only 4 minimum integration files differ, and machine-specific `local.properties` is omitted |

## Original-project scope audit

The supplied RAR metadata was used to compare CRC values without altering the archive. Excluding IDE/Gradle caches, 82 original files remain byte-for-byte unchanged. The only changed original files are:

1. `app/build.gradle` — RecyclerView dependency reference.
2. `gradle/libs.versions.toml` — RecyclerView version/library alias.
3. `app/src/main/AndroidManifest.xml` — Science activities, Internet/Camera permissions and Science FileProvider.
4. `app/src/main/res/values-night/themes.xml` — original base theme preserved, Science dark-theme override appended.

`local.properties` is not packaged because it contains a developer-machine SDK path. No Login, Registration, Home, Category, team Quiz, team Settings or Menu Java/XML logic was changed.

## Audited contrast pairs

| Pair | Calculated ratio |
|---|---:|
| Light primary text / Science background | 16.66:1 |
| Light secondary text / Science background | 9.66:1 |
| White button text / light primary button | 5.47:1 |
| Dark primary text / dark background | 18.01:1 |
| Dark secondary text / dark background | 14.19:1 |
| Dark user-message text / user bubble | 9.04:1 |

These calculations cover the main text surfaces. Android Accessibility Scanner should still be run on a real device because rendered states, font scaling and OEM themes can affect the final result.

## Included automated tests

Passed in the pure-Java validation harness:

1. Low quiz accuracy selects Support mode.
2. High quiz accuracy selects Challenge mode without changing education level.
3. Quiz repository failure falls back to On-level mode without changing education level.
4. The local fallback changes its question mix for Support versus Challenge while keeping the selected education level.
5. Physics, Chemistry and Biology prompts use different rules and include same-subject context.
6. Question validation accepts text or image and rejects an empty request.
7. Local study planning starts exactly at the captured creation timestamp and retains the selected level.

The original sample `ExampleUnitTest` remains in the project but was not executed by the lightweight external harness.

## Reliability review performed

The following crash-prone paths were reviewed and defensively handled in source:

- missing, invalid or quota-limited Gemini key;
- network timeout and empty/error response body;
- remote chat/quiz/planner failure with local fallback;
- camera permission denial and devices without a camera;
- FileProvider camera capture on API 24+;
- corrupt, oversized or memory-heavy images;
- Activity callbacks after destruction;
- rapid subject switching while a request is active;
- chat image binding reuse in RecyclerView;
- History, Bookmark, Progress and Periodic Table storage/asset failures;
- adaptive-quiz repository failures;
- quiz rotation and answer restoration;
- study-plan read/save/clear operations;
- animation lifecycle and Battery Saver.

## Full Android build status

A complete Gradle/Android build could **not** be performed in this environment.

Attempting `./gradlew --version` tried to download Gradle 8.13 and failed with `UnknownHostException: services.gradle.org`. The environment also has no `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `adb` or `sdkmanager`.

Therefore this report does not claim that an APK was produced or that all device-level tests passed here.

## Required final checks in Android Studio

1. Open the project and allow Android Studio to create `local.properties` for its SDK path.
2. Install/choose Android SDK 36.
3. Complete Gradle Sync.
4. Connect the team-owned Science button/card using `SCIENCE_INTEGRATION_SNIPPET.md`.
5. Run `testDebugUnitTest`.
6. Run `assembleDebug`.
7. Launch on API 24 and a current Android device/emulator.
8. Execute every manual case in `TEST_PLAN.md`.
9. Run Android Accessibility Scanner in light/dark mode and with larger font size.
10. Record actual results/screenshots for Assignment Part 2.
