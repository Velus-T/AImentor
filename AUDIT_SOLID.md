# Audit and SOLID refactor

## Scope and limits
Reviewed the app shell and the Math, Science, History and Programming modules:
navigation, forms, shared layouts, user-scoped database access, migrations,
background callbacks, activity history and module dependencies. This is a targeted
correctness and architecture pass, not a claim that every possible defect is gone.
Real Gemini calls are excluded because the public API configuration is empty.

## Corrected findings

| Finding | Correction |
| --- | --- |
| Home level label could overlap its value in larger translations | Bound the label before the value and allow wrapping |
| Logout only finished Home after Login had already finished | Both logout entries clear the task and open Login |
| Drawer, bottom tabs and profile back stack could disagree | Home owns navigation and synchronizes selection after back/restoration |
| Math/Programming events were assigned fabricated current timestamps | Normalize persisted epoch/SQLite UTC dates and order by actual time |
| One malformed History timestamp could suppress the remaining entries | Timestamp conversion handles each invalid value as unknown |
| Password hashing/checking and aggregate dashboard queries ran on UI | Account/profile actions and progress reads use the shared worker executor |
| Registration/profile password policies could drift | One PasswordPolicy is used by both screens |
| Callback could update an old or destroyed view | Async action disposal, view identity and generation checks guard completion |
| Failed dashboard source silently appeared as zero data | Preserve the other subjects and expose a retryable partial-data notice |
| Feature layouts depended on host app classes/resources | Reusable card row and motion resources move into ui-core; features depend on that library |
| Reading Math data assigned all ownerless legacy rows to the first account | Keep user_id=0 data stored, and require an explicit verified ownership migration |
| Science downgrade dropped learning tables | Reject unsupported downgrade without deleting data |
| Profile write could run before a successful initial read | Retry loading first; do not overwrite existing profile with blank fields |

The Math legacy change does not delete records or attempt to guess ownership.
Previously ownerless records may no longer appear in a signed-in account; a separate
migration/import flow must establish the correct owner before presenting them.

## SOLID boundaries

- **Single responsibility:** account persistence, profile persistence, timestamp
  conversion, progress aggregation, UI action scheduling and rendering are separate.
- **Open/closed:** CompositeDashboardRepository aggregates SubjectProgressSource
  implementations; local sources can be replaced by remote adapters without changing
  the aggregation or rendering logic.
- **Liskov substitution:** sources return the same immutable progress contract;
  tests substitute fake sources for local persistence and verify ordering/failure isolation.
- **Interface segregation:** Login uses UserAuthenticator, Register uses
  UserRegistration, dashboard resolves accounts through AccountLookup, and Profile
  uses UserProfileRepository. Screens do not need the entire database API.
- **Dependency inversion:** screens consume these contracts; AppServices composes
  the local implementations. No dependency-injection framework is required.

Science's existing domain/repository/service interfaces are retained. This pass does
not rewrite every module or introduce interfaces around trivial utility methods.

## Validation

- Debug and release APK builds.
- App lint (including dependent modules): zero errors; 50 remaining warnings.
- 58 unit tests: 49 existing module tests, plus 9 shell regressions covering UTC/epoch
  timestamps, ordering, per-user contracts, partial failure, anonymous access,
  duplicate submissions, disposed views, retry and shared password policy.
- CanonicalSmokeTest includes registration, login, profile/password change, logout,
  profile back navigation, all module tabs, safe system/keyboard insets, explicit
  Math account isolation/ownerless-data preservation and unsupported Science downgrade.
- All five instrumentation tests passed in both normal/light/gesture mode and
  320dp/dark/font-scale-1.3/three-button mode. The final level-label layout change
  is additionally checked through the full four-module UI navigation test.

## Remaining work

1. Run real AI integration checks for timeouts, quota errors and image inputs when
   credentials are provided; do not infer successful network behavior from UI tests.
2. Verify physical Android devices and accessibility services beyond the emulator cases.
3. Replace the local account/session storage with a server-backed identity contract
   before cloud synchronization; app navigation is not a server authorization boundary.
4. Define an explicit migration/import policy for old ownerless Math records.
5. Further split large feature controllers (especially Programming quiz/chat) into
   presenters/use cases as those features change. Preserve migration and user isolation tests.
6. Review remaining lint warnings and language restrictions (Gmail-only registration)
   as product decisions; do not silently relax validation during architecture work.
