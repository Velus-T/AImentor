package com.example.aimentor.science.domain.usecase;

import com.example.aimentor.science.domain.model.GeneratedQuiz;
import com.example.aimentor.science.domain.model.QuizQuestion;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.policy.AdaptiveQuizPolicy;
import com.example.aimentor.science.domain.repository.QuizVariationRepository;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceQuizGenerator;

import java.util.ArrayList;
import java.util.List;

/** Orchestrates adaptation, generation and anti-repetition storage. */
public final class GenerateScienceQuizUseCase {
    private final ScienceQuizGenerator generator;
    private final AdaptiveQuizPolicy adaptivePolicy;
    private final QuizVariationRepository variationRepository;

    public GenerateScienceQuizUseCase(ScienceQuizGenerator generator,
                                      AdaptiveQuizPolicy adaptivePolicy,
                                      QuizVariationRepository variationRepository) {
        this.generator = generator;
        this.adaptivePolicy = adaptivePolicy;
        this.variationRepository = variationRepository;
    }

    public void execute(int userId, QuizRequest request,
                        ResultCallback<GeneratedQuiz> callback) {
        if (request == null) {
            callback.onError("Quiz request is missing.", null);
            return;
        }

        final QuizRequest adaptedRequest;
        try {
            adaptedRequest = adaptivePolicy.adapt(userId, request);
        } catch (Exception error) {
            callback.onError("Unable to prepare an adaptive quiz safely.", error);
            return;
        }
        generator.generateQuiz(adaptedRequest, new ResultCallback<List<QuizQuestion>>() {
            @Override
            public void onSuccess(List<QuizQuestion> questions) {
                if (questions == null || questions.size() < adaptedRequest.getNumberOfQuestions()) {
                    callback.onError("The quiz generator returned too few questions. Please generate again.", null);
                    return;
                }
                List<QuizQuestion> safeQuestions = new ArrayList<>();
                List<String> prompts = new ArrayList<>();
                for (QuizQuestion question : questions) {
                    if (question == null || question.getPrompt() == null
                            || question.getPrompt().trim().isEmpty()) continue;
                    safeQuestions.add(question);
                    prompts.add(question.getPrompt());
                    if (safeQuestions.size() >= adaptedRequest.getNumberOfQuestions()) break;
                }
                if (safeQuestions.size() < adaptedRequest.getNumberOfQuestions()) {
                    callback.onError("The quiz contained invalid questions. Please generate again.", null);
                    return;
                }
                try {
                    variationRepository.rememberPrompts(
                            userId,
                            adaptedRequest.getSubject(),
                            adaptedRequest.getLevel(),
                            prompts
                    );
                } catch (Exception ignored) {
                    // Anti-repetition storage is best-effort and must not block a valid quiz.
                }
                callback.onSuccess(new GeneratedQuiz(adaptedRequest, safeQuestions));
            }

            @Override
            public void onError(String message, Throwable error) {
                callback.onError(message, error);
            }
        });
    }
}
