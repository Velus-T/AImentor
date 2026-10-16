package com.example.modulelaptrinh;

import com.example.modulelaptrinh.utils.ProgrammingLevelCalculator;
import com.example.modulelaptrinh.R;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProgrammingLevelCalculatorTest {
    @Test
    // Hàm `calculatesProgrammingLevelAtExpBoundaries()`: Tính toán giá trị kết quả dựa trên dữ liệu đầu vào.
    public void calculatesProgrammingLevelAtExpBoundaries() {
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(1, ProgrammingLevelCalculator.getLevel(-20));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(1, ProgrammingLevelCalculator.getLevel(99));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(2, ProgrammingLevelCalculator.getLevel(100));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(3, ProgrammingLevelCalculator.getLevel(220));
    }

    @Test
    // Hàm `calculatesProgrammingProgressInsideLevel()`: Tính toán giá trị kết quả dựa trên dữ liệu đầu vào.
    public void calculatesProgrammingProgressInsideLevel() {
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(50, ProgrammingLevelCalculator.getExpInsideCurrentLevel(150));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(42, ProgrammingLevelCalculator.getProgressPercent(150));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(120, ProgrammingLevelCalculator.getExpToNextLevel(2));
    }

    @Test
    // Hàm `capsAtMaximumAndUsesProgrammingSpecificTitles()`: Thực hiện chức năng `capsAtMaximumAndUsesProgrammingSpecificTitles` của class này.
    public void capsAtMaximumAndUsesProgrammingSpecificTitles() {
        // Tính/đọc và lưu giá trị `maxExp` (max exp) để sử dụng ở các bước phía sau.
        int maxExp = ProgrammingLevelCalculator.getTotalExpForLevel(
                ProgrammingLevelCalculator.MAX_LEVEL);
        assertEquals(ProgrammingLevelCalculator.MAX_LEVEL,
                // Gọi `getLevel()` trên `ProgrammingLevelCalculator` để thực hiện thao tác tương ứng.
                ProgrammingLevelCalculator.getLevel(maxExp + 50_000));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(100, ProgrammingLevelCalculator.getProgressPercent(maxExp));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(R.string.prog_level_5, ProgrammingLevelCalculator.getTitleResId(20));
    }
}
