package com.example.aimentor.science.domain.service;

import com.example.aimentor.science.domain.model.QuizQuestion;
import com.example.aimentor.science.domain.model.QuizRequest;

import java.util.List;

public interface ScienceQuizGenerator {
    void generateQuiz(QuizRequest request, ResultCallback<List<QuizQuestion>> callback);
}
