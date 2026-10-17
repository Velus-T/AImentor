package com.example.mathmodule.util;

/*
 * Role: Utility class for Math, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public final class MathLevelCalculator {
    public static final int MAX_LEVEL = 20;
    private static final int BASE = 120;
    private static final int STEP = 25;
    private MathLevelCalculator() {}
    /*
     * Responsibility: Reads or loads data for `get level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getLevel(int totalExp) { return com.example.mentor.data.QuizPolicy.level(totalExp); }
    /*
     * Responsibility: Reads or loads data for `get total exp for level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getTotalExpForLevel(int level) { return com.example.mentor.data.QuizPolicy.threshold(level); }
    public static int getExpToNextLevel(int level) { return com.example.mentor.data.QuizPolicy.required(level); }
    /*
     * Responsibility: Reads or loads data for `get inside level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getInsideLevel(int exp) { return com.example.mentor.data.QuizPolicy.inside(exp); }
    public static int getRemaining(int exp) { return com.example.mentor.data.QuizPolicy.remaining(exp); }
    public static int getTitleResId(int level) {
        if(level<=4)return com.example.mathmodule.R.string.math_level_1;
        if(level<=8)return com.example.mathmodule.R.string.math_level_2;
        if(level<=12)return com.example.mathmodule.R.string.math_level_3;
        if(level<=16)return com.example.mathmodule.R.string.math_level_4;
        if(level<=19)return com.example.mathmodule.R.string.math_level_5;
        return com.example.mathmodule.R.string.math_level_6;
    }
}
