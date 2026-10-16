package com.example.mathmodule.util;

/** Supported learner levels when generating mathematics quizzes. */
/*
 * Role: Utility class for Math, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public enum MathEducationLevel {
    MIDDLE_SCHOOL("Middle School", "học sinh THCS, lớp 6 đến lớp 9"),
    HIGH_SCHOOL("High School", "học sinh THPT, lớp 10 đến lớp 12"),
    UNIVERSITY("University", "sinh viên đại học");

    private final String displayName;
    private final String promptDescription;

    MathEducationLevel(String displayName, String promptDescription) {
        this.displayName = displayName;
        this.promptDescription = promptDescription;
    }

    /*
     * Responsibility: Reads or loads data for `get display name` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getDisplayName() {
        return displayName;
    }

    public String getPromptDescription() {
        return promptDescription;
    }

    /*
     * Responsibility: Performs the `from display name` responsibility of `MathEducationLevel` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public static MathEducationLevel fromDisplayName(String value) {
        if (value != null) {
            for (MathEducationLevel level : values()) {
                if (level.displayName.equalsIgnoreCase(value.trim())) return level;
            }
        }
        return MIDDLE_SCHOOL;
    }
}
