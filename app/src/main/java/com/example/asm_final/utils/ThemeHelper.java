package com.example.asm_final.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public final class ThemeHelper {
    private static final String SETTINGS_PREFERENCES = "settings";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private ThemeHelper() {
        // Utility class.
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `is dark mode` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    public static boolean isDarkMode(Context context) {
        return preferences(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean enabled) {
        preferences(context)
                .edit()
                .putBoolean(KEY_DARK_MODE, enabled)
                .apply();

        AppCompatDelegate.setDefaultNightMode(enabled
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /*
     * Responsibility: Applies or updates `apply saved theme` in the current state or UI.
     * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
     */
    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(isDarkMode(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /*
     * Responsibility: Performs the `preferences` responsibility of `ThemeHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }
}
