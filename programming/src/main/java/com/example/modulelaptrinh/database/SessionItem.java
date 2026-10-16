package com.example.modulelaptrinh.database;

/** Model một dòng Chat History: session id, name và timestamp. */
public class SessionItem {
    // Biến `id` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private long id;
    // Biến `name` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String name;
    // Biến `timestamp` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String timestamp;

    // Constructor `SessionItem`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public SessionItem() {}

    // Constructor `SessionItem`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public SessionItem(long id, String name, String timestamp) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `id` của đối tượng hiện tại.
        this.id = id;
        // Gán giá trị tham số/đầu vào vào thuộc tính `name` của đối tượng hiện tại.
        this.name = name;
        // Gán giá trị tham số/đầu vào vào thuộc tính `timestamp` của đối tượng hiện tại.
        this.timestamp = timestamp;
    }

    // Hàm `getId()`: Trả về giá trị Id hiện đang được đối tượng lưu giữ.
    public long getId() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return id;
    }

    // Hàm `setId()`: Cập nhật giá trị Id cho đối tượng hiện tại.
    public void setId(long id) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `id` của đối tượng hiện tại.
        this.id = id;
    }

    // Hàm `getName()`: Trả về giá trị Name hiện đang được đối tượng lưu giữ.
    public String getName() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return name;
    }

    // Hàm `setName()`: Cập nhật giá trị Name cho đối tượng hiện tại.
    public void setName(String name) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `name` của đối tượng hiện tại.
        this.name = name;
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
