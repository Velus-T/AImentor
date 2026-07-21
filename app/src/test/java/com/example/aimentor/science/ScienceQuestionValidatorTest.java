package com.example.aimentor.science;

import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ExplanationStyle;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.util.ScienceQuestionValidator;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScienceQuestionValidatorTest {
    @Test
    public void acceptsTextOrImageAndRejectsEmptyRequest() {
        ScienceQuestionValidator validator = new ScienceQuestionValidator();
        assertFalse(validator.validate(question("", null)).isValid());
        assertTrue(validator.validate(question("Why is the sky blue?", null)).isValid());
        assertTrue(validator.validate(question("", new byte[]{1, 2, 3})).isValid());
    }

    private ScienceQuestion question(String text, byte[] image) {
        return new ScienceQuestion(1, ScienceSubject.PHYSICS,
                EducationLevel.MIDDLE_SCHOOL, ExplanationStyle.SHORT,
                text, null, image, image == null ? null : "image/jpeg");
    }
}
