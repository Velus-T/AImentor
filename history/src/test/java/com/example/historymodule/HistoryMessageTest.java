package com.example.historymodule;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Role: Test class for History, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class HistoryMessageTest {
    @Test
    public void pendingMessageRepresentsHistoryAiAnalysisState() {
        HistoryMessage message = HistoryMessage.pending();

        assertEquals(HistoryMessage.SENT_BY_BOT, message.getSentBy());
        assertEquals("AI đang phân tích...", message.getMessage());
        assertTrue(message.isPending());
        assertFalse(message.hasImageAttached());
    }

    /*
     * Responsibility: Performs the `null fields are safe and state can be updated` responsibility of `HistoryMessageTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void nullFieldsAreSafeAndStateCanBeUpdated() {
        HistoryMessage message = new HistoryMessage(null, null, false, true);

        assertEquals("", message.getMessage());
        assertEquals(HistoryMessage.SENT_BY_BOT, message.getSentBy());
        assertTrue(message.hasImageAttached());

        message.setMessage("Phân tích sự kiện lịch sử");
        message.setPending(true);
        assertEquals("Phân tích sự kiện lịch sử", message.getMessage());
        assertTrue(message.isPending());
    }
}
