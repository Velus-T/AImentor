package com.brightpath.science.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.QuizVariationRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Stores only recent quiz prompts so the next AI request can avoid repetition. */
public final class SharedPreferencesQuizVariationRepository implements QuizVariationRepository {
    private static final String PREFS = "science_quiz_variations";
    private static final String SEPARATOR = "\u001E";
    private static final int MAX_STORED = 20;

    private final SharedPreferences preferences;

        /*
 * Responsibility: Initializes SharedPreferencesQuizVariationRepository and receives or prepares the dependencies/state required by this data repository.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public SharedPreferencesQuizVariationRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getRecentPrompts` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public List<String> getRecentPrompts(int userId, ScienceSubject subject,
                                         EducationLevel level, int limit) {
        String stored = preferences.getString(key(userId, subject, level), "");
        List<String> result = new ArrayList<>();
        if (stored == null || stored.isEmpty()) return result;
        String[] values = stored.split(SEPARATOR, -1);
        int safeLimit = Math.max(0, limit);
        for (String value : values) {
            String prompt = value.trim();
            if (!prompt.isEmpty()) result.add(prompt);
            if (result.size() >= safeLimit) break;
        }
        return result;
    }

        /*
 * Responsibility: Performs the repository operation `rememberPrompts` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
@Override
    public void rememberPrompts(int userId, ScienceSubject subject,
                                EducationLevel level, List<String> prompts) {
        Set<String> merged = new LinkedHashSet<>();
        if (prompts != null) {
            for (String prompt : prompts) {
                String sanitized = sanitize(prompt);
                if (!sanitized.isEmpty()) merged.add(sanitized);
            }
        }
        merged.addAll(getRecentPrompts(userId, subject, level, MAX_STORED));

        StringBuilder encoded = new StringBuilder();
        int count = 0;
        for (String prompt : merged) {
            if (count++ >= MAX_STORED) break;
            if (encoded.length() > 0) encoded.append(SEPARATOR);
            encoded.append(prompt);
        }
        preferences.edit().putString(key(userId, subject, level), encoded.toString()).apply();
    }

        /*
 * Responsibility: Performs the repository operation `key` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
private String key(int userId, ScienceSubject subject, EducationLevel level) {
        return "recent_" + Math.max(0, userId) + "_" + subject.name() + "_" + level.name();
    }

        /*
 * Responsibility: Performs the repository operation `sanitize` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
private String sanitize(String value) {
        return value == null ? "" : value.replace(SEPARATOR, " ")
                .replace('\n', ' ').replace('\r', ' ').trim();
    }
}
