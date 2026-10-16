package com.example.historymodule.util;

/*
 * Role: Utility class for History, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public final class HistoryPromptFactory {

    public String buildAnswerPrompt(String latestMessage,
                                    String conversationContext,
                                    boolean hasImage) {
        String message = latestMessage == null ? "" : latestMessage.trim();
        String context = conversationContext == null ? "" : conversationContext.trim();

        return "Bạn là gia sư Lịch sử trong ứng dụng AI Study Mentor.\n"
                + "Hỗ trợ Lịch sử Việt Nam và lịch sử nước ngoài bằng tiếng Việt rõ ràng, trung lập.\n\n"
                + "NGUYÊN TẮC:\n"
                + "- Kiểm tra tên nhân vật, niên đại, địa danh, triều đại và quan hệ nguyên nhân – kết quả.\n"
                + "- Phân biệt dữ kiện được thừa nhận rộng rãi với nhận định hoặc vấn đề còn tranh luận.\n"
                + "- Không bịa nguồn, câu trích dẫn, số liệu, sắc lệnh hoặc sự kiện. Khi không chắc, nói rõ.\n"
                + "- Giải thích bối cảnh chính trị, kinh tế, xã hội và văn hóa của thời kỳ.\n"
                + "- Với câu hỏi so sánh, dùng tiêu chí: thời gian, mục tiêu, lực lượng, diễn biến, kết quả và ý nghĩa.\n"
                + "- Với yêu cầu ôn thi, ưu tiên mốc thời gian, từ khóa, nguyên nhân, diễn biến, kết quả và ý nghĩa.\n"
                + (hasImage
                ? "- Ảnh đã được gửi kèm. Mô tả phần đọc được, nêu phần mờ và không tự đoán dữ kiện.\n"
                : "")
                + (context.isEmpty() ? "" : "\nHỘI THOẠI GẦN ĐÂY:\n" + context
                + "\n\nGiữ đúng nhân vật, sự kiện và giai đoạn của các câu hỏi nối tiếp. "
                + "Chỉ đổi chủ đề khi người dùng nêu rõ. Nếu câu ngắn có hai cách hiểu, hỏi lại thay vì đoán.\n")
                + "\nTIN NHẮN MỚI NHẤT:\n" + message + "\n\n"
                + "Trả lời trực tiếp trước, sau đó giải thích dễ đọc. Khi phù hợp, thêm dòng thời gian hoặc bảng so sánh ngắn. "
                + "Không dừng giữa câu và không thêm câu hỏi cuối khi yêu cầu đã được giải quyết.";
    }

    /*
     * Responsibility: Creates the object or data structure for `build continuation prompt` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    public String buildContinuationPrompt(String latestMessage,
                                          String conversationContext,
                                          String partialAnswer,
                                          int continuationNumber) {
        String tail = partialAnswer == null ? "" : partialAnswer.trim();
        if (tail.length() > 7_000) tail = tail.substring(tail.length() - 7_000);
        String context = conversationContext == null ? "" : conversationContext.trim();
        if (context.length() > 3_500) context = context.substring(context.length() - 3_500);

        return "Câu trả lời trước bị cắt do giới hạn đầu ra. Tiếp tục chính xác cùng chủ đề và cùng ngôn ngữ.\n"
                + "Không bắt đầu lại, không lặp tiêu đề hoặc nội dung đã viết.\n"
                + "Tin nhắn: " + latestMessage + "\n"
                + (context.isEmpty() ? "" : "Ngữ cảnh:\n" + context + "\n")
                + "Lần nối tiếp: " + continuationNumber + "\n\n"
                + "PHẦN CUỐI ĐANG DỞ:\n" + tail;
    }

    /*
     * Responsibility: Creates the object or data structure for `build quiz prompt` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    public String buildQuizPrompt(String topic,
                                  String educationLevel,
                                  int questionCount) {
        int count = Math.max(5, Math.min(10, questionCount));
        String safeTopic = topic == null || topic.trim().isEmpty() ? "Tổng hợp" : topic.trim();
        String safeLevel = educationLevel == null || educationLevel.trim().isEmpty()
                ? "Trung học cơ sở" : educationLevel.trim();

        String levelRule;
        if ("Đại học".equalsIgnoreCase(safeLevel)) {
            levelRule = "Ưu tiên phân tích đa nguyên nhân, đánh giá nguồn, so sánh cách diễn giải và liên hệ bối cảnh; không hỏi kiến thức chuyên ngành quá hẹp.";
        } else if ("Trung học phổ thông".equalsIgnoreCase(safeLevel)) {
            levelRule = "Bám kiến thức phổ thông, có câu so sánh, nguyên nhân – kết quả, ý nghĩa và vận dụng mốc thời gian.";
        } else {
            levelRule = "Dùng kiến thức nền tảng, nhân vật và sự kiện tiêu biểu, trình bày ngắn gọn, tránh thuật ngữ học thuật khó.";
        }

        String topicRule;
        if ("Lịch sử Việt Nam".equalsIgnoreCase(safeTopic)) {
            topicRule = "Chỉ dùng lịch sử Việt Nam.";
        } else if ("Lịch sử nước ngoài".equalsIgnoreCase(safeTopic)) {
            topicRule = "Chỉ dùng lịch sử khu vực và thế giới ngoài Việt Nam.";
        } else {
            topicRule = "Kết hợp hợp lý lịch sử Việt Nam và lịch sử nước ngoài; không để toàn bộ câu hỏi thuộc một phía.";
        }

        return "Bạn là giáo viên Lịch sử tạo Quiz cho người học.\n"
                + "Phạm vi: " + safeTopic + ". " + topicRule + "\n"
                + "Cấp học: " + safeLevel + ". " + levelRule + "\n"
                + "Tạo đúng " + count + " câu trắc nghiệm mới bằng tiếng Việt.\n"
                + "Câu hỏi phải dựa trên dữ kiện đáng tin cậy, không bịa niên đại, nhân vật hoặc sự kiện.\n"
                + "Mỗi câu có đúng 4 lựa chọn, chỉ một đáp án đúng, phương án nhiễu hợp lý nhưng không mơ hồ.\n"
                + "Phân bố độ khó cho 5 câu: khoảng 2 EASY, 2 MEDIUM và 1 HARD.\n"
                + "EASY là nhớ/nhận biết; MEDIUM là hiểu và nối nguyên nhân – kết quả; HARD là so sánh, đánh giá hoặc suy luận nhiều bước phù hợp cấp học.\n"
                + "Giải thích ngắn vì sao đáp án đúng.\n\n"
                + "Chỉ trả về JSON array hợp lệ, không markdown. Mỗi object có đúng các trường:\n"
                + "difficulty: EASY, MEDIUM hoặc HARD;\n"
                + "question: chuỗi; options: mảng 4 chuỗi; correctIndex: 0-3; explanation: chuỗi.";
    }
}
