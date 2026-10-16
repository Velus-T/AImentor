package com.example.historymodule;

/*
 * Role: Model class for History, representing data exchanged between the UI, database, and/or AI layer.
 * Purpose: The model provides a clear data structure, avoids passing many separate values, and gives different layers a shared format.
 */
public final class HistoryMessage {
    public static final String SENT_BY_ME = "me";
    public static final String SENT_BY_BOT = "bot";

    private String message;
    private final String sentBy;
    private boolean pending;
    private final boolean imageAttached;

    /*
     * Responsibility: Initializes the `HistoryMessage` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public HistoryMessage(String message, String sentBy) {
        this(message, sentBy, false, false);
    }

    public HistoryMessage(String message,
                          String sentBy,
                          boolean pending,
                          boolean imageAttached) {
        this.message = message == null ? "" : message;
        this.sentBy = sentBy == null ? SENT_BY_BOT : sentBy;
        this.pending = pending;
        this.imageAttached = imageAttached;
    }

    /*
     * Responsibility: Performs the `pending` responsibility of `HistoryMessage` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public static HistoryMessage pending() {
        return new HistoryMessage("AI đang phân tích...", SENT_BY_BOT, true, false);
    }

    public String getMessage() {
        return message;
    }

    /*
     * Responsibility: Applies or updates `set message` in the current state or UI.
     * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
     */
    public void setMessage(String message) {
        this.message = message == null ? "" : message;
    }

    public String getSentBy() {
        return sentBy;
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `is pending` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `has image attached` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    public boolean hasImageAttached() {
        return imageAttached;
    }
}
