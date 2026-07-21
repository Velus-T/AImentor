package com.example.aimentor.science.domain.service;

import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.model.ScienceQuestion;

public interface SciencePromptFactory {
    String buildAnswerPrompt(ScienceQuestion question);
    String buildQuizPrompt(QuizRequest request);
}
