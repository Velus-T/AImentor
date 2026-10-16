package com.example.modulelaptrinh.database;

/** Model legacy của bảng chat_history cũ; UI mới dùng SessionItem + messages. */
public class ProgrammingChatHistoryItem {
    // Biến `id` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int id;
    // Biến `question` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String question;
    // Biến `answer` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String answer;
    // Biến `timestamp` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String timestamp;

    // Constructor `ProgrammingChatHistoryItem`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingChatHistoryItem() {}

    // Constructor `ProgrammingChatHistoryItem`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingChatHistoryItem(String question, String answer, String timestamp) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `question` của đối tượng hiện tại.
        this.question = question;
        // Gán giá trị tham số/đầu vào vào thuộc tính `answer` của đối tượng hiện tại.
        this.answer = answer;
        // Gán giá trị tham số/đầu vào vào thuộc tính `timestamp` của đối tượng hiện tại.
        this.timestamp = timestamp;
    }

    // Hàm `getId()`: Trả về giá trị Id hiện đang được đối tượng lưu giữ.
    public int getId() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return id;
    }

    // Hàm `setId()`: Cập nhật giá trị Id cho đối tượng hiện tại.
    public void setId(int id) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `id` của đối tượng hiện tại.
        this.id = id;
    }

    // Hàm `getQuestion()`: Trả về giá trị Question hiện đang được đối tượng lưu giữ.
    public String getQuestion() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return question;
    }

    // Hàm `setQuestion()`: Cập nhật giá trị Question cho đối tượng hiện tại.
    public void setQuestion(String question) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `question` của đối tượng hiện tại.
        this.question = question;
    }

    // Hàm `getAnswer()`: Trả về giá trị Answer hiện đang được đối tượng lưu giữ.
    public String getAnswer() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return answer;
    }

    // Hàm `setAnswer()`: Cập nhật giá trị Answer cho đối tượng hiện tại.
    public void setAnswer(String answer) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `answer` của đối tượng hiện tại.
        this.answer = answer;
    }

    // Hàm `getTimestamp()`: Trả về giá trị Timestamp hiện đang được đối tượng lưu giữ.
    public String getTimestamp() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return timestamp;
    }

    // Hàm `setTimestamp()`: Cập nhật giá trị Timestamp cho đối tượng hiện tại.
    public void setTimestamp(String timestamp) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `timestamp` của đối tượng hiện tại.
        this.timestamp = timestamp;
    }
}
