package com.example.historymodule.model;

/** Shared History Quiz EXP curve. Level is derived from total EXP, never stored separately. */
/*
 * Role: Model class for History, representing data exchanged between the UI, database, and/or AI layer.
 * Purpose: The model provides a clear data structure, avoids passing many separate values, and gives different layers a shared format.
 */
public final class HistoryLevelCalculator {
  public static final int MAX_LEVEL = com.example.mentor.data.QuizPolicy.MAX_LEVEL;

  private HistoryLevelCalculator() {}

  /*
   * Responsibility: Reads or loads data for `get level` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public static int getLevel(int totalExp) {
    return com.example.mentor.data.QuizPolicy.level(totalExp);
  }

  /*
   * Responsibility: Reads or loads data for `get total exp for level` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public static int getTotalExpForLevel(int level) {
    return com.example.mentor.data.QuizPolicy.threshold(level);
  }

  /*
   * Responsibility: Reads or loads data for `get exp to next level` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public static int getExpToNextLevel(int level) {
    return com.example.mentor.data.QuizPolicy.required(level);
  }

  /*
   * Responsibility: Reads or loads data for `get exp inside current level` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public static int getExpInsideCurrentLevel(int totalExp) {
    return com.example.mentor.data.QuizPolicy.inside(totalExp);
  }

  /*
   * Responsibility: Reads or loads data for `get progress percent` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public static int getProgressPercent(int totalExp) {
    return com.example.mentor.data.QuizPolicy.progress(totalExp);
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
