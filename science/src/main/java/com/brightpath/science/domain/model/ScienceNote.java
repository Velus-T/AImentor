package com.brightpath.science.domain.model;

/**
 * Immutable personal note created inside the Science module.
 *
 * <p>review NOTE: Notes are local learning records. They are not sent to Gemini.
 * Ownership is enforced by the repository using the active {@code userId}.</p>
 */
public final class ScienceNote {
    private final long id;
    private final String title;
    private final String content;
    private final String subject;
    private final long updatedAt;

        /*
 * Responsibility: Initializes ScienceNote and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceNote(long id,
                       String title,
                       String content,
                       String subject,
                       long updatedAt) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.subject = subject == null ? "" : subject;
        this.updatedAt = updatedAt;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getId() {
        return id;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTitle` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getTitle() {
        return title;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getContent` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getContent() {
        return content;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getSubject() {
        return subject;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getUpdatedAt` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getUpdatedAt() {
        return updatedAt;
    }
}
