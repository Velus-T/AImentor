package com.example.modulelaptrinh.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Một câu Quiz đã parse từ JSON AI: prompt, options, đáp án, explanation, difficulty. */
public class QuizQuestion {
    public enum Type { MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK }

    // Thuộc tính `type` lưu đối tượng `Type` mà class cần dùng xuyên suốt nhiều hàm.
    private final Type type;
    // Biến `prompt` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private final String prompt;
    // Danh sách `options` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private final List<String> options;
    // Biến `correctAnswer` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private final String correctAnswer;
    // Biến `explanation` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private final String explanation;
    // Thuộc tính `difficulty` lưu đối tượng `QuizDifficulty` mà class cần dùng xuyên suốt nhiều hàm.
    private final QuizDifficulty difficulty;

    // Constructor `QuizQuestion`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public QuizQuestion(Type type, String prompt, List<String> options,
                        String correctAnswer, String explanation) {
        this(type, prompt, options, correctAnswer, explanation,
                // Gọi `inferFromType()` trên `QuizDifficulty` để thực hiện thao tác tương ứng.
                QuizDifficulty.inferFromType(type));
    }

    // Constructor `QuizQuestion`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public QuizQuestion(Type type, String prompt, List<String> options,
                        String correctAnswer, String explanation,
                        QuizDifficulty difficulty) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `type` của đối tượng hiện tại.
        this.type = type == null ? Type.MULTIPLE_CHOICE : type;
        // Gán giá trị tham số/đầu vào vào thuộc tính `prompt` của đối tượng hiện tại.
        this.prompt = prompt == null ? "" : prompt;
        // Gán giá trị tham số/đầu vào vào thuộc tính `options` của đối tượng hiện tại.
        this.options = options == null
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                ? Collections.emptyList()
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                : Collections.unmodifiableList(new ArrayList<>(options));
        // Gán giá trị tham số/đầu vào vào thuộc tính `correctAnswer` của đối tượng hiện tại.
        this.correctAnswer = correctAnswer == null ? "" : correctAnswer;
        // Gán giá trị tham số/đầu vào vào thuộc tính `explanation` của đối tượng hiện tại.
        this.explanation = explanation == null ? "" : explanation;
        // Gán giá trị tham số/đầu vào vào thuộc tính `difficulty` của đối tượng hiện tại.
        this.difficulty = difficulty == null
                ? QuizDifficulty.inferFromType(this.type)
                : difficulty;
    }

    // Hàm `getType()`: Trả về giá trị Type hiện đang được đối tượng lưu giữ.
    public Type getType() { return type; }
    // Hàm `getPrompt()`: Trả về giá trị Prompt hiện đang được đối tượng lưu giữ.
    public String getPrompt() { return prompt; }
    // Hàm `getOptions()`: Trả về giá trị Options hiện đang được đối tượng lưu giữ.
    public List<String> getOptions() { return options; }
    // Hàm `getCorrectAnswer()`: Trả về giá trị CorrectAnswer hiện đang được đối tượng lưu giữ.
    public String getCorrectAnswer() { return correctAnswer; }
    // Hàm `getExplanation()`: Trả về giá trị Explanation hiện đang được đối tượng lưu giữ.
    public String getExplanation() { return explanation; }
    // Hàm `getDifficulty()`: Trả về giá trị Difficulty hiện đang được đối tượng lưu giữ.
    public QuizDifficulty getDifficulty() { return difficulty; }
    // Hàm `getExpReward()`: Trả về giá trị ExpReward hiện đang được đối tượng lưu giữ.
    public int getExpReward() { return difficulty.getExpReward(); }
}
