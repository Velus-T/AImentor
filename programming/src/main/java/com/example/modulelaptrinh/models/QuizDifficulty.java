package com.example.modulelaptrinh.models;

import com.example.modulelaptrinh.R;

/** Difficulty và EXP reward: EASY=5, MEDIUM=10, HARD=20. */
public enum QuizDifficulty {
  EASY(R.string.quiz_diff_easy),
  MEDIUM(R.string.quiz_diff_medium),
  // Gọi hàm `HARD()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
  HARD(R.string.quiz_diff_hard);

  // Biến `displayNameResId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
  private final int displayNameResId;

  QuizDifficulty(int displayNameResId) {
    // Gán giá trị tham số/đầu vào vào thuộc tính `displayNameResId` của đối tượng hiện tại.
    this.displayNameResId = displayNameResId;
  }

  // Hàm `getDisplayNameResId()`: Trả về giá trị DisplayNameResId hiện đang được đối tượng lưu giữ.
  public int getDisplayNameResId() {
    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
    return displayNameResId;
  }

  // Hàm `getExpReward()`: Trả về giá trị ExpReward hiện đang được đối tượng lưu giữ.
  public int getExpReward() {
    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
    return com.example.mentor.data.QuizPolicy.reward(name());
  }

  // Hàm `fromApiValue()`: Thực hiện chức năng `fromApiValue` của class này.
  public static QuizDifficulty fromApiValue(String value) {
    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
    if (value == null) return MEDIUM;
    // Tính/đọc và lưu giá trị `normalized` (normalized) để sử dụng ở các bước phía sau.
    String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
    if ("EASY".equals(normalized) || "DỄ".equals(normalized)) return EASY;
    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
    if ("HARD".equals(normalized) || "KHÓ".equals(normalized)) return HARD;
    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
    return MEDIUM;
  }

  // Hàm `inferFromType()`: Thực hiện chức năng `inferFromType` của class này.
  public static QuizDifficulty inferFromType(QuizQuestion.Type type) {
    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
    if (type == null) return MEDIUM;
    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
    if (type == QuizQuestion.Type.TRUE_FALSE) return EASY;
    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
    if (type == QuizQuestion.Type.FILL_BLANK) return HARD;
    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
    return MEDIUM;
  }
}
