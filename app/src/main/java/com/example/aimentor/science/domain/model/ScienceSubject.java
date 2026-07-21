package com.example.aimentor.science.domain.model;

public enum ScienceSubject {
    PHYSICS("PHYSICS", "Physics"),
    CHEMISTRY("CHEMISTRY", "Chemistry"),
    BIOLOGY("BIOLOGY", "Biology");

    private final String code;
    private final String displayName;

    ScienceSubject(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }

    public static ScienceSubject fromCode(String value) {
        if (value == null) return PHYSICS;
        for (ScienceSubject subject : values()) {
            if (subject.code.equalsIgnoreCase(value) || subject.displayName.equalsIgnoreCase(value)) {
                return subject;
            }
        }
        return PHYSICS;
    }
}
