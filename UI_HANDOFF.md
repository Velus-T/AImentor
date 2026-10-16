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
