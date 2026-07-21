package com.example.aimentor.science;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.aimentor.science.data.remote.LocalAdaptiveQuizBank;
import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.QuizChallenge;
import com.example.aimentor.science.domain.model.QuizPerformance;
import com.example.aimentor.science.domain.model.QuizQuestion;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.model.ScienceSubject;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class LocalAdaptiveQuizBankTest {
    private final LocalAdaptiveQuizBank bank = new LocalAdaptiveQuizBank();

    @Test
    public void supportAndChallengeChangeQuestionMixWithoutChangingLevel() {
        QuizRequest base = new QuizRequest(
                ScienceSubject.PHYSICS,
                EducationLevel.HIGH_SCHOOL,
                "Mechanics",
                5
        );

        QuizRequest support = base.withAdaptiveContext(
                1,
                QuizChallenge.SUPPORT,
                new QuizPerformance(2, 5, 40),
                12345L,
                Collections.emptyList()
        );
        QuizRequest challenge = base.withAdaptiveContext(
                1,
                QuizChallenge.CHALLENGE,
                new QuizPerformance(5, 5, 100),
                12345L,
                Collections.emptyList()
        );

        List<QuizQuestion> supportQuestions = bank.generate(support);
        List<QuizQuestion> challengeQuestions = bank.generate(challenge);

        assertEquals(EducationLevel.HIGH_SCHOOL, support.getLevel());
        assertEquals(EducationLevel.HIGH_SCHOOL, challenge.getLevel());
        assertTrue(supportQuestions.get(0).getType() == QuizQuestion.Type.TRUE_FALSE
                || supportQuestions.get(0).getType() == QuizQuestion.Type.FILL_BLANK);
        assertTrue(challengeQuestions.get(0).getType() == QuizQuestion.Type.MATCHING
                || challengeQuestions.get(0).getType() == QuizQuestion.Type.MULTIPLE_CHOICE);
        assertNotEquals(supportQuestions.get(0).getPrompt(), challengeQuestions.get(0).getPrompt());
    }
}
