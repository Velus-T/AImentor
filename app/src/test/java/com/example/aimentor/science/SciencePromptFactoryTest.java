package com.example.aimentor.science;

import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ExplanationStyle;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.util.DefaultSciencePromptFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SciencePromptFactoryTest {
    @Test
    public void eachTutorUsesDifferentSubjectRulesAndContext() {
        DefaultSciencePromptFactory factory = new DefaultSciencePromptFactory();
        ScienceQuestion physics = question(ScienceSubject.PHYSICS);
        ScienceQuestion chemistry = question(ScienceSubject.CHEMISTRY);
        ScienceQuestion biology = question(ScienceSubject.BIOLOGY);

        String physicsPrompt = factory.buildAnswerPrompt(physics);
        String chemistryPrompt = factory.buildAnswerPrompt(chemistry);
        String biologyPrompt = factory.buildAnswerPrompt(biology);

        assertTrue(physicsPrompt.contains("Physics AI Tutor"));
        assertTrue(physicsPrompt.contains("SI units"));
        assertTrue(chemistryPrompt.contains("Chemistry AI Tutor"));
        assertTrue(chemistryPrompt.contains("Balance reaction equations"));
        assertTrue(biologyPrompt.contains("Biology AI Tutor"));
        assertTrue(biologyPrompt.contains("structure to function"));
        assertTrue(physicsPrompt.contains("Previous answer"));
        assertFalse(physicsPrompt.equals(chemistryPrompt));
        assertFalse(chemistryPrompt.equals(biologyPrompt));
    }

    private ScienceQuestion question(ScienceSubject subject) {
        return new ScienceQuestion(
                1,
                subject,
                EducationLevel.HIGH_SCHOOL,
                ExplanationStyle.STEP_BY_STEP,
                "Explain this concept",
                "Student: Previous question\nTutor: Previous answer",
                null,
                null,
                null
        );
    }
}
