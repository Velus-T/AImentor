package com.brightpath.science.domain.service;

import com.brightpath.science.domain.model.QuizQuestion;
import com.brightpath.science.domain.model.QuizRequest;

import java.util.List;

public interface ScienceQuizGenerator {
        /*
 * Responsibility: Generates the Science result handled by `generateQuiz` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
void generateQuiz(QuizRequest request, ResultCallback<List<QuizQuestion>> callback);
}
