package com.example.modulelaptrinh;

import com.example.modulelaptrinh.models.QuizRequest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProgrammingQuizRequestTest {
    @Test
    // Hàm `keepsProgrammingQuizConfigurationAndTrimsTopic()`: Thực hiện chức năng `keepsProgrammingQuizConfigurationAndTrimsTopic` của class này.
    public void keepsProgrammingQuizConfigurationAndTrimsTopic() {
        // Khởi tạo dữ liệu request chuẩn bị gửi đi phục vụ cho bước xử lý tiếp theo.
        QuizRequest request = new QuizRequest("Java", "Nâng cao", "  OOP và SOLID  ", 6);

        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("Java", request.getSubject());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("Nâng cao", request.getLevel());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("OOP và SOLID", request.getTopic());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(6, request.getNumberOfQuestions());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(0, request.getUserId());
        // Gọi hàm `assertTrue()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertTrue(request.getRecentlyUsedPrompts().isEmpty());
    }

    @Test
    // Hàm `missingProgrammingValuesUseDefaultsAndQuestionCountIsClamped()`: Thực hiện chức năng `missingProgrammingValuesUseDefaultsAndQuestionCountIsClamped` của class này.
    public void missingProgrammingValuesUseDefaultsAndQuestionCountIsClamped() {
        // Khởi tạo giá trị `minimum` (minimum) phục vụ cho bước xử lý tiếp theo.
        QuizRequest minimum = new QuizRequest(null, null, "  ", 1);
        // Khởi tạo giá trị `maximum` (maximum) phục vụ cho bước xử lý tiếp theo.
        QuizRequest maximum = new QuizRequest("Python", "Cơ bản", "Lists", 50);

        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("Lập trình", minimum.getSubject());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("Cơ bản", minimum.getLevel());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("Kiến thức lập trình", minimum.getTopic());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(5, minimum.getNumberOfQuestions());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(15, maximum.getNumberOfQuestions());
    }
}
