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
    public static int getLevel(int totalExp) {
        // Tính/đọc và lưu giá trị `safeExp` (safe exp) để sử dụng ở các bước phía sau.
        int safeExp = Math.max(0, totalExp);
        // Tính/đọc và lưu mức độ khó để sử dụng ở các bước phía sau.
        int level = 1;
        // Tiếp tục lặp khi điều kiện vẫn còn đúng.
        while (level < MAX_LEVEL && safeExp >= getTotalExpForLevel(level + 1)) {
            // Tăng `level` thêm 1 để chuyển/cập nhật sang trạng thái tiếp theo.
            level++;
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return level;
    }

    // Hàm `getTotalExpForLevel()`: Trả về giá trị TotalExpForLevel hiện đang được đối tượng lưu giữ.
    public static int getTotalExpForLevel(int level) {
        // Tính/đọc và lưu giá trị `safeLevel` (safe level) để sử dụng ở các bước phía sau.
        int safeLevel = Math.max(1, Math.min(level, MAX_LEVEL));
        // Tính/đọc và lưu giá trị `completedLevels` (completed levels) để sử dụng ở các bước phía sau.
        int completedLevels = safeLevel - 1;
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return BASE_EXP_TO_NEXT_LEVEL * completedLevels
                + EXP_STEP_PER_LEVEL * completedLevels * (completedLevels - 1) / 2;
    }

    // Hàm `getExpToNextLevel()`: Trả về giá trị ExpToNextLevel hiện đang được đối tượng lưu giữ.
    public static int getExpToNextLevel(int currentLevel) {
        // Tính/đọc và lưu giá trị `safeLevel` (safe level) để sử dụng ở các bước phía sau.
        int safeLevel = Math.max(1, Math.min(currentLevel, MAX_LEVEL));
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (safeLevel >= MAX_LEVEL) return 0;
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return BASE_EXP_TO_NEXT_LEVEL + EXP_STEP_PER_LEVEL * (safeLevel - 1);
    }

    // Hàm `getExpInsideCurrentLevel()`: Trả về giá trị ExpInsideCurrentLevel hiện đang được đối tượng lưu giữ.
    public static int getExpInsideCurrentLevel(int totalExp) {
        // Tính/đọc và lưu giá trị `safeExp` (safe exp) để sử dụng ở các bước phía sau.
        int safeExp = Math.max(0, totalExp);
        // Tính/đọc và lưu mức độ khó để sử dụng ở các bước phía sau.
        int level = getLevel(safeExp);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return Math.max(0, safeExp - getTotalExpForLevel(level));
    }

    // Hàm `getProgressPercent()`: Trả về giá trị ProgressPercent hiện đang được đối tượng lưu giữ.
    public static int getProgressPercent(int totalExp) {
        // Tính/đọc và lưu mức độ khó để sử dụng ở các bước phía sau.
        int level = getLevel(totalExp);
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (level >= MAX_LEVEL) return 100;
        // Tính/đọc và lưu giá trị `required` (required) để sử dụng ở các bước phía sau.
        int required = getExpToNextLevel(level);
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (required <= 0) return 100;
        // Tính/đọc và lưu giá trị `currentExp` (current exp) để sử dụng ở các bước phía sau.
        int currentExp = totalExp - getTotalExpForLevel(level);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return Math.min(100, Math.round(currentExp * 100f / required));
    }

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
