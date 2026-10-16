package com.brightpath.science.domain.service;

import com.brightpath.science.domain.model.QuizRequest;
import com.brightpath.science.domain.model.ScienceQuestion;

public interface SciencePromptFactory {
        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildAnswerPrompt`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
String buildAnswerPrompt(ScienceQuestion question);
        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildQuizPrompt`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
String buildQuizPrompt(QuizRequest request);
}
