package com.brightpath.science;

import static org.junit.Assert.*;

import com.brightpath.science.data.remote.LocalAdaptiveQuizBank;
import com.brightpath.science.domain.model.*;
import com.brightpath.science.domain.repository.QuizVariationRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.usecase.GenerateScienceQuizUseCase;
import java.util.*;
import org.junit.Test;

public class ScienceQuizInventoryTest {
  private final QuizVariationRepository variations =
      new QuizVariationRepository() {
        @Override
        public List<String> getRecentPrompts(
            int user, ScienceSubject subject, EducationLevel level, int limit) {
          return Collections.emptyList();
        }

        @Override
        public void rememberPrompts(
            int user, ScienceSubject subject, EducationLevel level, List<String> prompts) {}
      };

  @Test
  public void insufficientBuiltInSampleNeverPretendsToContainFifteenQuestions() {
    GenerateScienceQuizUseCase useCase =
        new GenerateScienceQuizUseCase(
            (request, callback) ->
                callback.onSuccess(new LocalAdaptiveQuizBank().generate(request)),
            (user, request) -> request,
            variations);
    final Throwable[] failure = {null};
    useCase.execute(
        1,
        new QuizRequest(ScienceSubject.PHYSICS, EducationLevel.MIDDLE_SCHOOL, "Mechanics", 15),
        new ResultCallback<GeneratedQuiz>() {
          @Override
          public void onSuccess(GeneratedQuiz result) {
            fail("Must not silently shorten a quiz");
          }

          @Override
          public void onError(String message, Throwable error) {
            failure[0] = error;
          }
        });
    assertTrue(failure[0] instanceof InsufficientLocalQuizException);
    assertEquals(15, ((InsufficientLocalQuizException) failure[0]).requested);
  }

  @Test
  public void fiveQuestionSampleRetainsItsActualSourceAcrossAllSubjectsAndLevels() {
    for (ScienceSubject subject : ScienceSubject.values())
      for (EducationLevel level : EducationLevel.values()) {
        List<QuizQuestion> questions =
            new LocalAdaptiveQuizBank().generate(new QuizRequest(subject, level, "General", 5));
        assertEquals(5, questions.size());
        assertTrue(questions.stream().allMatch(QuizQuestion::isLocal));
        assertEquals(5, questions.stream().map(QuizQuestion::getPrompt).distinct().count());
      }
  }
}
