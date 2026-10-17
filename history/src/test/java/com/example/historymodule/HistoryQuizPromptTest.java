package com.example.historymodule;

import com.example.historymodule.util.HistoryPromptFactory;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/*
 * Role: Test class for History, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class HistoryQuizPromptTest {
    @Test
    public void vietnamHistoryQuizKeepsTopicAndHighSchoolRules() {
        String prompt = new HistoryPromptFactory().buildQuizPrompt(
                "Lịch sử Việt Nam", "Trung học phổ thông", 7);

        assertTrue(prompt.contains("Tạo đúng 7 câu"));
        assertTrue(prompt.contains("Chỉ dùng lịch sử Việt Nam"));
        assertTrue(prompt.contains("nguyên nhân – kết quả"));
        assertTrue(prompt.contains("correctIndex: 0-3"));
    }

    /*
     * Responsibility: Performs the `question count is clamped and missing values use history defaults` responsibility of `HistoryQuizPromptTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void questionCountIsClampedAndMissingValuesUseHistoryDefaults() {
        HistoryPromptFactory factory = new HistoryPromptFactory();
        String minimumPrompt = factory.buildQuizPrompt(null, null, 2);
        String maximumPrompt = factory.buildQuizPrompt("Lịch sử nước ngoài", "Đại học", 50);

        assertTrue(minimumPrompt.contains("Phạm vi: Tổng hợp"));
        assertTrue(minimumPrompt.contains("Cấp học: Trung học cơ sở"));
        assertTrue(minimumPrompt.contains("Tạo đúng 5 câu"));
        assertTrue(maximumPrompt.contains("Tạo đúng 15 câu"));
        assertTrue(maximumPrompt.contains("Chỉ dùng lịch sử khu vực và thế giới ngoài Việt Nam"));
        assertTrue(maximumPrompt.contains("đánh giá nguồn"));
    }
}
