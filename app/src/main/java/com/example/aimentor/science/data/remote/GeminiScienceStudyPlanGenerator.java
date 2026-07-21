package com.example.aimentor.science.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.example.aimentor.science.domain.model.StudyPlan;
import com.example.aimentor.science.domain.model.StudyPlanRequest;
import com.example.aimentor.science.domain.model.StudySession;
import com.example.aimentor.science.domain.service.GenerativeAiClient;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceApiSettings;
import com.example.aimentor.science.domain.service.ScienceStudyPlanGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Gemini-backed content generator; schedule times are validated locally. */
public final class GeminiScienceStudyPlanGenerator implements ScienceStudyPlanGenerator {
    private final ScienceApiSettings apiSettings;
    private final GenerativeAiClient aiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public GeminiScienceStudyPlanGenerator(ScienceApiSettings apiSettings,
                                            GenerativeAiClient aiClient) {
        this.apiSettings = apiSettings;
        this.aiClient = aiClient;
    }

    @Override
    public void generate(StudyPlanRequest request, ResultCallback<StudyPlan> callback) {
        executor.execute(() -> {
            try {
                if (!apiSettings.isConfigured()) {
                    throw new IllegalStateException("Science API key is not configured.");
                }
                String response = aiClient.generateText(
                        apiSettings.getApiKey(), apiSettings.getModelName(),
                        buildPrompt(request), null, null, 0.9, 3000);
                StudyPlan plan = parsePlan(response, request);
                mainHandler.post(() -> callback.onSuccess(plan));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(
                        "AI planning is unavailable; a local plan will be created.", error));
            }
        });
    }

    private String buildPrompt(StudyPlanRequest request) {
        String start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
                .format(new Date(request.getCreatedAtMillis()));
        return "You are an adaptive " + request.getSubject().getDisplayName()
                + " study planner for an AI Study Mentor app.\n"
                + "Education level: " + request.getLevel().getDisplayName() + "\n"
                + "Goal: " + request.getGoal() + "\n"
                + "Plan length: " + request.getNumberOfDays() + " day(s)\n"
                + "Maximum study time per day: " + request.getMinutesPerDay() + " minutes\n"
                + "The user started creating this plan at: " + start + "\n"
                + "Create exactly one practical session for each day. The app will calculate clock times "
                + "from the creation moment, so do not output dates or clock times.\n"
                + "Return only a valid JSON array with exactly " + request.getNumberOfDays() + " objects.\n"
                + "Each object must contain: dayNumber, durationMinutes, title, activities.\n"
                + "activities must be a concise plain-text sequence with review, learning, practice and a quick check.\n"
                + "Keep every topic inside the selected education level and selected science subject.\n"
                + "Do not include markdown fences.";
    }


    private StudyPlan parsePlan(String raw, StudyPlanRequest request) throws Exception {
        String cleaned = raw.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('[');
        int end = cleaned.lastIndexOf(']');
        if (start < 0 || end <= start) throw new IllegalArgumentException("Plan JSON was not found.");
        JSONArray array = new JSONArray(cleaned.substring(start, end + 1));
        if (array.length() < request.getNumberOfDays()) {
            throw new IllegalArgumentException("The AI returned too few study days.");
        }

        Calendar first = Calendar.getInstance();
        first.setTimeInMillis(request.getCreatedAtMillis());
        List<StudySession> sessions = new ArrayList<>();
        for (int i = 0; i < request.getNumberOfDays(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) throw new IllegalArgumentException("A study day was invalid.");
            Calendar startTime = (Calendar) first.clone();
            startTime.add(Calendar.DAY_OF_YEAR, i);
            int duration = Math.max(20, Math.min(
                    item.optInt("durationMinutes", request.getMinutesPerDay()),
                    request.getMinutesPerDay()));
            sessions.add(new StudySession(
                    i + 1,
                    startTime.getTimeInMillis(),
                    duration,
                    item.optString("title", request.getSubject().getDisplayName() + " study"),
                    item.optString("activities", "Review, learn, practise and complete a quick check."),
                    false
            ));
        }
        return new StudyPlan(
                request.getCreatedAtMillis(),
                request.getSubject(),
                request.getLevel(),
                request.getGoal(),
                true,
                sessions
        );
    }
}
