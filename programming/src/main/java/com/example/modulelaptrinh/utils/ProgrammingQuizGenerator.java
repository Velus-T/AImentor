package com.example.modulelaptrinh.utils;

import android.util.Log;

import com.example.modulelaptrinh.models.QuizDifficulty;
import com.example.modulelaptrinh.models.QuizQuestion;
import com.example.modulelaptrinh.models.QuizRequest;
import com.example.modulelaptrinh.models.QuizResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * BỘ TẠO QUIZ BẰNG AI.
 *
 * <p>Trách nhiệm đúng 2 việc:</p>
 * <ol>
 *   <li>Xây prompt JSON từ QuizRequest và gọi GeminiApiClient.</li>
 *   <li>Parse JSON AI trả về thành List&lt;QuizQuestion&gt;.</li>
 * </ol>
 * File này KHÔNG lưu database; QuizFragment/ProgrammingDbHelper chịu trách nhiệm persistence.
 */
public class ProgrammingQuizGenerator {
    private static final String TAG = "ProgrammingQuizGenerator";

    private static final String QUIZ_PROMPT_TEMPLATE = String.join("\n",
            "Bạn là chuyên gia giáo dục lập trình. Hãy tạo chính xác %d câu hỏi trắc nghiệm",
            "về chủ đề \"%s\" ở mức \"%s\".",
            "",
            "YÊU CẦU:",
            "1. Mỗi câu có đúng 4 lựa chọn.",
            "2. correct_answer phải chỉ là một chữ A, B, C hoặc D.",
            "3. Có giải thích ngắn gọn, chính xác.",
            "4. Câu hỏi đa dạng: lý thuyết, đọc code, tìm lỗi, dự đoán kết quả và khái niệm.",
            "5. Mỗi câu phải có difficulty là EASY, MEDIUM hoặc HARD.",
            "6. Không tạo câu phụ thuộc vào hình ảnh hoặc tài liệu bên ngoài.",
            "7. Code trong chuỗi JSON phải escape đúng ký tự xuống dòng và dấu ngoặc kép.",
            "",
            "Chỉ trả về JSON hợp lệ, không markdown, không lời dẫn:",
            "{",
            "  \"questions\": [",
            "    {",
            "      \"prompt\": \"Câu hỏi\",",
            "      \"options\": [\"A. ...\", \"B. ...\", \"C. ...\", \"D. ...\"],",
            "      \"correct_answer\": \"A\",",
            "      \"explanation\": \"Giải thích\",",
            "      \"difficulty\": \"EASY\"",
            "    }",
            "  ]",
            "}",
            ""
    );

    // Client `geminiClient` dùng để gửi request và nhận phản hồi từ Gemini API.
    private final GeminiApiClient geminiClient;

    // Constructor `ProgrammingQuizGenerator`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingQuizGenerator() {
        // Gán giá trị tham số/đầu vào vào thuộc tính `geminiClient` của đối tượng hiện tại.
        this.geminiClient = new GeminiApiClient(GeminiApiClient.Usage.QUIZ);
    }

    /**
     * Entry point tạo Quiz. Prompt được format từ số câu/topic/level rồi gửi Gemini.
     */
    public QuizResult generateQuiz(QuizRequest request, int userId) throws Exception {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (request == null) {
            throw new IllegalArgumentException("Thiếu thông tin tạo Quiz.");
        }

        // Prompt ép AI trả đúng JSON schema để code có thể parse ổn định.
        String prompt = String.format(
                Locale.ROOT,
                QUIZ_PROMPT_TEMPLATE,
                request.getNumberOfQuestions(),
                request.getTopic(),
                request.getLevel()
        );

        // FIX: Sử dụng empty lists để tránh ambiguous method
        // Đây là điểm generator gửi prompt sang network client.
        GeminiApiClient.Result result = geminiClient.generateText(
                prompt,
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                Collections.emptyList(),
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                Collections.emptyList(),
                0.55,
                8192
        );

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (result == null || result.text == null || result.text.trim().isEmpty()) {
            throw new Exception("AI không trả về dữ liệu Quiz.");
        }

        // Tính/đọc và lưu danh sách câu hỏi quiz để sử dụng ở các bước phía sau.
        List<QuizQuestion> questions = parseQuizResponse(result.text);
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (questions.size() != request.getNumberOfQuestions()) {
            Log.w(TAG, "AI returned " + questions.size() + " questions instead of "
                    + request.getNumberOfQuestions());
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (questions.isEmpty()) {
            throw new Exception("Không có câu hỏi hợp lệ trong phản hồi AI.");
        }

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new QuizResult(request, questions, 0, 0);
    }

    /**
     * Parse JSON: questions[] -> prompt/options/correct_answer/explanation/difficulty.
     */
    private List<QuizQuestion> parseQuizResponse(String response) throws Exception {
        // Khởi tạo danh sách câu hỏi quiz phục vụ cho bước xử lý tiếp theo.
        List<QuizQuestion> questions = new ArrayList<>();

        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Tạo/parse JSONObject để làm việc với dữ liệu JSON theo key-value.
            JSONObject root = new JSONObject(extractJson(response));
            // Lấy mảng JSON nếu tồn tại; trả null thay vì ném lỗi khi thiếu key.
            JSONArray questionsArray = root.optJSONArray("questions");
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (questionsArray == null) {
                throw new Exception("Thiếu mảng questions.");
            }

            // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
            for (int i = 0; i < questionsArray.length(); i++) {
                // Tính/đọc và lưu giá trị `item` (item) để sử dụng ở các bước phía sau.
                JSONObject item = questionsArray.optJSONObject(i);
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (item == null) {
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }

                // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                String prompt = item.optString("prompt", "").trim();
                // Lấy mảng JSON nếu tồn tại; trả null thay vì ném lỗi khi thiếu key.
                JSONArray optionsArray = item.optJSONArray("options");
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (prompt.isEmpty() || optionsArray == null || optionsArray.length() != 4) {
                    // Gọi `w()` trên `Log` để thực hiện thao tác tương ứng.
                    Log.w(TAG, "Skipping invalid question at index " + i);
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }

                // Khởi tạo danh sách các lựa chọn trả lời phục vụ cho bước xử lý tiếp theo.
                List<String> options = new ArrayList<>();
                // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
                for (int j = 0; j < 4; j++) {
                    // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                    String option = optionsArray.optString(j, "").trim();
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (option.isEmpty()) {
                        // Xóa dữ liệu cũ trong danh sách để chuẩn bị nạp lại dữ liệu mới.
                        options.clear();
                        // Thoát khỏi vòng lặp hoặc nhánh switch hiện tại.
                        break;
                    }
                    // Thêm phần tử mới vào danh sách đang quản lý trong bộ nhớ.
                    options.add(option);
                }
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (options.size() != 4) {
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }

                // Tính/đọc và lưu đáp án đúng để sử dụng ở các bước phía sau.
                String correctAnswer = normalizeCorrectAnswer(
                        // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                        item.optString("correct_answer", ""),
                        options
                );
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (correctAnswer.isEmpty()) {
                    // Gọi `w()` trên `Log` để thực hiện thao tác tương ứng.
                    Log.w(TAG, "Skipping question with invalid answer at index " + i);
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }

                // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                String explanation = item.optString(
                        "explanation",
                        "Chưa có giải thích."
                ).trim();
                // Tính/đọc và lưu độ khó của câu hỏi để sử dụng ở các bước phía sau.
                QuizDifficulty difficulty = QuizDifficulty.fromApiValue(
                        // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                        item.optString("difficulty", "MEDIUM")
                );

                // Sau bước này JSON thô đã thành object QuizQuestion dùng được trên UI.
            questions.add(new QuizQuestion(
                        QuizQuestion.Type.MULTIPLE_CHOICE,
                        prompt,
                        options,
                        correctAnswer,
                        explanation,
                        difficulty
                ));
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception error) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Không thể parse Quiz AI", error);
            throw new Exception(
                    "Dữ liệu Quiz từ AI không đúng định dạng: " + error.getMessage(),
                    error
            );
        }

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return questions;
    }

    // Hàm `normalizeCorrectAnswer()`: Chuẩn hóa dữ liệu đầu vào về định dạng thống nhất mà module mong đợi.
    private String normalizeCorrectAnswer(String rawAnswer, List<String> options) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (rawAnswer == null) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return "";
        }

        // Tính/đọc và lưu giá trị `normalized` (normalized) để sử dụng ở các bước phía sau.
        String normalized = rawAnswer.trim().toUpperCase(Locale.ROOT);
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (!normalized.isEmpty()) {
            // Tính/đọc và lưu giá trị `first` (first) để sử dụng ở các bước phía sau.
            char first = normalized.charAt(0);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (first >= 'A' && first <= 'D') {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return String.valueOf(first);
            }
        }

        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Tính/đọc và lưu giá trị `number` (number) để sử dụng ở các bước phía sau.
            int number = Integer.parseInt(normalized);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (number >= 1 && number <= 4) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return String.valueOf((char) ('A' + number - 1));
            }
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (number >= 0 && number <= 3) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return String.valueOf((char) ('A' + number));
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (NumberFormatException ignored) {
        }

        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int i = 0; i < options.size(); i++) {
            // Tính/đọc và lưu giá trị `option` (option) để sử dụng ở các bước phía sau.
            String option = options.get(i);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (option.equalsIgnoreCase(rawAnswer.trim())) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return String.valueOf((char) ('A' + i));
            }
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return "";
    }

    // Hàm `extractJson()`: Thực hiện chức năng `extractJson` của class này.
    private String extractJson(String response) throws Exception {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (response == null) {
            throw new Exception("Phản hồi rỗng.");
        }

        // Tính/đọc và lưu giá trị `cleaned` (cleaned) để sử dụng ở các bước phía sau.
        String cleaned = response.trim();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cleaned.startsWith("```")) {
            // Cập nhật giá trị `cleaned` (cleaned) theo trạng thái/kết quả vừa tính được.
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            // Cập nhật giá trị `cleaned` (cleaned) theo trạng thái/kết quả vừa tính được.
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }

        // Tính/đọc và lưu giá trị `start` (start) để sử dụng ở các bước phía sau.
        int start = cleaned.indexOf('{');
        // Tính/đọc và lưu giá trị `end` (end) để sử dụng ở các bước phía sau.
        int end = cleaned.lastIndexOf('}');
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (start < 0 || end <= start) {
            throw new Exception("Không tìm thấy JSON.");
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return cleaned.substring(start, end + 1);
    }
}
