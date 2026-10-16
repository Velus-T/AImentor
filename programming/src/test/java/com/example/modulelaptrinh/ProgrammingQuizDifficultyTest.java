package com.example.modulelaptrinh;

import com.example.modulelaptrinh.models.QuizDifficulty;
import com.example.modulelaptrinh.models.QuizQuestion;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProgrammingQuizDifficultyTest {
    @Test
    // Hàm `mapsProgrammingDifficultyFromApiValues()`: Thực hiện chức năng `mapsProgrammingDifficultyFromApiValues` của class này.
    public void mapsProgrammingDifficultyFromApiValues() {
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizDifficulty.EASY, QuizDifficulty.fromApiValue("EASY"));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizDifficulty.EASY, QuizDifficulty.fromApiValue("Dễ"));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizDifficulty.HARD, QuizDifficulty.fromApiValue("Khó"));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizDifficulty.MEDIUM, QuizDifficulty.fromApiValue("unknown"));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(QuizDifficulty.MEDIUM, QuizDifficulty.fromApiValue(null));
    }

    @Test
    // Hàm `infersDifficultyFromProgrammingQuestionType()`: Thực hiện chức năng `infersDifficultyFromProgrammingQuestionType` của class này.
    public void infersDifficultyFromProgrammingQuestionType() {
        assertEquals(QuizDifficulty.EASY,
                // Gọi `inferFromType()` trên `QuizDifficulty` để thực hiện thao tác tương ứng.
                QuizDifficulty.inferFromType(QuizQuestion.Type.TRUE_FALSE));
        assertEquals(QuizDifficulty.HARD,
                // Gọi `inferFromType()` trên `QuizDifficulty` để thực hiện thao tác tương ứng.
                QuizDifficulty.inferFromType(QuizQuestion.Type.FILL_BLANK));
        assertEquals(QuizDifficulty.MEDIUM,
                // Gọi `inferFromType()` trên `QuizDifficulty` để thực hiện thao tác tương ứng.
                QuizDifficulty.inferFromType(QuizQuestion.Type.MULTIPLE_CHOICE));
        assertEquals(QuizDifficulty.MEDIUM,
                // Gọi `inferFromType()` trên `QuizDifficulty` để thực hiện thao tác tương ứng.
                QuizDifficulty.inferFromType(null));
    }
}
