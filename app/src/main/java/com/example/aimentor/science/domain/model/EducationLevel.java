package com.example.aimentor.science.domain.model;

public enum EducationLevel {
    MIDDLE_SCHOOL("Middle school"),
    HIGH_SCHOOL("High school"),
    UNIVERSITY("University");

    private final String displayName;

    EducationLevel(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static EducationLevel fromPosition(int position) {
        EducationLevel[] values = values();
        return position >= 0 && position < values.length ? values[position] : MIDDLE_SCHOOL;
    }
}
