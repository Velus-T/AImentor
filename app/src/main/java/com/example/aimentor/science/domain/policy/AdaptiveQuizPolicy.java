package com.example.aimentor.science.domain.policy;

import com.example.aimentor.science.domain.model.QuizRequest;

public interface AdaptiveQuizPolicy {
    QuizRequest adapt(int userId, QuizRequest baseRequest);
}
