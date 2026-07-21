package com.example.aimentor.science.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.QuizVariationRepository;

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

    public SharedPreferencesQuizVariationRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

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

    private String key(int userId, ScienceSubject subject, EducationLevel level) {
        return "recent_" + Math.max(0, userId) + "_" + subject.name() + "_" + level.name();
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace(SEPARATOR, " ")
                .replace('\n', ' ').replace('\r', ' ').trim();
    }
}
