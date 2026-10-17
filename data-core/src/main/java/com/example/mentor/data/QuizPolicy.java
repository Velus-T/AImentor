package com.example.mentor.data;

/** The single reward and level curve used by every subject. Existing EXP is never rewritten. */
public final class QuizPolicy {
    public static final int MAX_LEVEL = 20;
    private QuizPolicy() {}
    public static int[] questionCounts() { return new int[]{5,10,15}; }
    public static int reward(String difficulty) {
        if ("EASY".equalsIgnoreCase(difficulty)) return 5;
        if ("HARD".equalsIgnoreCase(difficulty)) return 20;
        return 10;
    }
    public static int threshold(int level) {
        int n = Math.max(1, Math.min(MAX_LEVEL, level)) - 1;
        return 120 * n + 25 * n * (n - 1) / 2;
    }
    public static int level(int exp) {
        int level = 1;
        while (level < MAX_LEVEL && Math.max(0, exp) >= threshold(level + 1)) level++;
        return level;
    }
    public static int required(int level) {
        return level >= MAX_LEVEL ? 0 : 120 + 25 * (Math.max(1, level) - 1);
    }
    public static int inside(int exp) { return Math.max(0, exp) - threshold(level(exp)); }
    public static int remaining(int exp) {
        return level(exp) == MAX_LEVEL ? 0 : Math.max(0, required(level(exp)) - inside(exp));
    }
    public static int progress(int exp) {
        return level(exp) == MAX_LEVEL ? 100 : Math.min(100, Math.round(inside(exp) * 100f / required(level(exp))));
    }
}
