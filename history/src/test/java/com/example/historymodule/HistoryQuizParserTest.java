package com.example.historymodule;

import com.example.historymodule.ai.HistoryQuizParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
 * Role: Test class for History, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class HistoryQuizParserTest {
    @Test
    public void extractsHistoryQuizArrayFromMarkdownResponse() {
        String response = "```json\n[{\"question\":\"Chiến thắng Điện Biên Phủ diễn ra năm nào?\"}]\n```";

        assertEquals("[{\"question\":\"Chiến thắng Điện Biên Phủ diễn ra năm nào?\"}]",
                HistoryQuizParser.cleanJson(response));
    }

    /*
     * Responsibility: Performs the `malformed history quiz response returns empty array` responsibility of `HistoryQuizParserTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void malformedHistoryQuizResponseReturnsEmptyArray() {
        assertEquals("[]", HistoryQuizParser.cleanJson(null));
        assertEquals("[]", HistoryQuizParser.cleanJson("Không có JSON"));
        assertEquals("[]", HistoryQuizParser.cleanJson("[{\"question\":\"thiếu đóng\"}"));
    }
}
