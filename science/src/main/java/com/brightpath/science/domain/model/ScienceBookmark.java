package com.brightpath.science.domain.model;

public final class ScienceBookmark {
    private final long id;
    private final int userId;
    private final long historyId;
    private final ScienceSubject subject;
    private final String title;
    private final String content;
    private final long createdAt;

        /*
 * Responsibility: Initializes ScienceBookmark and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceBookmark(long id, int userId, long historyId, ScienceSubject subject,
                           String title, String content, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.historyId = historyId;
        this.subject = subject;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getId() { return id; }
        /*
 * Responsibility: Retrieves the data/state required by `getUserId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getUserId() { return userId; }
        /*
 * Responsibility: Retrieves the data/state required by `getHistoryId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getHistoryId() { return historyId; }
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getSubject() { return subject; }
        /*
 * Responsibility: Retrieves the data/state required by `getTitle` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getTitle() { return title; }
        /*
 * Responsibility: Retrieves the data/state required by `getContent` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getContent() { return content; }
        /*
 * Responsibility: Retrieves the data/state required by `getCreatedAt` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getCreatedAt() { return createdAt; }
}
