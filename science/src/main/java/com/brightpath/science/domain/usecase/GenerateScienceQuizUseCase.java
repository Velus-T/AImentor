package com.brightpath.science.domain.usecase;

import com.brightpath.science.domain.model.GeneratedQuiz;
import com.brightpath.science.domain.model.QuizQuestion;
import com.brightpath.science.domain.model.QuizRequest;
import com.brightpath.science.domain.policy.AdaptiveQuizPolicy;
import com.brightpath.science.domain.repository.QuizVariationRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceQuizGenerator;
import java.util.ArrayList;
import java.util.List;

/** Orchestrates adaptation, generation and anti-repetition storage. */
public final class GenerateScienceQuizUseCase {
  private final ScienceQuizGenerator generator;
  private final AdaptiveQuizPolicy adaptivePolicy;
  private final QuizVariationRepository variationRepository;

  /*
   * Responsibility: Initializes GenerateScienceQuizUseCase and receives or prepares the dependencies/state required by this application/business use-case.
   * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
   */
  public GenerateScienceQuizUseCase(
      ScienceQuizGenerator generator,
      AdaptiveQuizPolicy adaptivePolicy,
      QuizVariationRepository variationRepository) {
    this.generator = generator;
    this.adaptivePolicy = adaptivePolicy;
    this.variationRepository = variationRepository;
  }

  /*
   * Responsibility: Determines the adaptive quiz challenge, requests a new quiz variation, and returns a GeneratedQuiz through the callback.
   * Purpose: Keeps adaptive-learning decisions outside the Activity and makes quiz behavior reusable/testable.
   */
  public void execute(int userId, QuizRequest request, ResultCallback<GeneratedQuiz> callback) {
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
    generator.generateQuiz(
        adaptedRequest,
        new ResultCallback<List<QuizQuestion>>() {
          /*
           * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
           * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
           */
          @Override
          public void onSuccess(List<QuizQuestion> questions) {
            if (questions == null || questions.size() < adaptedRequest.getNumberOfQuestions()) {
              Throwable error =
                  questions != null && questions.stream().allMatch(QuizQuestion::isLocal)
                      ? new com.brightpath.science.domain.model.InsufficientLocalQuizException(
                          questions.size(), adaptedRequest.getNumberOfQuestions())
                      : null;
              callback.onError(
                  "The quiz generator returned too few questions. Please generate again.", error);
              return;
            }
            List<QuizQuestion> safeQuestions = new ArrayList<>();
            List<String> prompts = new ArrayList<>();
            for (QuizQuestion question : questions) {
              if (question == null
                  || question.getPrompt() == null
                  || question.getPrompt().trim().isEmpty()) continue;
              safeQuestions.add(question);
              prompts.add(question.getPrompt());
              if (safeQuestions.size() >= adaptedRequest.getNumberOfQuestions()) break;
            }
            if (safeQuestions.size() < adaptedRequest.getNumberOfQuestions()) {
              callback.onError(
                  "The quiz contained invalid questions. Please generate again.", null);
              return;
            }
            try {
              variationRepository.rememberPrompts(
                  userId, adaptedRequest.getSubject(), adaptedRequest.getLevel(), prompts);
            } catch (Exception ignored) {
              // Anti-repetition storage is best-effort and must not block a valid quiz.
            }
            callback.onSuccess(new GeneratedQuiz(adaptedRequest, safeQuestions));
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
}
