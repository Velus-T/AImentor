package com.brightpath.science.util;

import com.brightpath.science.domain.model.ScienceQuestion;

public final class ScienceQuestionValidator {
    private static final int MAX_QUESTION_LENGTH = 2000;

        /*
 * Responsibility: Checks whether a ScienceQuestion has acceptable text/image input and satisfies the configured request limits.
 * Purpose: Blocks invalid requests before an AI call or history save can occur.
 */
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

                /*
 * Responsibility: Initializes ValidationResult and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

                /*
 * Responsibility: Performs the `valid` operation for ValidationResult within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public static ValidationResult valid() { return new ValidationResult(true, ""); }
                /*
 * Responsibility: Performs the `invalid` operation for ValidationResult within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public static ValidationResult invalid(String message) { return new ValidationResult(false, message); }
                /*
 * Responsibility: Checks the condition represented by `isValid` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isValid() { return valid; }
                /*
 * Responsibility: Retrieves the data/state required by `getMessage` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getMessage() { return message; }
    }
}
