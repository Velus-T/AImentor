package com.example.aimentor.science.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ExplanationStyle;
import com.example.aimentor.science.domain.model.ScienceSubject;

/** Preferences owned only by the Science module. */
public final class SciencePreferenceStore {
    private static final String FILE_NAME = "SCIENCE_LEARNING_PREFERENCES";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_STYLE = "style";
    private static final String KEY_ANIMATION = "animated_background";
    private static final String KEY_HIGH_CONTRAST = "high_contrast_controls";

    private final SharedPreferences preferences;

    public SciencePreferenceStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public ScienceSubject getSubject() {
        int ordinal = preferences.getInt(KEY_SUBJECT, ScienceSubject.PHYSICS.ordinal());
        ScienceSubject[] values = ScienceSubject.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ScienceSubject.PHYSICS;
    }

    public int getLevelPosition() {
        int value = preferences.getInt(KEY_LEVEL, EducationLevel.MIDDLE_SCHOOL.ordinal());
        return Math.max(0, Math.min(value, EducationLevel.values().length - 1));
    }

    public int getStylePosition() {
        int value = preferences.getInt(KEY_STYLE, ExplanationStyle.STEP_BY_STEP.ordinal());
        return Math.max(0, Math.min(value, ExplanationStyle.values().length - 1));
    }

    public boolean isAnimationEnabled() {
        return preferences.getBoolean(KEY_ANIMATION, true);
    }

    public boolean isHighContrastControlsEnabled() {
        return preferences.getBoolean(KEY_HIGH_CONTRAST, true);
    }

    public void save(ScienceSubject subject, int levelPosition, int stylePosition) {
        ScienceSubject safeSubject = subject == null ? ScienceSubject.PHYSICS : subject;
        preferences.edit()
                .putInt(KEY_SUBJECT, safeSubject.ordinal())
                .putInt(KEY_LEVEL, Math.max(0, Math.min(levelPosition,
                        EducationLevel.values().length - 1)))
                .putInt(KEY_STYLE, Math.max(0, Math.min(stylePosition,
                        ExplanationStyle.values().length - 1)))
                .apply();
    }

    public void setAnimationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_ANIMATION, enabled).apply();
    }

    public void setHighContrastControlsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply();
    }
}
