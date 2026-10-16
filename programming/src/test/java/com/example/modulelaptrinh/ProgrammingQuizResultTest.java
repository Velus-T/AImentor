package com.example.modulelaptrinh;

import com.example.modulelaptrinh.models.QuizQuestion;
import com.example.modulelaptrinh.models.QuizRequest;
import com.example.modulelaptrinh.models.QuizResult;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ProgrammingQuizResultTest {
    @Test
    // Hàm `calculatesProgrammingQuizAccuracyAndClampsValues()`: Tính toán giá trị kết quả dựa trên dữ liệu đầu vào.
    public void calculatesProgrammingQuizAccuracyAndClampsValues() {
        // Khởi tạo kết quả trả về sau khi xử lý phục vụ cho bước xử lý tiếp theo.
        QuizResult result = new QuizResult(
                new QuizRequest("Java", "Cơ bản", "Biến", 3),
                Arrays.asList(question("Q1"), question("Q2"), question("Q3")),
                8,
                -10);

        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(3, result.getTotalQuestions());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(3, result.getCorrectAnswers());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(0, result.getExpEarned());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(100.0, result.getAccuracyPercent(), 0.001);
    }

    @Test
    // Hàm `emptyProgrammingQuizHasZeroAccuracy()`: Thực hiện chức năng `emptyProgrammingQuizHasZeroAccuracy` của class này.
    public void emptyProgrammingQuizHasZeroAccuracy() {
        // Khởi tạo kết quả trả về sau khi xử lý phục vụ cho bước xử lý tiếp theo.
        QuizResult result = new QuizResult(
                new QuizRequest("Python", "Cơ bản", "List", 3),
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                Collections.emptyList(),
                2,
                15);

        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(0, result.getTotalQuestions());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(0, result.getCorrectAnswers());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(0.0, result.getAccuracyPercent(), 0.001);
    }

    // Hàm `question()`: Thực hiện chức năng `question` của class này.
    private QuizQuestion question(String prompt) {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new QuizQuestion(
                QuizQuestion.Type.TRUE_FALSE,
                prompt,
                Arrays.asList("True", "False"),
                "True",
                "Explanation");
    }
}
