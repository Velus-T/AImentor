package com.example.aimentor.science.domain.repository;

import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.QuizChallenge;
import com.example.aimentor.science.domain.model.QuizPerformance;
import com.example.aimentor.science.domain.model.ScienceSubject;

public interface QuizAttemptRepository {
    long saveAttempt(int userId, ScienceSubject subject, EducationLevel level,
                     String topic, QuizChallenge challenge,
                     int totalQuestions, int correctAnswers);

    QuizPerformance getRecentPerformance(int userId, ScienceSubject subject,
                                         EducationLevel level, int attemptLimit);

    int countAttempts(int userId);
    double calculateAccuracy(int userId);
}
