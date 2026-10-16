package com.example.modulelaptrinh.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** DTO cấu hình yêu cầu tạo Quiz: subject, level, topic và số câu. */
public class QuizRequest {
    // Biến `subject` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private final String subject;
    // Biến `level` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private final String level;
    // Biến `topic` lưu chuỗi dữ liệu/cấu hình được class sử dụng.
    private final String topic;
    // Biến `numberOfQuestions` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private final int numberOfQuestions;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private final int userId;
    // Biến `variationSeed` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private final long variationSeed;
    // Danh sách `recentlyUsedPrompts` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private final List<String> recentlyUsedPrompts;

    // Constructor `QuizRequest`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public QuizRequest(String subject, String level, String topic, int numberOfQuestions) {
        // Chuyển constructor rút gọn sang constructor đầy đủ với user mặc định, seed ngẫu nhiên và danh sách lịch sử rỗng.
        this(subject, level, topic, numberOfQuestions, 0, System.nanoTime(), Collections.emptyList());
    }

    // Constructor `QuizRequest`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    private QuizRequest(String subject, String level, String topic, int numberOfQuestions,
                        int userId, long variationSeed, List<String> recentlyUsedPrompts) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `subject` của đối tượng hiện tại.
        this.subject = subject == null ? "Lập trình" : subject;
        // Gán giá trị tham số/đầu vào vào thuộc tính `level` của đối tượng hiện tại.
        this.level = level == null ? "Cơ bản" : level;
        // Gán giá trị tham số/đầu vào vào thuộc tính `topic` của đối tượng hiện tại.
        this.topic = topic == null || topic.trim().isEmpty() ? "Kiến thức lập trình" : topic.trim();
        // Đồng bộ với QuizSelectionFragment: cho phép 3 đến 15 câu.
        this.numberOfQuestions = Math.max(3, Math.min(15, numberOfQuestions));
        // Gán giá trị tham số/đầu vào vào thuộc tính `userId` của đối tượng hiện tại.
        this.userId = Math.max(0, userId);
        // Gán giá trị tham số/đầu vào vào thuộc tính `variationSeed` của đối tượng hiện tại.
        this.variationSeed = variationSeed;
        // Gán giá trị tham số/đầu vào vào thuộc tính `recentlyUsedPrompts` của đối tượng hiện tại.
        this.recentlyUsedPrompts = recentlyUsedPrompts == null
                // Nếu không có lịch sử prompt thì dùng danh sách rỗng bất biến để tránh null.
                ? Collections.emptyList()
                // Nếu có dữ liệu thì copy rồi khóa danh sách để bên ngoài không sửa trạng thái nội bộ.
                : Collections.unmodifiableList(new ArrayList<>(recentlyUsedPrompts));
    }

    // Hàm `getSubject()`: Trả về giá trị Subject hiện đang được đối tượng lưu giữ.
    public String getSubject() { return subject; }
    // Hàm `getLevel()`: Trả về giá trị Level hiện đang được đối tượng lưu giữ.
    public String getLevel() { return level; }
    // Hàm `getTopic()`: Trả về giá trị Topic hiện đang được đối tượng lưu giữ.
    public String getTopic() { return topic; }
    // Hàm `getNumberOfQuestions()`: Trả về giá trị NumberOfQuestions hiện đang được đối tượng lưu giữ.
    public int getNumberOfQuestions() { return numberOfQuestions; }
    // Hàm `getUserId()`: Trả về giá trị UserId hiện đang được đối tượng lưu giữ.
    public int getUserId() { return userId; }
    // Hàm `getVariationSeed()`: Trả về giá trị VariationSeed hiện đang được đối tượng lưu giữ.
    public long getVariationSeed() { return variationSeed; }
    // Hàm `getRecentlyUsedPrompts()`: Trả về giá trị RecentlyUsedPrompts hiện đang được đối tượng lưu giữ.
    public List<String> getRecentlyUsedPrompts() { return recentlyUsedPrompts; }
}
