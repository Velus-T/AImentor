# AI Mentor: comprehensive reform report

## Delivered behavior

| Area | Change and retained functionality |
| --- | --- |
| Shell | Home / Subjects / Profile navigation; settings embedded in Profile; edit profile, password, reminders, language, theme, share, about and logout retained |
| Login/register | Consistent brand, approved neutral headline, outlined inputs, password visibility and original validation retained |
| Common UI | Stronger subject colors, shared card radius/press feedback, responsive rows, consistent dark palettes and system/keyboard safe areas |
| Math | Solve/chat, history with LaTeX, formulas/notes, quiz filters and progress retained; new draggable calculator only in Math |
| Science | Physics/chemistry/biology, solver, quizzes, periodic table, circuit, planner, history, bookmarks, progress and display settings retained |
| History | Chat/solver, saved conversations, notes and quizzes retained, cards/list rows simplified |
| Programming | Chat/images, conversation history, notes/search/share, quizzes, languages and progress retained; simpler home and quiz layouts |
| Storage | Five account/subject schemas share AImentor.db, with table namespaces, independent versions and one active account contract |

## Calculator interaction

A 56dp bubble snaps to either side and remembers its position. It reserves space above
bottom navigation and stays inside the inset-aware content area. Tapping opens a dialog
up to 360dp wide; its keypad scrolls on short screens. Minimize retains the expression.
Hide removes the bubble; Solve has a permanent reopen action. Long press offers Hide.
It requests no system overlay permission and is absent from Science, History and Programming.

Arithmetic is local: +, −, ×, ÷, parentheses, sign, square, square root and percentage.
Percent divides by 100. Errors are shown for division by zero, invalid expressions,
non-finite results and excessively long input. This is a pocket calculator, not a graphing
or symbolic algebra engine.

## Circuit correction

The schematic now has actual component gaps: source, switch, resistor and lamp are no
longer bypassed by continuous wires. Parallel branches share distinct source poles.
Voltage U and resistances R1/R2 are editable, with responsive input rows. Closing/opening
the switch changes the diagram and calculated current. Series and parallel models report
load equivalent resistance, total/branch current, load voltage and power.

The screen explicitly explains the ideal constant-resistance DC assumption. R2 is the
lamp resistance; this does not simulate temperature or predict physical lamp brightness.
With the switch open, current and power are zero; the reported resistance is load
resistance, not an assertion that the open supply circuit has finite total resistance.

## Misleading behavior corrected

- Home charts now label and compare actual EXP, replacing an invented estimated-time metric.
- Timestamp displays use the device locale/timezone; unknown dates do not become 1970 or now.
- Math category filters store stable keys and recognize older Vietnamese/English labels.
- Local Science reference answers and saved answers explicitly identify their source.
- Programming language counts do not imply mastery; sharing notes is labeled Share.
- Note/conversation deletion requires confirmation and reports the actual database result.
- Reminders only report enabled when notifications are permitted; daily delivery is around
  20:00 because Android scheduling is inexact. New installs are opt-in.
- Profile name and login drafts survive tested appearance changes.
- Recycled Math history rows reset expansion; long LaTeX solutions have a bounded scroll area
  and dark-aware colors. HTML answers are escaped before MathJax rendering.

## Data preservation and architecture

ModuleDatabaseHelper owns migration/version bookkeeping. Existing helpers still own
subject CRUD. A module import creates its namespaced schema, copies compatible columns,
verifies source rows and foreign keys, normalizes recognized timestamps to epoch
milliseconds and marks completion in one transaction. Failure rolls back the module import.
Original module files remain unchanged as backups; subsequent opens do not import again.
Unowned old Math records remain stored without guessing their owner.

Pure CalculatorEngine and CircuitModel separate computation from Android UI. Existing
account/profile/dashboard interfaces remain the SOLID boundaries. Shared ActiveUser,
Timestamps, TimeLabels and AdaptiveCardRow avoid separate implementations per subject.
This is incremental modularization; large legacy Programming controllers remain candidates
for future decomposition rather than claiming the entire codebase is perfectly SOLID.

## Verification

- JDK 21; debug and release assemblies pass.
- 74 unit tests pass, including calculator parsing and circuit laws.
- All seven Gradle modules pass Android lint with zero errors. Warnings remain:
  app 52, Math 120, History 46, Programming 120, Science 149, ui-core 2, data-core 1.
  Reports overlap dependency findings; do not treat their sum as unique defects.
- 13 instrumentation cases cover account creation/login/password/logout, every main module tab,
  safe insets, account isolation, migration fresh/import/rollback cases, Math calculator,
  circuit interactions, specialized Science tool launch, all four Vietnamese/English ×
  day/night combinations, and preservation of unsaved login/profile text.
- UI checks include normal gesture navigation and a compact 320dp configuration with
  font scale 1.3, dark mode and three-button navigation.
- No paid AI requests were executed; all ten committed API keys remain empty.

## Recommended next work

1. Backend team: authentication/session contract, user-scoped sync and server-side AI
   credentials. Keep the current local account and progress adapters replaceable.
2. Add real AI integration fixtures for offline, timeout/quota, malformed quiz and image
   responses when test credentials are available.
3. Test physical devices, TalkBack, landscape/tablets and very large legacy datasets.
   Current migration tests cover known schemas and controlled conflicts, not every arbitrary old schema.
4. Reduce remaining lint/accessibility warnings and split Programming chat/quiz controllers
   as their features evolve; preserve user isolation and migration regression coverage.
5. Offer an explicit ownership/import UI for unowned legacy Math records and an optional
   storage-backup policy once migration has been validated on real installations.

Built-in reference data and user/AI-authored content retain their original language; changing UI language does not translate saved content.

## Debug follow-up

Reproduced a calculator keypad regression before fixing it: pressing square repeatedly
built a right-associative power chain instead of repeatedly squaring the displayed value.
For example, 2 followed by square three times produced 65,536 instead of 256.
Square now computes the current complete expression. Square root also operates on a
complete displayed value (including the last result); for an incomplete expression it
still inserts the root prefix for continued input. Dragging explicitly cancels long press.

CalculatorInteractionDebugTest covers repeated square, root after entering a value,
root of the previous result, drag clamping, edge persistence and absence of an accidental
Hide dialog while dragging. These extend the instrumentation suite from 13 to 15 cases.
API credentials remain empty; live AI calls are outside this debug session.

## User-research follow-up

The UX review in [UX_RESEARCH.md](UX_RESEARCH.md) distinguishes external research,
individual app reviews, implementation decisions and evidence from this app. Home now
prioritizes learning, forms target recoverable field errors, and the calculator display
remains visible while its keypad scrolls. The Programming editor verifies write outcomes;
unconfigured online-only chat preserves user input. Existing specialized tools remain.

New regression coverage includes simulated INSERT/UPDATE failures, retry, account isolation,
editor recreation, first-use/recent-subject navigation, honest AI-unavailable state and
portrait/landscape calculator interaction. Full visual/AI quality and physical-device
usability remain separate from automated functional checks.

## Shared quiz/history follow-up — 2026-10-17

See [QUIZ_AND_HISTORY_REPORT.md](QUIZ_AND_HISTORY_REPORT.md) for the detailed Vietnamese handoff. All four modules now use common quiz setup, presentation and EXP policy, with 5/10/15 question choices and an account-scoped offline bank. History UI/search/date filtering is shared without changing individual saved questions into fabricated conversations. Bundled KaTeX renders formulas offline. Android 13+ locale changes use context-accessible LocaleManager, including before an Activity exists.

The expanded unit suite has 84 passing cases. Targeted compact-screen tests passed for locale/theme recreation, landscape calculator and actual soft-keyboard interaction. Final suite/publication status is recorded in WORK_IN_PROGRESS.md. Server authentication, secure AI proxy, synchronization, server-side grading and submission idempotency remain backend work. API keys remain empty and live Gemini quality has not been tested.
