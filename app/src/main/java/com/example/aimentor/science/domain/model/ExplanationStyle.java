package com.example.aimentor.science.domain.model;

public enum ExplanationStyle {
    SHORT("Short"),
    DETAILED("Detailed"),
    STEP_BY_STEP("Step-by-step");

    private final String displayName;

    ExplanationStyle(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static ExplanationStyle fromPosition(int position) {
        ExplanationStyle[] values = values();
        return position >= 0 && position < values.length ? values[position] : STEP_BY_STEP;
    }
}
