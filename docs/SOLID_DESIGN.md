# SOLID Design — Science Module

## Architecture overview

```text
Presentation → Use cases/policies → Domain interfaces ← Data implementations
                     ↑
              ScienceModule DI
```

The module owns its UI, database, AI settings and internal dependencies. This prevents Science changes from altering existing team features.

## S — Single Responsibility Principle

Each important class has one main reason to change:

- `ScienceActivity`: chat-screen interaction and lifecycle only.
- `ScienceChatAdapter`: binds user/assistant messages and image previews.
- `AskScienceQuestionUseCase`: validates, checks cache, requests an answer and saves history.
- `DefaultSciencePromptFactory`: assembles shared answer/quiz instructions.
- `PhysicsTutorPromptStrategy`, `ChemistryTutorPromptStrategy`, `BiologyTutorPromptStrategy`: subject-specific teaching rules only.
- `GeminiApiClient`: HTTP request construction, timeout, response parsing and connection cleanup.
- `GeminiScienceService`: maps Science domain requests to Gemini text/quiz requests.
- `PerformanceBasedQuizPolicy`: selects challenge and variation context without changing education level.
- `GenerateScienceQuizUseCase`: coordinates adaptation, generation, validation and anti-repetition storage.
- `GenerateStudyPlanUseCase`: coordinates schedule generation and persistence.
- `LocalScienceStudyPlanGenerator`: deterministic offline schedule content.
- `SQLiteScienceHistoryRepository`: History persistence/query only.
- `AndroidKeystoreTextCipher`: encryption/decryption only.
- `ScienceAnimatedBackgroundView`: lightweight chat background rendering and lifecycle control only.

UI, networking, persistence, scheduling and business decisions are not combined in a single Activity.

## O — Open/Closed Principle

The module is extended through contracts and strategies:

- A future `OpenAiScienceService` can implement `ScienceAnswerService` and `ScienceQuizGenerator`.
- Another HTTP implementation can implement `GenerativeAiClient`.
- New tutor behaviour can implement `ScienceTutorPromptStrategy`.
- A different adaptive algorithm can implement `AdaptiveQuizPolicy`.
- Room/cloud storage can implement the existing repository interfaces.
- A different study planner can implement `ScienceStudyPlanGenerator`.

The high-level use cases do not need to be rewritten for these substitutions.

## L — Liskov Substitution Principle

Remote, local and resilient implementations obey the same contracts:

- `GeminiScienceService` and `LocalScienceService` both return `ScienceAnswer` and quiz-domain objects through `ResultCallback`.
- `ResilientScienceService` can replace either at the use-case boundary without changing the caller.
- `GeminiScienceStudyPlanGenerator` and `LocalScienceStudyPlanGenerator` both return a valid `StudyPlan`; `ResilientScienceStudyPlanGenerator` substitutes safely when remote generation fails.

Callers receive the same domain type and callback behaviour regardless of implementation.

## I — Interface Segregation Principle

Focused interfaces avoid forcing a client to depend on unrelated methods:

- `ScienceAnswerService`
- `ScienceQuizGenerator`
- `ScienceStudyPlanGenerator`
- `ScienceApiConnectionTester`
- `ScienceApiSettings`
- `GenerativeAiClient`
- `ScienceHistoryRepository`
- `ScienceBookmarkRepository`
- `QuizAttemptRepository`
- `QuizVariationRepository`
- `StudyPlanRepository`
- `PeriodicTableRepository`

For example, `ScienceSettingsActivity` depends on API settings and connection testing, not on History or Quiz repositories.

## D — Dependency Inversion Principle

High-level logic depends on abstractions:

```text
ScienceActivity
  → AskScienceQuestionUseCase
      → ScienceAnswerService
      → ScienceHistoryRepository

ScienceQuizActivity
  → GenerateScienceQuizUseCase
      → ScienceQuizGenerator
      → AdaptiveQuizPolicy
      → QuizVariationRepository

ScienceStudyPlannerActivity
  → GenerateStudyPlanUseCase
      → ScienceStudyPlanGenerator
      → StudyPlanRepository
```

`ScienceModule` is the composition root and creates concrete implementations once. Activities do not instantiate Gemini, SQLite or encryption classes directly.

## Additional patterns

- **Strategy:** three subject tutors and adaptive quiz policy.
- **Repository:** storage details hidden behind domain contracts.
- **Use case/Application service:** one class per business operation.
- **Decorator/fallback composition:** resilient remote-to-local Science services.
- **Factory/composition root:** `ScienceModule` centralizes object construction.
- **Adapter:** RecyclerView and list adapters convert domain items into UI.

## Robustness decisions

- Gemini HTTP logic is shared in one client with timeouts and `finally` disconnection.
- Image capture uses a Science-only `FileProvider` cache path.
- Image decoding uses bounds sampling and catches invalid/OOM cases.
- Remote answer/quiz/planner failure falls back locally.
- Database/cache/anti-repetition failures do not block a valid answer or quiz result.
- UI callbacks check Activity lifecycle before changing views.
- Quiz answers and feedback survive configuration changes.
- The first study session uses the captured generation timestamp; AI cannot invent clock times.

## Scope isolation

Science uses `science_ai.db` rather than modifying the existing app database. Existing team Activities, Fragments, adapters and authentication logic are not replaced. Manifest/dependency entries are the only global integration changes required to compile and launch the module.
