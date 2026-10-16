package com.brightpath.science.presentation.chat;

import com.brightpath.science.domain.model.ScienceSubject;

import org.json.JSONObject;

/** UI model for the AI-style science conversation. */
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

        /*
 * Responsibility: Initializes ScienceChatMessage and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
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

        /*
 * Responsibility: Performs the `user` operation for ScienceChatMessage within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public static ScienceChatMessage user(String text, String imageUri, ScienceSubject subject) {
        return new ScienceChatMessage(Role.USER, text, imageUri, false, false,
                false, 0, subject, text, false);
    }

        /*
 * Responsibility: Retrieves the data/state required by `loading` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public static ScienceChatMessage loading(ScienceSubject subject, String sourceQuestion) {
        return new ScienceChatMessage(Role.ASSISTANT, "Thinking…", null, true, false,
                false, 0, subject, sourceQuestion, false);
    }

        /*
 * Responsibility: Performs the `assistant` operation for ScienceChatMessage within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public static ScienceChatMessage assistant(String text,
                                                long historyId,
                                                ScienceSubject subject,
                                                String sourceQuestion,
                                                boolean fromCache,
                                                boolean bookmarked) {
        return new ScienceChatMessage(Role.ASSISTANT, text, null, false, false,
                fromCache, historyId, subject, sourceQuestion, bookmarked);
    }

        /*
 * Responsibility: Performs the `error` operation for ScienceChatMessage within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public static ScienceChatMessage error(String message,
                                           ScienceSubject subject,
                                           String sourceQuestion) {
        return new ScienceChatMessage(Role.ASSISTANT, message, null, false, true,
                false, 0, subject, sourceQuestion, false);
    }

    /** Loading rows are intentionally not persisted across configuration changes. */
        /*
 * Responsibility: Converts input data in `toJson` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
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

        /*
 * Responsibility: Converts input data in `fromJson` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
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

        /*
 * Responsibility: Retrieves the data/state required by `getRole` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public Role getRole() { return role; }
        /*
 * Responsibility: Retrieves the data/state required by `getText` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getText() { return text; }
        /*
 * Responsibility: Retrieves the data/state required by `getImageUri` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getImageUri() { return imageUri; }
        /*
 * Responsibility: Checks the condition represented by `isLoading` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isLoading() { return loading; }
        /*
 * Responsibility: Checks the condition represented by `isError` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isError() { return error; }
        /*
 * Responsibility: Checks the condition represented by `isFromCache` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isFromCache() { return fromCache; }
        /*
 * Responsibility: Retrieves the data/state required by `getHistoryId` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getHistoryId() { return historyId; }
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getSubject() { return subject; }
        /*
 * Responsibility: Retrieves the data/state required by `getSourceQuestion` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getSourceQuestion() { return sourceQuestion; }
        /*
 * Responsibility: Checks the condition represented by `isBookmarked` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isBookmarked() { return bookmarked; }
        /*
 * Responsibility: Updates the state or value handled by `setBookmarked` for ScienceChatMessage.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }
}
