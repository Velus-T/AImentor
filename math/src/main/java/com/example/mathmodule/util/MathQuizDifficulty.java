package com.example.mathmodule.util;

import java.util.Locale;

/** Difficulty and EXP reward for one mathematics quiz question. */
/*
 * Role: Utility class for Math, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public enum MathQuizDifficulty {
    EASY("Dễ", 5),
    MEDIUM("Trung bình", 10),
    HARD("Khó", 20);

    private final String displayName;
    private final int expReward;

    MathQuizDifficulty(String displayName, int expReward) {
        this.displayName = displayName;
        this.expReward = expReward;
    }

    /*
     * Responsibility: Reads or loads data for `get display name` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getDisplayName() {
        return displayName;
    }

    public int getExpReward() {
        return com.example.mentor.data.QuizPolicy.reward(name());
    }

    /*
     * Responsibility: Performs the `from api value` responsibility of `MathQuizDifficulty` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public static MathQuizDifficulty fromApiValue(String value) {
        if (value == null) return MEDIUM;
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace('Á', 'A')
                .replace('À', 'A')
                .replace('Ạ', 'A')
                .replace('Ả', 'A')
                .replace('Ã', 'A')
                .replace('Ă', 'A')
                .replace('Â', 'A')
                .replace('Ó', 'O')
                .replace('Ò', 'O')
                .replace('Ọ', 'O')
                .replace('Ỏ', 'O')
                .replace('Õ', 'O')
                .replace('Ô', 'O')
                .replace('Ơ', 'O')
                .replace('Í', 'I')
                .replace('Ì', 'I')
                .replace('Ị', 'I')
                .replace('Ỉ', 'I')
                .replace('Ĩ', 'I')
                .replace('Ú', 'U')
                .replace('Ù', 'U')
                .replace('Ụ', 'U')
                .replace('Ủ', 'U')
                .replace('Ũ', 'U')
                .replace('Ư', 'U')
                .replace('É', 'E')
                .replace('È', 'E')
                .replace('Ẹ', 'E')
                .replace('Ẻ', 'E')
                .replace('Ẽ', 'E')
                .replace('Ê', 'E')
                .replace('Đ', 'D');

        if (normalized.contains("EASY") || normalized.contains("DE")) return EASY;
        if (normalized.contains("HARD") || normalized.contains("KHO")) return HARD;
        return MEDIUM;
    }
}
