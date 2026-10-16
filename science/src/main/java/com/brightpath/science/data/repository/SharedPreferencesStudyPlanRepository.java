package com.brightpath.science.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.brightpath.science.data.local.TextCipher;
import com.brightpath.science.domain.model.StudyPlan;
import com.brightpath.science.domain.repository.StudyPlanRepository;

import org.json.JSONObject;

/** Persists the latest encrypted Science study plan independently for each signed-in user. */
public final class SharedPreferencesStudyPlanRepository implements StudyPlanRepository {
    private static final String FILE_NAME = "SCIENCE_STUDY_PLANS";
    private final SharedPreferences preferences;
    private final TextCipher textCipher;

        /*
 * Responsibility: Initializes SharedPreferencesStudyPlanRepository and receives or prepares the dependencies/state required by this data repository.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public SharedPreferencesStudyPlanRepository(Context context, TextCipher textCipher) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        this.textCipher = textCipher;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getLatest` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
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

        /*
 * Responsibility: Persists the data supplied to `save` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
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

        /*
 * Responsibility: Removes or resets the data/state handled by `clear`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
@Override
    public void clear(int userId) {
        preferences.edit().remove(key(userId)).apply();
    }

        /*
 * Responsibility: Performs the repository operation `key` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
private String key(int userId) {
        return "plan_user_" + Math.max(0, userId);
    }
}
