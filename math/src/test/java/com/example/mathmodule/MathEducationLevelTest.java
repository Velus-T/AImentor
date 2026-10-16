package com.example.mathmodule;

import com.example.mathmodule.util.MathEducationLevel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * Role: Test class for Math, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class MathEducationLevelTest {
    @Test
    public void resolvesDisplayNamesIgnoringCaseAndWhitespace() {
        assertEquals(MathEducationLevel.HIGH_SCHOOL,
                MathEducationLevel.fromDisplayName("  high school "));
        assertEquals(MathEducationLevel.UNIVERSITY,
                MathEducationLevel.fromDisplayName("University"));
    }

    /*
     * Responsibility: Performs the `unknown or missing level falls back to middle school` responsibility of `MathEducationLevelTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void unknownOrMissingLevelFallsBackToMiddleSchool() {
        assertEquals(MathEducationLevel.MIDDLE_SCHOOL,
                MathEducationLevel.fromDisplayName(null));
        assertEquals(MathEducationLevel.MIDDLE_SCHOOL,
                MathEducationLevel.fromDisplayName("Primary School"));
    }

    /*
     * Responsibility: Performs the `each level provides math learning context for ai prompt` responsibility of `MathEducationLevelTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void eachLevelProvidesMathLearningContextForAiPrompt() {
        assertTrue(MathEducationLevel.MIDDLE_SCHOOL.getPromptDescription().contains("lớp 6"));
        assertTrue(MathEducationLevel.HIGH_SCHOOL.getPromptDescription().contains("lớp 10"));
        assertTrue(MathEducationLevel.UNIVERSITY.getPromptDescription().contains("đại học"));
    }
}
