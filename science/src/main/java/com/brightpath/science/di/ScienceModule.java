package com.brightpath.science.di;

import android.content.Context;

import com.brightpath.science.data.local.AndroidKeystoreTextCipher;
import com.brightpath.science.data.local.ScienceDbHelper;
import com.brightpath.science.data.local.TextCipher;
import com.brightpath.science.data.remote.GeminiApiClient;
import com.brightpath.science.data.remote.GeminiScienceService;
import com.brightpath.science.data.remote.GeminiScienceStudyPlanGenerator;
import com.brightpath.science.data.remote.LocalScienceService;
import com.brightpath.science.data.remote.LocalScienceStudyPlanGenerator;
import com.brightpath.science.data.remote.ResilientScienceService;
import com.brightpath.science.data.remote.ResilientScienceStudyPlanGenerator;
import com.brightpath.science.data.repository.AssetPeriodicTableRepository;
import com.brightpath.science.data.repository.SQLiteQuizAttemptRepository;
import com.brightpath.science.data.repository.SQLiteScienceBookmarkRepository;
import com.brightpath.science.data.repository.SQLiteScienceHistoryRepository;
import com.brightpath.science.data.repository.SQLiteScienceNoteRepository;
import com.brightpath.science.data.repository.SharedPreferencesQuizVariationRepository;
import com.brightpath.science.data.repository.SharedPreferencesStudyPlanRepository;
import com.brightpath.science.domain.policy.AdaptiveQuizPolicy;
import com.brightpath.science.domain.policy.impl.PerformanceBasedQuizPolicy;
import com.brightpath.science.domain.repository.PeriodicTableRepository;
import com.brightpath.science.domain.repository.QuizAttemptRepository;
import com.brightpath.science.domain.repository.QuizVariationRepository;
import com.brightpath.science.domain.repository.ScienceBookmarkRepository;
import com.brightpath.science.domain.repository.ScienceHistoryRepository;
import com.brightpath.science.domain.repository.ScienceNoteRepository;
import com.brightpath.science.domain.repository.StudyPlanRepository;
import com.brightpath.science.domain.usecase.AskScienceQuestionUseCase;
import com.brightpath.science.domain.usecase.GenerateScienceQuizUseCase;
import com.brightpath.science.domain.usecase.GenerateStudyPlanUseCase;
import com.brightpath.science.domain.usecase.GetScienceStatsUseCase;
import com.brightpath.science.domain.usecase.SaveBookmarkUseCase;
import com.brightpath.science.domain.prompt.BiologyTutorPromptStrategy;
import com.brightpath.science.domain.prompt.ChemistryTutorPromptStrategy;
import com.brightpath.science.domain.prompt.DefaultSciencePromptFactory;
import com.brightpath.science.domain.prompt.PhysicsTutorPromptStrategy;
import com.brightpath.science.util.QuestionFingerprint;
import com.brightpath.science.util.QuestionNormalizer;
import com.brightpath.science.util.ScienceQuestionValidator;

/**
 * Composition root owned by the Science module.
 *
 * <p>review NOTE: If the lecturer asks "where are the dependencies connected?",
 * open this class. It is the one place that wires UI-facing use cases to repositories,
 * SQLite, Gemini, local fallback, encryption and prompt strategies.</p>
 *
 * <pre>
 * Presentation
 *    -> Use cases
 *       -> Domain interfaces
 *          -> SQLite / Gemini / Local fallback implementations
 * </pre>
 */
public final class ScienceModule {
    private static volatile ScienceModule instance;

    // region Repositories exposed to presentation/use cases

    private final ScienceHistoryRepository historyRepository;
    private final ScienceBookmarkRepository bookmarkRepository;
    private final ScienceNoteRepository noteRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final PeriodicTableRepository periodicTableRepository;
    private final StudyPlanRepository studyPlanRepository;

    // endregion

    // region Use cases exposed to presentation

    private final AskScienceQuestionUseCase askScienceQuestionUseCase;
    private final SaveBookmarkUseCase saveBookmarkUseCase;
    private final GenerateScienceQuizUseCase generateScienceQuizUseCase;
    private final GetScienceStatsUseCase getScienceStatsUseCase;
    private final GenerateStudyPlanUseCase generateStudyPlanUseCase;

    // endregion

        /*
 * Responsibility: Initializes ScienceModule and receives or prepares the dependencies/state required by this composition root / dependency-injection container.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private ScienceModule(Context context) {
        Context appContext = context.getApplicationContext();

        // 1) Local infrastructure -----------------------------------------------------------
        ScienceDbHelper dbHelper = new ScienceDbHelper(appContext);
        QuestionNormalizer normalizer = new QuestionNormalizer();
        QuestionFingerprint fingerprint = new QuestionFingerprint();
        TextCipher textCipher = new AndroidKeystoreTextCipher();

        historyRepository = new SQLiteScienceHistoryRepository(
                dbHelper,
                normalizer,
                fingerprint,
                textCipher);
        bookmarkRepository = new SQLiteScienceBookmarkRepository(dbHelper, textCipher);
        noteRepository = new SQLiteScienceNoteRepository(dbHelper);
        quizAttemptRepository = new SQLiteQuizAttemptRepository(dbHelper);
        periodicTableRepository = new AssetPeriodicTableRepository(appContext);
        studyPlanRepository = new SharedPreferencesStudyPlanRepository(appContext, textCipher);

        QuizVariationRepository quizVariationRepository =
                new SharedPreferencesQuizVariationRepository(appContext);
        AdaptiveQuizPolicy adaptiveQuizPolicy = new PerformanceBasedQuizPolicy(
                quizAttemptRepository,
                quizVariationRepository);

        // 2) AI prompt strategy -------------------------------------------------------------
        // Each selected Science subject has its own tutor strategy. The factory also tells
        // Gemini to refuse questions that belong to another subject and ask the user to switch.
        DefaultSciencePromptFactory promptFactory = new DefaultSciencePromptFactory(
                new PhysicsTutorPromptStrategy(),
                new ChemistryTutorPromptStrategy(),
                new BiologyTutorPromptStrategy());

        // 3) Remote AI + local fallback -----------------------------------------------------
        GeminiApiClient aiClient = new GeminiApiClient();
        GeminiScienceService geminiService = new GeminiScienceService(appContext, promptFactory, aiClient);
        LocalScienceService localService = new LocalScienceService();
        ResilientScienceService resilientService = new ResilientScienceService(
                geminiService,
                localService);

        // 4) Application use cases ----------------------------------------------------------
        askScienceQuestionUseCase = new AskScienceQuestionUseCase(
                resilientService,
                historyRepository,
                new ScienceQuestionValidator(),
                normalizer);
        saveBookmarkUseCase = new SaveBookmarkUseCase(bookmarkRepository);
        generateScienceQuizUseCase = new GenerateScienceQuizUseCase(
                resilientService,
                adaptiveQuizPolicy,
                quizVariationRepository);
        getScienceStatsUseCase = new GetScienceStatsUseCase(
                historyRepository,
                bookmarkRepository,
                quizAttemptRepository);

        // 5) Study planner -----------------------------------------------------------------
        ResilientScienceStudyPlanGenerator studyPlanGenerator =
                new ResilientScienceStudyPlanGenerator(
                        new GeminiScienceStudyPlanGenerator(aiClient),
                        new LocalScienceStudyPlanGenerator());
        generateStudyPlanUseCase = new GenerateStudyPlanUseCase(
                studyPlanGenerator,
                studyPlanRepository);
    }

    /** Returns the single Science dependency graph for the current application process. */
        /*
 * Responsibility: Returns the ScienceModule composition-root instance for the given application context.
 * Purpose: Provides one central place to obtain shared Science dependencies instead of constructing database/services inside Activities.
 */
public static ScienceModule get(Context context) {
        if (instance == null) {
            synchronized (ScienceModule.class) {
                if (instance == null) {
                    instance = new ScienceModule(context);
                }
            }
        }
        return instance;
    }

    // region Repository accessors

        /*
 * Responsibility: Returns the configured ScienceHistoryRepository implementation.
 * Purpose: Lets screens/use-cases depend on the repository abstraction rather than directly on SQLiteScienceHistoryRepository.
 */
public ScienceHistoryRepository historyRepository() {
        return historyRepository;
    }

        /*
 * Responsibility: Performs the `bookmarkRepository` operation for ScienceModule within the Science Module.
 * Purpose: Encapsulates this responsibility inside the composition root / dependency-injection container so the code is easier to read, test, maintain, and locate during the review.
 */
public ScienceBookmarkRepository bookmarkRepository() {
        return bookmarkRepository;
    }

        /*
 * Responsibility: Returns the configured ScienceNoteRepository implementation.
 * Purpose: Lets the Notes UI use a repository abstraction instead of opening SQLite directly.
 */
public ScienceNoteRepository noteRepository() {
        return noteRepository;
    }

        /*
 * Responsibility: Performs the `quizAttemptRepository` operation for ScienceModule within the Science Module.
 * Purpose: Encapsulates this responsibility inside the composition root / dependency-injection container so the code is easier to read, test, maintain, and locate during the review.
 */
public QuizAttemptRepository quizAttemptRepository() {
        return quizAttemptRepository;
    }

        /*
 * Responsibility: Performs the `periodicTableRepository` operation for ScienceModule within the Science Module.
 * Purpose: Encapsulates this responsibility inside the composition root / dependency-injection container so the code is easier to read, test, maintain, and locate during the review.
 */
public PeriodicTableRepository periodicTableRepository() {
        return periodicTableRepository;
    }

        /*
 * Responsibility: Performs the `studyPlanRepository` operation for ScienceModule within the Science Module.
 * Purpose: Encapsulates this responsibility inside the composition root / dependency-injection container so the code is easier to read, test, maintain, and locate during the review.
 */
public StudyPlanRepository studyPlanRepository() {
        return studyPlanRepository;
    }

    // endregion

    // region Use-case accessors

        /*
 * Responsibility: Exposes the configured AskScienceQuestionUseCase built from validator, normalizer, history repository, and resilient answer service.
 * Purpose: Makes the main Ask AI workflow available to presentation code through dependency injection.
 */
public AskScienceQuestionUseCase askScienceQuestionUseCase() {
        return askScienceQuestionUseCase;
    }

        /*
 * Responsibility: Persists the data supplied to `saveBookmarkUseCase` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
public SaveBookmarkUseCase saveBookmarkUseCase() {
        return saveBookmarkUseCase;
    }

        /*
 * Responsibility: Generates the Science result handled by `generateScienceQuizUseCase` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
public GenerateScienceQuizUseCase generateScienceQuizUseCase() {
        return generateScienceQuizUseCase;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getScienceStatsUseCase` from the appropriate composition root / dependency-injection container.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public GetScienceStatsUseCase getScienceStatsUseCase() {
        return getScienceStatsUseCase;
    }

        /*
 * Responsibility: Generates the Science result handled by `generateStudyPlanUseCase` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
public GenerateStudyPlanUseCase generateStudyPlanUseCase() {
        return generateStudyPlanUseCase;
    }

    // endregion
}
