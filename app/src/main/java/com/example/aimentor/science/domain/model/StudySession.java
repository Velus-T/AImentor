package com.example.aimentor.science.domain.model;

import org.json.JSONObject;

public final class StudySession {
    private final int dayNumber;
    private final long startAtMillis;
    private final int durationMinutes;
    private final String title;
    private final String activities;
    private boolean completed;

    public StudySession(int dayNumber,
                        long startAtMillis,
                        int durationMinutes,
                        String title,
                        String activities,
                        boolean completed) {
        this.dayNumber = Math.max(1, dayNumber);
        this.startAtMillis = Math.max(0L, startAtMillis);
        this.durationMinutes = Math.max(10, Math.min(durationMinutes, 240));
        this.title = title == null || title.trim().isEmpty() ? "Science study session" : title.trim();
        this.activities = activities == null ? "Review, practise and check understanding." : activities.trim();
        this.completed = completed;
    }

    public int getDayNumber() { return dayNumber; }
    public long getStartAtMillis() { return startAtMillis; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getTitle() { return title; }
    public String getActivities() { return activities; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("dayNumber", dayNumber);
            json.put("startAtMillis", startAtMillis);
            json.put("durationMinutes", durationMinutes);
            json.put("title", title);
            json.put("activities", activities);
            json.put("completed", completed);
        } catch (Exception ignored) { }
        return json;
    }

    public static StudySession fromJson(JSONObject json) {
        if (json == null) return null;
        return new StudySession(
                json.optInt("dayNumber", 1),
                json.optLong("startAtMillis", 0L),
                json.optInt("durationMinutes", 30),
                json.optString("title", "Science study session"),
                json.optString("activities", "Review and practise."),
                json.optBoolean("completed", false)
        );
    }
}
