package com.brightpath.science.domain.model;

public final class ScienceAnswer {
    private final String content;
    private boolean local;
    public boolean isLocal() { return local; }
    public ScienceAnswer withLocalSource() { ScienceAnswer copy = new ScienceAnswer(content,detectedSubject,difficulty,fromCache,historyId,promptTokens,responseTokens,totalTokens);copy.local=true;return copy; }
    private final String detectedSubject;
    private final String difficulty;
    private final boolean fromCache;
    private final long historyId;
    private final int promptTokens;
    private final int responseTokens;
    private final int totalTokens;

        /*
 * Responsibility: Initializes ScienceAnswer and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceAnswer(String content, String detectedSubject, String difficulty,
                         boolean fromCache, long historyId) {
        this(content, detectedSubject, difficulty, fromCache, historyId, 0, 0, 0);
    }

        /*
 * Responsibility: Initializes ScienceAnswer and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceAnswer(String content, String detectedSubject, String difficulty,
                         boolean fromCache, long historyId,
                         int promptTokens, int responseTokens, int totalTokens) {
        this.content = content == null ? "" : content;
        this.detectedSubject = detectedSubject == null ? "" : detectedSubject;
        this.difficulty = difficulty == null ? "" : difficulty;
        this.fromCache = fromCache;
        this.historyId = historyId;
        this.promptTokens = promptTokens;
        this.responseTokens = responseTokens;
        this.totalTokens = totalTokens;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getContent` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getContent() { return content; }
        /*
 * Responsibility: Retrieves the data/state required by `getDetectedSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getDetectedSubject() { return detectedSubject; }
        /*
 * Responsibility: Retrieves the data/state required by `getDifficulty` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getDifficulty() { return difficulty; }
        /*
 * Responsibility: Checks the condition represented by `isFromCache` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isFromCache() { return fromCache; }
        /*
 * Responsibility: Retrieves the data/state required by `getHistoryId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getHistoryId() { return historyId; }
        /*
 * Responsibility: Retrieves the data/state required by `getPromptTokens` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getPromptTokens() { return promptTokens; }
        /*
 * Responsibility: Retrieves the data/state required by `getResponseTokens` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getResponseTokens() { return responseTokens; }
        /*
 * Responsibility: Retrieves the data/state required by `getTotalTokens` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getTotalTokens() { return totalTokens; }

        /*
 * Responsibility: Returns the `withPersistence` value stored by the ScienceAnswer domain model.
 * Purpose: Provides controlled read access to immutable/model state shared between Science layers.
 */
public ScienceAnswer withPersistence(long newHistoryId, boolean cached) {
        ScienceAnswer copy = new ScienceAnswer(content, detectedSubject, difficulty, cached, newHistoryId,
                promptTokens, responseTokens, totalTokens);copy.local=local;return copy;
    }
}
