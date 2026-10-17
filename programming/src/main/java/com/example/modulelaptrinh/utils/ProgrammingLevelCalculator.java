package com.example.modulelaptrinh.utils;

/** Chỉ tính level/progress từ total EXP; không đọc/ghi database. */
public final class ProgrammingLevelCalculator {
    // Tính/đọc và lưu giá trị `MAX_LEVEL` (max level) để sử dụng ở các bước phía sau.
    public static final int MAX_LEVEL = 20;
    private static final int BASE_EXP_TO_NEXT_LEVEL = 100;
    private static final int EXP_STEP_PER_LEVEL = 20;

    // Constructor `ProgrammingLevelCalculator`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    private ProgrammingLevelCalculator() { }

    // Hàm `getLevel()`: Trả về giá trị Level hiện đang được đối tượng lưu giữ.
    public static int getLevel(int totalExp) { return com.example.mentor.data.QuizPolicy.level(totalExp); }

    // Hàm `getTotalExpForLevel()`: Trả về giá trị TotalExpForLevel hiện đang được đối tượng lưu giữ.
    public static int getTotalExpForLevel(int level) { return com.example.mentor.data.QuizPolicy.threshold(level); }

    // Hàm `getExpToNextLevel()`: Trả về giá trị ExpToNextLevel hiện đang được đối tượng lưu giữ.
    public static int getExpToNextLevel(int currentLevel) { return com.example.mentor.data.QuizPolicy.required(currentLevel); }

    // Hàm `getExpInsideCurrentLevel()`: Trả về giá trị ExpInsideCurrentLevel hiện đang được đối tượng lưu giữ.
    public static int getExpInsideCurrentLevel(int totalExp) { return com.example.mentor.data.QuizPolicy.inside(totalExp); }

    // Hàm `getProgressPercent()`: Trả về giá trị ProgressPercent hiện đang được đối tượng lưu giữ.
    public static int getProgressPercent(int totalExp) { return com.example.mentor.data.QuizPolicy.progress(totalExp); }

    // Hàm `getTitleResId()`: Trả về giá trị TitleResId hiện đang được đối tượng lưu giữ.
    public static int getTitleResId(int level) {
        // Tính/đọc và lưu giá trị `safeLevel` (safe level) để sử dụng ở các bước phía sau.
        int safeLevel = Math.max(1, Math.min(level, MAX_LEVEL));
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (safeLevel <= 4) return com.example.modulelaptrinh.R.string.prog_level_1;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (safeLevel <= 8) return com.example.modulelaptrinh.R.string.prog_level_2;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (safeLevel <= 12) return com.example.modulelaptrinh.R.string.prog_level_3;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (safeLevel <= 16) return com.example.modulelaptrinh.R.string.prog_level_4;
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return com.example.modulelaptrinh.R.string.prog_level_5;
    }
}
