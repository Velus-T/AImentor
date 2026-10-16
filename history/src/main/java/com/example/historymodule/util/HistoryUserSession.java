package com.example.historymodule.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the host application's active user id for History-owned data. */
/*
 * Role: Utility class for History, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public final class HistoryUserSession {
    private static final String FILE_NAME = "HISTORY_HOST_SESSION";
    private static final String KEY_USER_ID = "history_user_id";

    private HistoryUserSession() {
    }

    /*
     * Responsibility: Applies or updates `set user id` in the current state or UI.
     * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
     */
    public static void setUserId(Context context, int userId) {
        if (context == null) return;
        preferences(context).edit().putInt(KEY_USER_ID, Math.max(0, userId)).apply();
    }

    /*
     * Responsibility: Reads or loads data for `get user id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static int getUserId(Context context) {
        if (context == null) return 0;
        return Math.max(0, preferences(context).getInt(KEY_USER_ID, 0));
    }

    /*
     * Responsibility: Performs the `preferences` responsibility of `HistoryUserSession` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }
}
