package com.brightpath.science.domain.policy;

import com.brightpath.science.domain.model.QuizRequest;

public interface AdaptiveQuizPolicy {
        /*
 * Responsibility: Performs the `adapt` operation for AdaptiveQuizPolicy within the Science Module.
 * Purpose: Encapsulates this responsibility inside the learning/business-rule component so the code is easier to read, test, maintain, and locate during the review.
 */
QuizRequest adapt(int userId, QuizRequest baseRequest);
}
