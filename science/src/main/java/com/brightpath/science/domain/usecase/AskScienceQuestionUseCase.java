package com.brightpath.science.domain.usecase;

import com.brightpath.science.domain.model.ScienceAnswer;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.domain.model.ScienceQuestion;
import com.brightpath.science.domain.repository.ScienceHistoryRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceAnswerService;
import com.brightpath.science.util.QuestionNormalizer;
import com.brightpath.science.util.ScienceQuestionValidator;

/**
 * Coordinates the complete "Send Science question" business flow.
 *
 * <p>review FLOW:</p>
 * <pre>
 * ScienceActivity
 *   -> execute(question)
 *   -> validate input
 *   -> normalize + safe cache lookup
 *   -> ScienceAnswerService (Gemini with local fallback)
 *   -> save successful answer to ScienceHistoryRepository
 *   -> callback result to the UI
 * </pre>
 *
 * <p>This class contains application logic only. It does not know Android Views,
 * HTTP details or raw SQLite queries.</p>
 */
public final class AskScienceQuestionUseCase {
    private final ScienceAnswerService answerService;
    private final ScienceHistoryRepository historyRepository;
    private final ScienceQuestionValidator validator;
    private final QuestionNormalizer normalizer;

        /*
 * Responsibility: Initializes AskScienceQuestionUseCase and receives or prepares the dependencies/state required by this application/business use-case.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public AskScienceQuestionUseCase(ScienceAnswerService answerService,
                                     ScienceHistoryRepository historyRepository,
                                     ScienceQuestionValidator validator,
                                     QuestionNormalizer normalizer) {
        this.answerService = answerService;
        this.historyRepository = historyRepository;
        this.validator = validator;
        this.normalizer = normalizer;
    }

        /*
 * Responsibility: Coordinates the complete Ask Science AI flow: validate the request, normalize/check cache, call the answer service when needed, and save valid history.
 * Purpose: Centralizes the business workflow so the UI stays thin and the same logic can be unit-tested independently.
 */
public void execute(ScienceQuestion question, ResultCallback<ScienceAnswer> callback) {
        // 1) Reject invalid input before cache, network or database-save work.
        ScienceQuestionValidator.ValidationResult validation = validator.validate(question);
        if (!validation.isValid()) {
            callback.onError(validation.getMessage(), null);
            return;
        }

        // 2) Reuse a previous exact/normalized standalone answer when it is safe.
        String normalizedQuestion = normalizer.normalize(question.getQuestionText());
        if (isSafeStandaloneCacheRequest(question, normalizedQuestion)) {
            ScienceHistory cached = findCachedAnswer(question, normalizedQuestion);
            if (cached != null) {
                callback.onSuccess(asCachedAnswer(question, cached));
                return;
            }
        }

        // 3) No safe cache -> ask the configured Science answer service.
        answerService.answer(question, new ResultCallback<ScienceAnswer>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onSuccess(ScienceAnswer answer) {
                long historyId = saveBestEffort(question, answer.getContent());
                callback.onSuccess(answer.withPersistence(historyId, false));
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onError(String message, Throwable error) {
                callback.onError(message, error);
            }
        });
    }

        /*
 * Responsibility: Searches Science history for a reusable answer that matches the current user, subject, education level, and normalized question.
 * Purpose: Avoids unnecessary Gemini calls, reduces response time/API usage, and still respects user/subject boundaries.
 */
private ScienceHistory findCachedAnswer(ScienceQuestion question, String normalizedQuestion) {
        try {
            return historyRepository.findCached(
                    question.getUserId(),
                    question.getSubject(),
                    question.getEducationLevel().name(),
                    normalizedQuestion);
        } catch (Exception ignored) {
            // A damaged/locked local DB must not prevent a new AI request.
            return null;
        }
    }

        /*
 * Responsibility: Performs the `asCachedAnswer` operation for AskScienceQuestionUseCase within the Science Module.
 * Purpose: Encapsulates this responsibility inside the application/business use-case so the code is easier to read, test, maintain, and locate during the review.
 */
private ScienceAnswer asCachedAnswer(ScienceQuestion question, ScienceHistory cached) {
        long historyId = cached.getId();
        try {
            // Record the reuse in the current conversation/history when possible.
            historyId = historyRepository.save(question, cached.getAnswer());
        } catch (Exception ignored) {
            // The cached answer is still usable even if the new history row cannot be saved.
        }

        return new ScienceAnswer(
                cached.getAnswer(),
                cached.getSubject().getDisplayName(),
                cached.getEducationLevel(),
                true,
                historyId);
    }

        /*
 * Responsibility: Attempts to persist a valid question-answer pair to Science history after an answer is available.
 * Purpose: Preserves learning history without allowing a secondary persistence failure to crash the successful AI response flow.
 */
private long saveBestEffort(ScienceQuestion question, String answerContent) {
        try {
            return historyRepository.save(question, answerContent);
        } catch (Exception ignored) {
            // The answer remains visible even if encrypted local persistence fails.
            return 0L;
        }
    }

    /**
     * Cached answers are safe only for standalone questions. A short follow-up such as
     * "why?" or "continue step 2" must be answered with the current conversation, never
     * by reusing an unrelated historical answer with the same short text.
     */
        /*
 * Responsibility: Checks the condition represented by `isSafeStandaloneCacheRequest` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
private boolean isSafeStandaloneCacheRequest(ScienceQuestion question,
                                                 String normalizedQuestion) {
        if (question == null || question.hasImage() || question.hasConversationContext()) {
            return false;
        }
        if (normalizedQuestion == null || normalizedQuestion.length() < 12) {
            return false;
        }

        String value = normalizedQuestion.trim();
        String[] contextDependentPrefixes = {
                "why", "why is", "why does", "how so", "continue", "go on",
                "explain again", "what about", "and then", "step",
                "tai sao", "vi sao", "the con", "vay con", "con", "tiep tuc",
                "giai thich lai", "buoc", "do la gi", "cai nay", "truong hop kia"
        };

        for (String prefix : contextDependentPrefixes) {
            if (value.equals(prefix) || value.startsWith(prefix + " ")) {
                return false;
            }
        }
        return true;
    }
}
