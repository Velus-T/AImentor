package com.example.aimentor.science.domain.usecase;

import com.example.aimentor.science.domain.model.ScienceAnswer;
import com.example.aimentor.science.domain.model.ScienceHistory;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.repository.ScienceHistoryRepository;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceAnswerService;
import com.example.aimentor.science.util.QuestionNormalizer;
import com.example.aimentor.science.util.ScienceQuestionValidator;

/** Coordinates validation, cache lookup, AI response and best-effort persistence. */
public final class AskScienceQuestionUseCase {
    private final ScienceAnswerService answerService;
    private final ScienceHistoryRepository historyRepository;
    private final ScienceQuestionValidator validator;
    private final QuestionNormalizer normalizer;

    public AskScienceQuestionUseCase(ScienceAnswerService answerService,
                                     ScienceHistoryRepository historyRepository,
                                     ScienceQuestionValidator validator,
                                     QuestionNormalizer normalizer) {
        this.answerService = answerService;
        this.historyRepository = historyRepository;
        this.validator = validator;
        this.normalizer = normalizer;
    }

    public void execute(ScienceQuestion question, ResultCallback<ScienceAnswer> callback) {
        ScienceQuestionValidator.ValidationResult validation = validator.validate(question);
        if (!validation.isValid()) {
            callback.onError(validation.getMessage(), null);
            return;
        }

        String normalizedQuestion = normalizer.normalize(question.getQuestionText());
        if (!normalizedQuestion.isEmpty() && !question.hasImage()) {
            try {
                ScienceHistory cached = historyRepository.findCached(
                        question.getUserId(),
                        question.getSubject(),
                        question.getEducationLevel().name(),
                        normalizedQuestion
                );
                if (cached != null) {
                    callback.onSuccess(new ScienceAnswer(
                            cached.getAnswer(),
                            cached.getSubject().getDisplayName(),
                            cached.getEducationLevel(),
                            true,
                            cached.getId()
                    ));
                    return;
                }
            } catch (Exception ignored) {
                // A damaged or temporarily locked local database must not block the AI response.
            }
        }

        answerService.answer(question, new ResultCallback<ScienceAnswer>() {
            @Override
            public void onSuccess(ScienceAnswer answer) {
                long historyId = 0L;
                try {
                    historyId = historyRepository.save(question, answer.getContent());
                } catch (Exception ignored) {
                    // The answer remains usable even if encrypted local persistence fails.
                }
                callback.onSuccess(answer.withPersistence(historyId, false));
            }

            @Override
            public void onError(String message, Throwable error) {
                callback.onError(message, error);
            }
        });
    }
}
