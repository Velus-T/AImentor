package com.example.modulelaptrinh.database;

/** Model được tạo từ một record của bảng notes để đưa lên UI. */
public class ProgrammingNote {
    private int id;  // Giữ nguyên int
    // Biến `title` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String title;
    // Biến `content` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String content;
    // Biến `timestamp` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String timestamp;

    // Constructor `ProgrammingNote`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingNote() {}

    // Constructor `ProgrammingNote`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingNote(String title, String content, String timestamp) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `title` của đối tượng hiện tại.
        this.title = title;
        // Gán giá trị tham số/đầu vào vào thuộc tính `content` của đối tượng hiện tại.
        this.content = content;
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

    // Hàm `getTitle()`: Trả về giá trị Title hiện đang được đối tượng lưu giữ.
    public String getTitle() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return title;
    }

    // Hàm `setTitle()`: Cập nhật giá trị Title cho đối tượng hiện tại.
    public void setTitle(String title) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `title` của đối tượng hiện tại.
        this.title = title;
    }

    // Hàm `getContent()`: Trả về giá trị Content hiện đang được đối tượng lưu giữ.
    public String getContent() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return content;
    }

    // Hàm `setContent()`: Cập nhật giá trị Content cho đối tượng hiện tại.
    public void setContent(String content) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `content` của đối tượng hiện tại.
        this.content = content;
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
