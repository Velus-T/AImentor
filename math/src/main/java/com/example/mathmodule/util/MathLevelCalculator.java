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
    public static int getLevel(int totalExp) {
        int exp = Math.max(0, totalExp); int level = 1;
        while (level < MAX_LEVEL && exp >= getTotalExpForLevel(level + 1)) level++;
        return level;
    }
    /*
     * Responsibility: Reads or loads data for `get total exp for level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getTotalExpForLevel(int level) {
        int safe = Math.max(1, Math.min(level, MAX_LEVEL)); int n = safe - 1;
        return BASE * n + STEP * n * (n - 1) / 2;
    }
    public static int getExpToNextLevel(int level) {
        if (level >= MAX_LEVEL) return 0; return BASE + STEP * (Math.max(1, level) - 1);
    }
    /*
     * Responsibility: Reads or loads data for `get inside level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getInsideLevel(int exp) { int l=getLevel(exp); return Math.max(0, exp-getTotalExpForLevel(l)); }
    public static int getRemaining(int exp) { int l=getLevel(exp); return l>=MAX_LEVEL?0:Math.max(0,getExpToNextLevel(l)-getInsideLevel(exp)); }
    public static int getTitleResId(int level) {
        if(level<=4)return com.example.mathmodule.R.string.math_level_1;
        if(level<=8)return com.example.mathmodule.R.string.math_level_2;
        if(level<=12)return com.example.mathmodule.R.string.math_level_3;
        if(level<=16)return com.example.mathmodule.R.string.math_level_4;
        if(level<=19)return com.example.mathmodule.R.string.math_level_5;
        return com.example.mathmodule.R.string.math_level_6;
    }
}
