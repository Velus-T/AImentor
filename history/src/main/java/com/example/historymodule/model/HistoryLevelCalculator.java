package com.example.historymodule.model;

/** Shared History Quiz EXP curve. Level is derived from total EXP, never stored separately. */
/*
 * Role: Model class for History, representing data exchanged between the UI, database, and/or AI layer.
 * Purpose: The model provides a clear data structure, avoids passing many separate values, and gives different layers a shared format.
 */
public final class HistoryLevelCalculator {
    public static final int MAX_LEVEL = 20;

    private HistoryLevelCalculator() {
    }

    /*
     * Responsibility: Reads or loads data for `get level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getLevel(int totalExp) {
        int exp = Math.max(0, totalExp);
        for (int level = MAX_LEVEL; level >= 1; level--) {
            if (exp >= getTotalExpForLevel(level)) return level;
        }
        return 1;
    }

    /*
     * Responsibility: Reads or loads data for `get total exp for level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getTotalExpForLevel(int level) {
        int safeLevel = Math.max(1, Math.min(MAX_LEVEL, level));
        int completedLevels = safeLevel - 1;
        return completedLevels * 120 + 25 * completedLevels * (completedLevels - 1) / 2;
    }

    /*
     * Responsibility: Reads or loads data for `get exp to next level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getExpToNextLevel(int level) {
        int safeLevel = Math.max(1, Math.min(MAX_LEVEL, level));
        if (safeLevel >= MAX_LEVEL) return 0;
        return 120 + 25 * (safeLevel - 1);
    }

    /*
     * Responsibility: Reads or loads data for `get exp inside current level` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getExpInsideCurrentLevel(int totalExp) {
        int level = getLevel(totalExp);
        return Math.max(0, totalExp - getTotalExpForLevel(level));
    }

    /*
     * Responsibility: Reads or loads data for `get progress percent` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getProgressPercent(int totalExp) {
        int level = getLevel(totalExp);
        if (level >= MAX_LEVEL) return 100;
        int required = getExpToNextLevel(level);
        return required <= 0 ? 100 : Math.min(100,
                Math.round(getExpInsideCurrentLevel(totalExp) * 100f / required));
    }

    /*
     * Responsibility: Reads or loads data for `get title res id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getTitleResId(int level) {
        int safeLevel = Math.max(1, Math.min(MAX_LEVEL, level));
        if (safeLevel <= 4) return com.example.historymodule.R.string.history_level_1;
        if (safeLevel <= 8) return com.example.historymodule.R.string.history_level_2;
        if (safeLevel <= 12) return com.example.historymodule.R.string.history_level_3;
        if (safeLevel <= 16) return com.example.historymodule.R.string.history_level_4;
        if (safeLevel <= 19) return com.example.historymodule.R.string.history_level_5;
        return com.example.historymodule.R.string.history_level_6;
    }
}
