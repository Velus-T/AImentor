package com.example.mathmodule;
/*
 * Role: Model class for Math, representing data exchanged between the UI, database, and/or AI layer.
 * Purpose: The model provides a clear data structure, avoids passing many separate values, and gives different layers a shared format.
 */
public class MathMessage {
    private String content;
    private boolean isUser; // true nếu là học sinh gửi, false nếu là AI trả lời

    public MathMessage(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

    /*
     * Responsibility: Reads or loads data for `get content` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }
}
