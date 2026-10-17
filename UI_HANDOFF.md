# UI handoff

## Scope
The complete Android app contains the shell and Math, Science, History and Programming.
Existing authentication, quizzes, notes, history and specialized subject tools are retained.
This update also corrects local persistence, circuit calculations, result labels and
notification permission reporting. No new server, role system or cloud sync is introduced.

## Visual system
- Brand: AI Mentor. Neutral navy text on pale blue surfaces; matching dark surfaces.
- Subject accents: blue (Math), teal (Science), amber (History), violet (Programming).
- Cards: 20dp radius. Inputs/buttons: 16dp radius, minimum 48dp touch target.
- Navigation labels: 12sp; content cards and heroes can wrap on small windows.
- Root system-bar/keyboard insets are applied once by SystemSafeArea.
- Short activity animations use Android animation scaling settings.

## Simplified flows
- Programming notes retain search, add and share in one action row.
- Math quiz retains experience/progress and all filters, without duplicated banner title
  and overlapping decoration.
- Home/settings spacing is reduced; no statistic or action was removed.
- Module toolbars reserve space for back navigation and support two-line titles.

## API integration
Root api.properties has separate MATH_, HISTORY_, PROGRAMMING_ and SCIENCE_ slots.
The committed file contains empty keys. Each module reads only its own group at build
time. Sync and rebuild after changing values. UI validation does not verify real AI responses.

## Suggested next work (not implemented)
1. Add a server authentication/session API and connect existing forms; define roles only
   when the product actually needs them.
2. Move AI calls and credentials to the server, using a common response/error contract.
3. Connect the unified local storage to user-scoped APIs for optional cloud synchronization.
4. Design shared loading/error/empty states and accessibility coverage for all detail screens.
5. Consider a global study plan and notification center after defining the actual data model;
   Science already has local study-planner functionality that can inform the design.

## Validation
Use JDK 21. Assemble the debug app, run all four module unit suites and app lint.
CanonicalSmokeTest covers auth navigation, shell/profile/settings, every module tab
and non-accumulating system/keyboard insets. Real AI calls are excluded.

Verified emulator configurations: normal light mode with gesture navigation, and
320dp width / 1.3 font scale / dark mode / three-button navigation. All navigation
controls remain within the system safe area. Other physical devices remain to be tested.

## Current interaction additions
- Shell: Home / Subjects / Profile. Profile includes all prior settings and profile editing.
- Math: 56dp draggable bubble, snaps to a horizontal edge, reserves room above the
  module navigation. Tap opens a scrollable calculator up to 360dp wide. Minimize
  retains the calculation. Hide removes the bubble; the Solve tab can reopen it.
- Calculator provides arithmetic, parentheses, square root, square and percent. Percent
  means division by 100 (e.g. 200 × 50% = 100); it is not a contextual commercial percent.
- Science circuit: source voltage, R1 and lamp resistance R2, switch and topology are
  editable. Values reflect an ideal constant-resistance DC model, not real lamp heating.
- Non-destructive card press feedback and short transitions share a common style.
- Vietnamese/English and day/night preferences apply across the shell and subjects.
- API configuration is empty in Git; local reference answers explicitly identify their source.

## Research-based interaction rules
- Primary learning action appears before progress statistics. Reopening a subject is labeled
  honestly; it does not claim to restore an unfinished exercise.
- Detailed statistics use one explicit show/hide control and preserve their expansion state.
- Field errors appear after submission at the relevant field, without a duplicate summary.
  Scroll/focus includes the input container so its message can be visible with the keyboard.
- The calculator result remains outside its keypad scroll area, including in landscape.
- Missing AI configuration is a persistent informational state, not a failed chat message.
- Note-save errors preserve the editor/draft and offer retry or explicit discard on exit.
- Research findings are documented in UX_RESEARCH.md; no analytics collection was added.

## Shared quiz/history follow-up — 2026-10-17

See [QUIZ_AND_HISTORY_REPORT.md](QUIZ_AND_HISTORY_REPORT.md) for the detailed Vietnamese handoff. All four modules now use common quiz setup, presentation and EXP policy, with 5/10/15 question choices and an account-scoped offline bank. History UI/search/date filtering is shared without changing individual saved questions into fabricated conversations. Bundled KaTeX renders formulas offline. Android 13+ locale changes use context-accessible LocaleManager, including before an Activity exists.

The expanded unit suite has 84 passing cases. Targeted compact-screen tests passed for locale/theme recreation, landscape calculator and actual soft-keyboard interaction. Final suite/publication status is recorded in WORK_IN_PROGRESS.md. Server authentication, secure AI proxy, synchronization, server-side grading and submission idempotency remain backend work. API keys remain empty and live Gemini quality has not been tested.


## Logic audit follow-up — 2026-10-17

See [LOGIC_AUDIT_2026-10-17.md](LOGIC_AUDIT_2026-10-17.md) for the next correctness pass. Shared generated-quiz validation prevents unusable AI content entering the offline bank. NoteEditorState preserves dialog drafts while subject modules retain persistence. Navigation reuses restored fragments; Programming confirms both tab and Back exits from unfinished quizzes. The expanded unit suite has 97 cases and the Android suite has 48 cases; completed results are recorded in WORK_IN_PROGRESS.md. No server backend or live Gemini test is implied.
