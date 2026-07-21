package com.example.aimentor.science.di;

import android.content.Context;

import com.example.aimentor.science.data.local.AndroidKeystoreTextCipher;
import com.example.aimentor.science.data.local.EncryptedScienceApiSettings;
import com.example.aimentor.science.data.local.ScienceDbHelper;
import com.example.aimentor.science.data.local.TextCipher;
import com.example.aimentor.science.data.remote.GeminiApiClient;
import com.example.aimentor.science.data.remote.GeminiScienceService;
import com.example.aimentor.science.data.remote.LocalScienceService;
import com.example.aimentor.science.data.remote.ResilientScienceService;
import com.example.aimentor.science.data.remote.GeminiScienceStudyPlanGenerator;
import com.example.aimentor.science.data.remote.LocalScienceStudyPlanGenerator;
import com.example.aimentor.science.data.remote.ResilientScienceStudyPlanGenerator;
import com.example.aimentor.science.data.repository.AssetPeriodicTableRepository;
import com.example.aimentor.science.data.repository.SQLiteQuizAttemptRepository;
import com.example.aimentor.science.data.repository.SQLiteScienceBookmarkRepository;
import com.example.aimentor.science.data.repository.SQLiteScienceHistoryRepository;
import com.example.aimentor.science.data.repository.SharedPreferencesQuizVariationRepository;
import com.example.aimentor.science.data.repository.SharedPreferencesStudyPlanRepository;
import com.example.aimentor.science.domain.repository.PeriodicTableRepository;
import com.example.aimentor.science.domain.repository.QuizAttemptRepository;
import com.example.aimentor.science.domain.repository.ScienceBookmarkRepository;
import com.example.aimentor.science.domain.repository.ScienceHistoryRepository;
import com.example.aimentor.science.domain.repository.QuizVariationRepository;
import com.example.aimentor.science.domain.repository.StudyPlanRepository;
import com.example.aimentor.science.domain.service.GenerativeAiClient;
import com.example.aimentor.science.domain.service.ScienceApiSettings;
import com.example.aimentor.science.domain.service.ScienceApiConnectionTester;
import com.example.aimentor.science.domain.policy.AdaptiveQuizPolicy;
import com.example.aimentor.science.domain.policy.impl.PerformanceBasedQuizPolicy;
import com.example.aimentor.science.domain.usecase.AskScienceQuestionUseCase;
import com.example.aimentor.science.domain.usecase.GenerateScienceQuizUseCase;
import com.example.aimentor.science.domain.usecase.GetScienceStatsUseCase;
import com.example.aimentor.science.domain.usecase.SaveBookmarkUseCase;
import com.example.aimentor.science.domain.usecase.GenerateStudyPlanUseCase;
import com.example.aimentor.science.util.BiologyTutorPromptStrategy;
import com.example.aimentor.science.util.ChemistryTutorPromptStrategy;
import com.example.aimentor.science.util.DefaultSciencePromptFactory;
import com.example.aimentor.science.util.PhysicsTutorPromptStrategy;
import com.example.aimentor.science.util.QuestionFingerprint;
import com.example.aimentor.science.util.QuestionNormalizer;
import com.example.aimentor.science.util.ScienceQuestionValidator;

/** Composition root owned by the Science module. */
public final class ScienceModule {
    private static volatile ScienceModule instance;

    private final ScienceHistoryRepository historyRepository;
    private final ScienceBookmarkRepository bookmarkRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final PeriodicTableRepository periodicTableRepository;
    private final ScienceApiSettings apiSettings;
    private final ScienceApiConnectionTester apiConnectionTester;
    private final AskScienceQuestionUseCase askScienceQuestionUseCase;
    private final SaveBookmarkUseCase saveBookmarkUseCase;
    private final GenerateScienceQuizUseCase generateScienceQuizUseCase;
    private final GetScienceStatsUseCase getScienceStatsUseCase;
    private final StudyPlanRepository studyPlanRepository;
    private final GenerateStudyPlanUseCase generateStudyPlanUseCase;

    private ScienceModule(Context context) {
        Context appContext = context.getApplicationContext();
        ScienceDbHelper dbHelper = new ScienceDbHelper(appContext);
        QuestionNormalizer normalizer = new QuestionNormalizer();
        QuestionFingerprint fingerprint = new QuestionFingerprint();
        TextCipher textCipher = new AndroidKeystoreTextCipher();

        historyRepository = new SQLiteScienceHistoryRepository(dbHelper, normalizer, fingerprint, textCipher);
        bookmarkRepository = new SQLiteScienceBookmarkRepository(dbHelper, textCipher);
        quizAttemptRepository = new SQLiteQuizAttemptRepository(dbHelper);
        QuizVariationRepository quizVariationRepository =
                new SharedPreferencesQuizVariationRepository(appContext);
        AdaptiveQuizPolicy adaptiveQuizPolicy = new PerformanceBasedQuizPolicy(
                quizAttemptRepository, quizVariationRepository);
        periodicTableRepository = new AssetPeriodicTableRepository(appContext);
        apiSettings = new EncryptedScienceApiSettings(appContext, textCipher);
        studyPlanRepository = new SharedPreferencesStudyPlanRepository(appContext, textCipher);

        DefaultSciencePromptFactory promptFactory = new DefaultSciencePromptFactory(
                new PhysicsTutorPromptStrategy(),
                new ChemistryTutorPromptStrategy(),
                new BiologyTutorPromptStrategy()
        );
        GenerativeAiClient aiClient = new GeminiApiClient();
        GeminiScienceService gemini = new GeminiScienceService(apiSettings, promptFactory, aiClient);
        apiConnectionTester = gemini;
        LocalScienceService local = new LocalScienceService();
        ResilientScienceService resilientService = new ResilientScienceService(gemini, local);

        askScienceQuestionUseCase = new AskScienceQuestionUseCase(
                resilientService,
                historyRepository,
                new ScienceQuestionValidator(),
                normalizer
        );
        saveBookmarkUseCase = new SaveBookmarkUseCase(bookmarkRepository);
        generateScienceQuizUseCase = new GenerateScienceQuizUseCase(
                resilientService, adaptiveQuizPolicy, quizVariationRepository);
        getScienceStatsUseCase = new GetScienceStatsUseCase(
                historyRepository,
                bookmarkRepository,
                quizAttemptRepository
        );
        ResilientScienceStudyPlanGenerator studyPlanGenerator =
                new ResilientScienceStudyPlanGenerator(
                        new GeminiScienceStudyPlanGenerator(apiSettings, aiClient),
                        new LocalScienceStudyPlanGenerator());
        generateStudyPlanUseCase = new GenerateStudyPlanUseCase(
                studyPlanGenerator, studyPlanRepository);
    }

    public static ScienceModule get(Context context) {
        if (instance == null) {
            synchronized (ScienceModule.class) {
                if (instance == null) instance = new ScienceModule(context);
            }
        }
        return instance;
    }

    public ScienceHistoryRepository historyRepository() { return historyRepository; }
    public ScienceBookmarkRepository bookmarkRepository() { return bookmarkRepository; }
    public QuizAttemptRepository quizAttemptRepository() { return quizAttemptRepository; }
    public PeriodicTableRepository periodicTableRepository() { return periodicTableRepository; }
    public ScienceApiSettings apiSettings() { return apiSettings; }
    public ScienceApiConnectionTester apiConnectionTester() { return apiConnectionTester; }
    public AskScienceQuestionUseCase askScienceQuestionUseCase() { return askScienceQuestionUseCase; }
    public SaveBookmarkUseCase saveBookmarkUseCase() { return saveBookmarkUseCase; }
    public GenerateScienceQuizUseCase generateScienceQuizUseCase() { return generateScienceQuizUseCase; }
    public GetScienceStatsUseCase getScienceStatsUseCase() { return getScienceStatsUseCase; }
    public StudyPlanRepository studyPlanRepository() { return studyPlanRepository; }
    public GenerateStudyPlanUseCase generateStudyPlanUseCase() { return generateStudyPlanUseCase; }
}
