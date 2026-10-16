package com.example.modulelaptrinh.models;

import java.util.List;

/** Kết quả Quiz trong RAM; finishQuiz() mới persist các số liệu chính xuống SQLite. */
public class QuizResult {
    // Thuộc tính `request` lưu đối tượng `QuizRequest` mà class cần dùng xuyên suốt nhiều hàm.
    private final QuizRequest request;
    // Danh sách `questions` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private final List<QuizQuestion> questions;
    // Biến `totalQuestions` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private final int totalQuestions;
    // Biến `correctAnswers` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private final int correctAnswers;
    // Biến `expEarned` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private final int expEarned;

    // Constructor `QuizResult`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public QuizResult(QuizRequest request, List<QuizQuestion> questions,
                      int correctAnswers, int expEarned) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `request` của đối tượng hiện tại.
        this.request = request;
        // Gán giá trị tham số/đầu vào vào thuộc tính `questions` của đối tượng hiện tại.
        this.questions = questions;
        // Gán giá trị tham số/đầu vào vào thuộc tính `totalQuestions` của đối tượng hiện tại.
        this.totalQuestions = questions != null ? questions.size() : 0;
        // Gán giá trị tham số/đầu vào vào thuộc tính `correctAnswers` của đối tượng hiện tại.
        this.correctAnswers = Math.max(0, Math.min(correctAnswers, totalQuestions));
        // Gán giá trị tham số/đầu vào vào thuộc tính `expEarned` của đối tượng hiện tại.
        this.expEarned = Math.max(0, expEarned);
    }

    // Hàm `getRequest()`: Trả về giá trị Request hiện đang được đối tượng lưu giữ.
    public QuizRequest getRequest() { return request; }
    // Hàm `getQuestions()`: Trả về giá trị Questions hiện đang được đối tượng lưu giữ.
    public List<QuizQuestion> getQuestions() { return questions; }
    // Hàm `getTotalQuestions()`: Trả về giá trị TotalQuestions hiện đang được đối tượng lưu giữ.
    public int getTotalQuestions() { return totalQuestions; }
    // Hàm `getCorrectAnswers()`: Trả về giá trị CorrectAnswers hiện đang được đối tượng lưu giữ.
    public int getCorrectAnswers() { return correctAnswers; }
    // Hàm `getExpEarned()`: Trả về giá trị ExpEarned hiện đang được đối tượng lưu giữ.
    public int getExpEarned() { return expEarned; }
    // Hàm `getAccuracyPercent()`: Trả về giá trị AccuracyPercent hiện đang được đối tượng lưu giữ.
    public double getAccuracyPercent() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return totalQuestions == 0 ? 0 : (correctAnswers * 100.0) / totalQuestions;
    }
}
