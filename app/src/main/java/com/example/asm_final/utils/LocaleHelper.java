package com.example.asm_final.utils;

import android.app.LocaleManager;
import android.os.Build;
import android.os.LocaleList;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public final class LocaleHelper {
    public static final String LANGUAGE_VIETNAMESE = "vi";
    public static final String LANGUAGE_ENGLISH = "en";

    private static final String SETTINGS_PREFERENCES = "settings";
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    /*
     * Responsibility: Initializes the `LocaleHelper` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    private LocaleHelper() {
        // Utility class.
    }

    public static void setLocale(Context context, String language) {
        String supportedLanguage = normalizeLanguage(language);
        preferences(context)
                .edit()
                .putString(SELECTED_LANGUAGE, supportedLanguage)
                .apply();

        applyLanguage(context, supportedLanguage);
    }

    /*
     * Responsibility: Applies or updates `apply saved locale` in the current state or UI.
     * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
     */
    public static void applySavedLocale(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager != null && !manager.getApplicationLocales().isEmpty()) {
                String language = normalizeLanguage(manager.getApplicationLocales().get(0).getLanguage());
                preferences(context).edit().putString(SELECTED_LANGUAGE, language).apply();
                return;
            }
        }
        String language = getLanguage(context);
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        String currentLanguage = currentLocales.isEmpty()
                ? ""
                : currentLocales.get(0).getLanguage();

        if (!language.equals(currentLanguage)) {
            applyLanguage(context, language);
        }
    }

    /*
     * Responsibility: Reads or loads data for `get language` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public static String getLanguage(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager != null && !manager.getApplicationLocales().isEmpty()) {
                return normalizeLanguage(manager.getApplicationLocales().get(0).getLanguage());
            }
        }
        String savedLanguage = preferences(context)
                .getString(SELECTED_LANGUAGE, LANGUAGE_VIETNAMESE);
        return normalizeLanguage(savedLanguage);
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `is english` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    public static boolean isEnglish(Context context) {
        return LANGUAGE_ENGLISH.equals(getLanguage(context));
    }

    private static void applyLanguage(Context context, String language) {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager != null) {
                manager.setApplicationLocales(LocaleList.forLanguageTags(language));
                return;
            }
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language));
    }

    private static String normalizeLanguage(String language) {
        return LANGUAGE_ENGLISH.equalsIgnoreCase(language)
                ? LANGUAGE_ENGLISH
                : LANGUAGE_VIETNAMESE;
    }

    /*
     * Responsibility: Performs the `preferences` responsibility of `LocaleHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }
}
