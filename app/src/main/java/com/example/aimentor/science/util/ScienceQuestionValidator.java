package com.example.aimentor.science.util;

import com.example.aimentor.science.domain.model.ScienceQuestion;

public final class ScienceQuestionValidator {
    private static final int MAX_QUESTION_LENGTH = 2000;

    public ValidationResult validate(ScienceQuestion question) {
        if (question == null) return ValidationResult.invalid("Question is missing.");
        if (question.getQuestionText().isEmpty() && !question.hasImage()) {
            return ValidationResult.invalid("Enter a question or attach an image.");
        }
        if (question.getQuestionText().length() > MAX_QUESTION_LENGTH) {
            return ValidationResult.invalid("Question must be under 2,000 characters.");
        }
        if (question.hasImage() && question.getImageBytes().length > 8 * 1024 * 1024) {
            return ValidationResult.invalid("Image is too large. Please choose an image under 8 MB.");
        }
        return ValidationResult.valid();
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() { return new ValidationResult(true, ""); }
        public static ValidationResult invalid(String message) { return new ValidationResult(false, message); }
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
