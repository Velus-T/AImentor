package com.example.modulelaptrinh;

import com.example.modulelaptrinh.models.QuizDifficulty;
import com.example.modulelaptrinh.models.QuizQuestion;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProgrammingQuizQuestionTest {
    @Test
    // Hàm `infersProgrammingDifficultyAndRewardFromQuestionType()`: Thực hiện chức năng `infersProgrammingDifficultyAndRewardFromQuestionType` của class này.
    public void infersProgrammingDifficultyAndRewardFromQuestionType() {
        // Khởi tạo giá trị `question` (question) phục vụ cho bước xử lý tiếp theo.
        QuizQuestion question = new QuizQuestion(
                QuizQuestion.Type.FILL_BLANK,
                "Điền từ khóa để khai báo lớp trong Java",
                Arrays.asList(),
                "class",
                "Java dùng từ khóa class.");

        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizDifficulty.HARD, question.getDifficulty());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(20, question.getExpReward());
    }

    @Test
    // Hàm `protectsOptionsAndUsesSafeDefaults()`: Thực hiện chức năng `protectsOptionsAndUsesSafeDefaults` của class này.
    public void protectsOptionsAndUsesSafeDefaults() {
        // Khởi tạo danh sách các lựa chọn trả lời phục vụ cho bước xử lý tiếp theo.
        List<String> options = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        // Khởi tạo giá trị `question` (question) phục vụ cho bước xử lý tiếp theo.
        QuizQuestion question = new QuizQuestion(
                null, null, options, null, null, null);
        // Gọi `set()` trên `options` để thực hiện thao tác tương ứng.
        options.set(0, "Changed");

        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizQuestion.Type.MULTIPLE_CHOICE, question.getType());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("", question.getPrompt());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("A", question.getOptions().get(0));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("", question.getCorrectAnswer());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizDifficulty.MEDIUM, question.getDifficulty());
    }
}
