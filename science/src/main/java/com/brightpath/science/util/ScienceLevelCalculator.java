package com.brightpath.science.util;

/**
 * Converts the total Science quiz EXP into a module level.
 *
 * The level is derived from total EXP instead of being stored separately,
 * so the displayed level can never become inconsistent with quiz history.
 */
public final class ScienceLevelCalculator {
    public static final int MAX_LEVEL = 20;

    private static final int BASE_EXP_TO_NEXT_LEVEL = 120;
    private static final int EXP_STEP_PER_LEVEL = 25;

        /*
 * Responsibility: Initializes ScienceLevelCalculator and receives or prepares the dependencies/state required by this learning/business-rule component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private ScienceLevelCalculator() {
    }

        /*
 * Responsibility: Retrieves the data/state required by `getLevel` from the appropriate learning/business-rule component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static int getLevel(int totalExp) {
        int safeExp = Math.max(0, totalExp);
        int level = 1;

        while (level < MAX_LEVEL
                && safeExp >= getTotalExpForLevel(level + 1)) {
            level++;
        }

        return level;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTotalExpForLevel` from the appropriate learning/business-rule component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static int getTotalExpForLevel(int level) {
        int safeLevel = Math.max(1, Math.min(level, MAX_LEVEL));
        int completedLevels = safeLevel - 1;

        return BASE_EXP_TO_NEXT_LEVEL * completedLevels
                + EXP_STEP_PER_LEVEL * completedLevels * (completedLevels - 1) / 2;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getExpToNextLevel` from the appropriate learning/business-rule component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static int getExpToNextLevel(int currentLevel) {
        int safeLevel = Math.max(1, Math.min(currentLevel, MAX_LEVEL));
        if (safeLevel >= MAX_LEVEL) {
            return 0;
        }

        return BASE_EXP_TO_NEXT_LEVEL
                + EXP_STEP_PER_LEVEL * (safeLevel - 1);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getExpInsideCurrentLevel` from the appropriate learning/business-rule component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static int getExpInsideCurrentLevel(int totalExp) {
        int safeExp = Math.max(0, totalExp);
        int level = getLevel(safeExp);
        return Math.max(0, safeExp - getTotalExpForLevel(level));
    }

        /*
 * Responsibility: Retrieves the data/state required by `getRemainingExp` from the appropriate learning/business-rule component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static int getRemainingExp(int totalExp) {
        int level = getLevel(totalExp);
        if (level >= MAX_LEVEL) {
            return 0;
        }

        return Math.max(0,
                getExpToNextLevel(level) - getExpInsideCurrentLevel(totalExp));
    }

        /*
 * Responsibility: Retrieves the data/state required by `getProgressPercent` from the appropriate learning/business-rule component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static int getProgressPercent(int totalExp) {
        int level = getLevel(totalExp);
        if (level >= MAX_LEVEL) {
            return 100;
        }

        int required = getExpToNextLevel(level);
        if (required <= 0) {
            return 100;
        }

        return Math.min(100, Math.round(
                getExpInsideCurrentLevel(totalExp) * 100f / required));
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTitleResId` from the appropriate learning/business-rule component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static int getTitleResId(int level) {
        int safeLevel = Math.max(1, Math.min(level, MAX_LEVEL));

        if (safeLevel <= 4) {
            return com.brightpath.science.R.string.science_level_1;
        }
        if (safeLevel <= 8) {
            return com.brightpath.science.R.string.science_level_2;
        }
        if (safeLevel <= 12) {
            return com.brightpath.science.R.string.science_level_3;
        }
        if (safeLevel <= 16) {
            return com.brightpath.science.R.string.science_level_4;
        }
        if (safeLevel <= 19) {
            return com.brightpath.science.R.string.science_level_5;
        }
        return com.brightpath.science.R.string.science_level_6;
    }
}
