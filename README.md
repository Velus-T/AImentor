# AI Mentor

This is the complete project supplied in `AI_Mentor_English_Comments_No_Defense_Note.zip`,
replacing the earlier science-only repository snapshot. Existing Git history is retained.

## Modules

- `app`: login, registration, home, course categories, profile and settings.
- `ui-core`: host-independent reusable card layouts and transition resources.
- `math`: mathematics dashboard, chat, solving, formulas, quizzes and study history.
- `science`: physics, chemistry, biology, quizzes and science tools.
- `history`: history chat, quizzes, notes and saved conversations.
- `programming`: programming chat, quizzes and notes.

The app package is **`com.example.asm_final`**, distinct from the earlier
`com.example.aimentor` app. Run the `app` module to open the full application.

## Open in Android Studio

Open this repository root, let Gradle sync, and run `app`. The project uses the
included Gradle **9.5.0** wrapper, Android SDK **36**, and was validated with
**JDK 21**. Install missing SDK components through Android Studio as needed.
Local SDK paths belong in `local.properties`, which is ignored by Git.

## API configuration

All four modules read the single root `api.properties` file at build time.
Git contains empty keys. Fill only the section for the corresponding subject:
`MATH_`, `HISTORY_`, `PROGRAMMING_`, or `SCIENCE_`. Keys are never borrowed
from a different subject. Optional rotation slots can remain empty.
After editing, **Sync Gradle and rebuild/run** the app; installed APKs do not
read this file dynamically. Do not commit filled real keys. `local.properties`
is only for the Android SDK path. No paid AI calls were made during UI checks.

## UI updates

The shell and all four feature modules share navy text, stronger blue/teal/amber/violet
accents, 20dp cards, outlined fields and 52dp buttons, with matching dark surfaces.
Short window transitions follow the system animation scale. Card rows stack on
narrow windows or at larger font scales; math and science heroes use fluid text
widths instead of fixed overlapping columns.

Login and registration share rounded Material fields, password visibility
controls, readable spacing, keyboard submission and a bounded form width.
The original Gmail, phone and password validation and bcrypt password storage
remain in place. Course cards scroll on compact screens and use two columns
on wider windows.

`SystemSafeArea` applies one system-bar, display-cutout and keyboard inset
policy to the app shell and every module. Bottom keyboard space uses the
larger of keyboard/navigation insets, and padding does not accumulate.

## Debug accounts

Debug builds create these accounts if their usernames do not already exist.
Existing users and their passwords are not overwritten. Release builds do not
automatically create these accounts.

| Username | Password |
| --- | --- |
| hocvien01 | Mentor01!2026 |
| hocvien02 | Mentor02!2026 |
| hocvien03 | Mentor03!2026 |

This version has no account-role column or separate administrator permission
model; all three accounts use the same standard account functionality.

## Validation

The complete debug APK builds, all existing module unit tests pass, and app
Android lint passes with warnings. `CanonicalSmokeTest` passes on the connected
Android 37 emulator for login, registration navigation, opening all four
modules and the shared system-inset behavior. Real AI response quality,
additional devices and all screen-size combinations were not exhaustively
tested in this update.

## UI handoff and next steps

See [UI_HANDOFF.md](UI_HANDOFF.md) for the shared visual rules, simplified flows,
API integration boundary and proposed next work. No new backend business logic
was added. Existing module functionality remains available.

Final UI smoke checks also passed at 320dp width, font scale 1.3, dark mode
and three-button system navigation. There are 49 passing existing module unit tests.

## Correctness audit and architecture

See [AUDIT_SOLID.md](AUDIT_SOLID.md) for fixed findings, SOLID boundaries,
regression coverage and remaining integration work. Shared UI infrastructure now
lives in `ui-core`; account/profile and dashboard persistence are accessed through
focused interfaces. Both debug and release builds are checked.
