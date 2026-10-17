package com.example.mathmodule;

import com.example.mathmodule.databases.QuizQuestion;
import com.example.mathmodule.util.MathQuizDifficulty;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
 * Role: Test class for Math, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class MathQuizQuestionTest {
    @Test
    public void normalizesAnswerAndKeepsMathDifficultyReward() {
        QuizQuestion question = new QuizQuestion(
                1, 7, "  Tính 3 x 4  ", " 10 ", " 12 ", " 14 ", " 16 ",
                " b ", " 3 nhân 4 bằng 12 ", "HARD");

        assertEquals("Tính 3 x 4", question.getQuestionText());
        assertEquals("B", question.getCorrectAnswer());
        assertEquals(MathQuizDifficulty.HARD, question.getDifficulty());
        assertEquals(20, question.getExpReward());
    }

    /*
     * Responsibility: Performs the `invalid correct and selected answers are handled safely` responsibility of `MathQuizQuestionTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void invalidCorrectAndSelectedAnswersAreHandledSafely() {
        QuizQuestion question = new QuizQuestion(
                2, 8, null, null, null, null, null,
                "Z", null, (MathQuizDifficulty) null);

        assertEquals("", question.getQuestionText());
        assertEquals("", question.getCorrectAnswer());
        assertEquals(MathQuizDifficulty.MEDIUM, question.getDifficulty());

        question.setSelectedAnswer(" d ");
        assertEquals("D", question.getSelectedAnswer());
        question.setSelectedAnswer("X");
        assertEquals("", question.getSelectedAnswer());
    }
}
