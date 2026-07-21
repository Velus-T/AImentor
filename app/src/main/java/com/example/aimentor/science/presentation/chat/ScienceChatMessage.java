package com.example.aimentor.science.presentation.chat;

import com.example.aimentor.science.domain.model.ScienceSubject;

import org.json.JSONObject;

/** UI model for the ChatGPT-style science conversation. */
public final class ScienceChatMessage {
    public enum Role { USER, ASSISTANT }

    private final Role role;
    private final String text;
    private final String imageUri;
    private final boolean loading;
    private final boolean error;
    private final boolean fromCache;
    private final long historyId;
    private final ScienceSubject subject;
    private final String sourceQuestion;
    private boolean bookmarked;

    private ScienceChatMessage(Role role,
                               String text,
                               String imageUri,
                               boolean loading,
                               boolean error,
                               boolean fromCache,
                               long historyId,
                               ScienceSubject subject,
                               String sourceQuestion,
                               boolean bookmarked) {
        this.role = role == null ? Role.ASSISTANT : role;
        this.text = text == null ? "" : text;
        this.imageUri = imageUri;
        this.loading = loading;
        this.error = error;
        this.fromCache = fromCache;
        this.historyId = historyId;
        this.subject = subject == null ? ScienceSubject.PHYSICS : subject;
        this.sourceQuestion = sourceQuestion == null ? "" : sourceQuestion;
        this.bookmarked = bookmarked;
    }

    public static ScienceChatMessage user(String text, String imageUri, ScienceSubject subject) {
        return new ScienceChatMessage(Role.USER, text, imageUri, false, false,
                false, 0, subject, text, false);
    }

    public static ScienceChatMessage loading(ScienceSubject subject, String sourceQuestion) {
        return new ScienceChatMessage(Role.ASSISTANT, "Thinking…", null, true, false,
                false, 0, subject, sourceQuestion, false);
    }

    public static ScienceChatMessage assistant(String text,
                                                long historyId,
                                                ScienceSubject subject,
                                                String sourceQuestion,
                                                boolean fromCache,
                                                boolean bookmarked) {
        return new ScienceChatMessage(Role.ASSISTANT, text, null, false, false,
                fromCache, historyId, subject, sourceQuestion, bookmarked);
    }

    public static ScienceChatMessage error(String message,
                                           ScienceSubject subject,
                                           String sourceQuestion) {
        return new ScienceChatMessage(Role.ASSISTANT, message, null, false, true,
                false, 0, subject, sourceQuestion, false);
    }

    /** Loading rows are intentionally not persisted across configuration changes. */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("role", role.name());
            json.put("text", text);
            json.put("imageUri", imageUri == null ? JSONObject.NULL : imageUri);
            json.put("error", error);
            json.put("fromCache", fromCache);
            json.put("historyId", historyId);
            json.put("subject", subject.name());
            json.put("sourceQuestion", sourceQuestion);
            json.put("bookmarked", bookmarked);
        } catch (Exception ignored) {
            // JSONObject backed by in-memory values should not fail here.
        }
        return json;
    }

    public static ScienceChatMessage fromJson(JSONObject json) {
        if (json == null) return null;
        try {
            Role role = Role.valueOf(json.optString("role", Role.ASSISTANT.name()));
            ScienceSubject subject = ScienceSubject.valueOf(
                    json.optString("subject", ScienceSubject.PHYSICS.name()));
            String imageUri = json.isNull("imageUri") ? null : json.optString("imageUri", null);
            return new ScienceChatMessage(
                    role,
                    json.optString("text", ""),
                    imageUri,
                    false,
                    json.optBoolean("error", false),
                    json.optBoolean("fromCache", false),
                    json.optLong("historyId", 0),
                    subject,
                    json.optString("sourceQuestion", ""),
                    json.optBoolean("bookmarked", false)
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    public Role getRole() { return role; }
    public String getText() { return text; }
    public String getImageUri() { return imageUri; }
    public boolean isLoading() { return loading; }
    public boolean isError() { return error; }
    public boolean isFromCache() { return fromCache; }
    public long getHistoryId() { return historyId; }
    public ScienceSubject getSubject() { return subject; }
    public String getSourceQuestion() { return sourceQuestion; }
    public boolean isBookmarked() { return bookmarked; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }
}
