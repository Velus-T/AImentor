package com.example.historymodule;

import com.example.historymodule.util.HistoryPromptFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Role: Test class for History, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class HistoryPromptFactoryTest {
    @Test
    public void answerPromptUsesHistoryRulesAndConversationContext() {
        HistoryPromptFactory factory = new HistoryPromptFactory();
        String prompt = factory.buildAnswerPrompt(
                "Phân tích nguyên nhân Cách mạng tháng Tám",
                "Học sinh: Sự kiện diễn ra năm nào?\nAI: Năm 1945.",
                false);

        assertTrue(prompt.contains("gia sư Lịch sử"));
        assertTrue(prompt.contains("niên đại"));
        assertTrue(prompt.contains("nguyên nhân – kết quả"));
        assertTrue(prompt.contains("HỘI THOẠI GẦN ĐÂY"));
        assertTrue(prompt.contains("Năm 1945"));
        assertTrue(prompt.contains("Cách mạng tháng Tám"));
        assertFalse(prompt.contains("Ảnh đã được gửi kèm"));
    }

    /*
     * Responsibility: Performs the `image prompt warns against guessing unreadable history evidence` responsibility of `HistoryPromptFactoryTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void imagePromptWarnsAgainstGuessingUnreadableHistoryEvidence() {
        String prompt = new HistoryPromptFactory().buildAnswerPrompt(
                "Đọc nội dung tài liệu lịch sử này", "", true);

        assertTrue(prompt.contains("Ảnh đã được gửi kèm"));
        assertTrue(prompt.contains("không tự đoán dữ kiện"));
    }
}
