package com.example.mathmodule;

import com.example.mathmodule.util.MathQuizDifficulty;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
 * Role: Test class for Math, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class MathQuizDifficultyTest {
    @Test
    public void mapsApiDifficultyValuesToMathDifficulty() {
        assertEquals(MathQuizDifficulty.EASY,
                MathQuizDifficulty.fromApiValue("easy"));
        assertEquals(MathQuizDifficulty.HARD,
                MathQuizDifficulty.fromApiValue("Khó"));
        assertEquals(MathQuizDifficulty.MEDIUM,
                MathQuizDifficulty.fromApiValue("unknown"));
        assertEquals(MathQuizDifficulty.MEDIUM,
                MathQuizDifficulty.fromApiValue(null));
    }

    /*
     * Responsibility: Performs the `rewards increase with math question difficulty` responsibility of `MathQuizDifficultyTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void rewardsIncreaseWithMathQuestionDifficulty() {
        assertEquals(5, MathQuizDifficulty.EASY.getExpReward());
        assertEquals(10, MathQuizDifficulty.MEDIUM.getExpReward());
        assertEquals(20, MathQuizDifficulty.HARD.getExpReward());
    }
}
