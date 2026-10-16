package com.brightpath.science.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.ExplanationStyle;
import com.brightpath.science.domain.model.ScienceSubject;

/** Preferences owned only by the Science module. */
public final class SciencePreferenceStore {
    private static final String FILE_NAME = "SCIENCE_LEARNING_PREFERENCES";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_STYLE = "style";
    private static final String KEY_ANIMATION = "animated_background";
    private static final String KEY_HIGH_CONTRAST = "high_contrast_controls";

    private final SharedPreferences preferences;

        /*
 * Responsibility: Initializes SciencePreferenceStore and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public SciencePreferenceStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getSubject() {
        int ordinal = preferences.getInt(KEY_SUBJECT, ScienceSubject.PHYSICS.ordinal());
        ScienceSubject[] values = ScienceSubject.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ScienceSubject.PHYSICS;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getLevelPosition` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getLevelPosition() {
        int value = preferences.getInt(KEY_LEVEL, EducationLevel.MIDDLE_SCHOOL.ordinal());
        return Math.max(0, Math.min(value, EducationLevel.values().length - 1));
    }

        /*
 * Responsibility: Retrieves the data/state required by `getStylePosition` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getStylePosition() {
        int value = preferences.getInt(KEY_STYLE, ExplanationStyle.STEP_BY_STEP.ordinal());
        return Math.max(0, Math.min(value, ExplanationStyle.values().length - 1));
    }

        /*
 * Responsibility: Checks the condition represented by `isAnimationEnabled` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isAnimationEnabled() {
        return preferences.getBoolean(KEY_ANIMATION, true);
    }

        /*
 * Responsibility: Checks the condition represented by `isHighContrastControlsEnabled` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isHighContrastControlsEnabled() {
        return preferences.getBoolean(KEY_HIGH_CONTRAST, true);
    }

        /*
 * Responsibility: Persists the data supplied to `save` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
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

        /*
 * Responsibility: Updates the state or value handled by `setAnimationEnabled` for SciencePreferenceStore.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public void setAnimationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_ANIMATION, enabled).apply();
    }

        /*
 * Responsibility: Updates the state or value handled by `setHighContrastControlsEnabled` for SciencePreferenceStore.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public void setHighContrastControlsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply();
    }
}
