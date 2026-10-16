package com.example.asm_final.utils;

import com.example.asm_final.R;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public class ExpLevelCalculator {
    public static final int EXP_PER_LEVEL = 200;

    public static int getLevel(int totalExp) {
        return (totalExp / EXP_PER_LEVEL) + 1;
    }

    /*
     * Responsibility: Reads or loads data for `get progress percentage` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getProgressPercentage(int totalExp) {
        return (int) (((double) (totalExp % EXP_PER_LEVEL) / EXP_PER_LEVEL) * 100);
    }

    public static int getLevelTitleResId(int level) {
        if (level < 5) return R.string.home_level_1;
        if (level < 10) return R.string.home_level_2;
        if (level < 20) return R.string.home_level_3;
        if (level < 50) return R.string.home_level_4;
        return R.string.home_level_5;
    }

    /*
     * Responsibility: Reads or loads data for `get exp to next level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getExpToNextLevel(int totalExp) {
        return EXP_PER_LEVEL - (totalExp % EXP_PER_LEVEL);
    }
}
