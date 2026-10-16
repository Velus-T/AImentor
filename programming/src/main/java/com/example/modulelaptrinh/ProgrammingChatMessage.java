package com.example.modulelaptrinh;

import java.util.ArrayList;
import java.util.List;

/**
 * MODEL CỦA MỘT TIN NHẮN CHAT.
 * {@code content} là nội dung, {@code isUser} phân biệt User/AI,
 * {@code imageBase64List} giữ ảnh đã mã hóa Base64 để có thể lưu/khôi phục từ SQLite.
 */
public class ProgrammingChatMessage {
    // Biến `content` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private String content;
    // Cờ `isUser` lưu trạng thái true/false để điều khiển luồng xử lý.
    private boolean isUser;
    // Danh sách `imageBase64List` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<String> imageBase64List;

    // Constructor `ProgrammingChatMessage`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingChatMessage(String content, boolean isUser) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `content` của đối tượng hiện tại.
        this.content = content;
        // Gán giá trị tham số/đầu vào vào thuộc tính `isUser` của đối tượng hiện tại.
        this.isUser = isUser;
        // Gán giá trị tham số/đầu vào vào thuộc tính `imageBase64List` của đối tượng hiện tại.
        this.imageBase64List = new ArrayList<>();
    }

    // Constructor `ProgrammingChatMessage`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingChatMessage(String content, boolean isUser, List<String> imageBase64List) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `content` của đối tượng hiện tại.
        this.content = content;
        // Gán giá trị tham số/đầu vào vào thuộc tính `isUser` của đối tượng hiện tại.
        this.isUser = isUser;
        // Gán giá trị tham số/đầu vào vào thuộc tính `imageBase64List` của đối tượng hiện tại.
        this.imageBase64List = imageBase64List != null ? imageBase64List : new ArrayList<>();
    }

    // Hàm `getContent()`: Trả về giá trị Content hiện đang được đối tượng lưu giữ.
    public String getContent() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return content;
    }

    // Hàm `isUser()`: Kiểm tra trạng thái/điều kiện và trả về true hoặc false.
    public boolean isUser() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return isUser;
    }

    // Hàm `getImageBase64List()`: Trả về giá trị ImageBase64List hiện đang được đối tượng lưu giữ.
    public List<String> getImageBase64List() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageBase64List;
    }

    // Hàm `setImageBase64List()`: Cập nhật giá trị ImageBase64List cho đối tượng hiện tại.
    public void setImageBase64List(List<String> imageBase64List) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `imageBase64List` của đối tượng hiện tại.
        this.imageBase64List = imageBase64List != null ? imageBase64List : new ArrayList<>();
    }

    // Hàm `addImage()`: Thêm dữ liệu/phần tử mới vào trạng thái hoặc danh sách hiện tại.
    public void addImage(String imageBase64) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (imageBase64List == null) {
            // Cập nhật danh sách ảnh đã mã hóa Base64 theo trạng thái/kết quả vừa tính được.
            imageBase64List = new ArrayList<>();
        }
        // Thêm phần tử vừa tạo/xử lý vào danh sách `imageBase64List`.
        imageBase64List.add(imageBase64);
    }

    // Hàm `hasImages()`: Kiểm tra trạng thái/điều kiện và trả về true hoặc false.
    public boolean hasImages() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageBase64List != null && !imageBase64List.isEmpty();
    }

    // Hàm `getImageCount()`: Trả về giá trị ImageCount hiện đang được đối tượng lưu giữ.
    public int getImageCount() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageBase64List != null ? imageBase64List.size() : 0;
    }

    // Hàm `getFirstImage()`: Trả về giá trị FirstImage hiện đang được đối tượng lưu giữ.
    public String getFirstImage() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return hasImages() ? imageBase64List.get(0) : null;
    }
}
