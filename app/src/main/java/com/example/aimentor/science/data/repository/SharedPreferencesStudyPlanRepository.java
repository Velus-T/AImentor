package com.example.aimentor.science.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aimentor.science.data.local.TextCipher;
import com.example.aimentor.science.domain.model.StudyPlan;
import com.example.aimentor.science.domain.repository.StudyPlanRepository;

import org.json.JSONObject;

/** Persists the latest encrypted Science study plan independently for each signed-in user. */
public final class SharedPreferencesStudyPlanRepository implements StudyPlanRepository {
    private static final String FILE_NAME = "SCIENCE_STUDY_PLANS";
    private final SharedPreferences preferences;
    private final TextCipher textCipher;

    public SharedPreferencesStudyPlanRepository(Context context, TextCipher textCipher) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        this.textCipher = textCipher;
    }

    @Override
    public StudyPlan getLatest(int userId) {
        String stored = preferences.getString(key(userId), "");
        if (stored == null || stored.isEmpty()) return null;
        try {
            String raw = textCipher.decrypt(stored);
            if (raw.startsWith("[Encrypted content")) return null;
            return StudyPlan.fromJson(new JSONObject(raw));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void save(int userId, StudyPlan plan) {
        if (plan == null) return;
        try {
            String encrypted = textCipher.encrypt(plan.toJson().toString());
            preferences.edit().putString(key(userId), encrypted).apply();
        } catch (Exception ignored) {
            // A storage failure should not crash the active study session.
        }
    }

    @Override
    public void clear(int userId) {
        preferences.edit().remove(key(userId)).apply();
    }

    private String key(int userId) {
        return "plan_user_" + Math.max(0, userId);
    }
}
