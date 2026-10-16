package com.example.modulelaptrinh.utils;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GEMINI API CLIENT DÙNG CHUNG CHO PROGRAMMING MODULE.
 *
 * <p>File này được gộp từ 2 client cũ: ChatGeminiApiClient và QuizGeminiApiClient.
 * Hai client cũ gần như giống nhau hoàn toàn, chỉ khác thứ tự ưu tiên API key. Gộp lại giúp
 * giáo viên nhìn luồng network dễ hơn và tránh phải bảo trì cùng một đoạn HTTP code ở hai nơi.</p>
 *
 * <p>Luồng xử lý chính:</p>
 * <pre>
 * ProgrammingChatActivity / ProgrammingQuizGenerator
 *              -> GeminiApiClient.generateText(...)
 *              -> buildRequest(...) tạo JSON
 *              -> HttpURLConnection gửi POST tới Gemini
 *              -> parseFullResponse(...) lấy text + token
 * </pre>
 */
public final class GeminiApiClient {

    /**
     * Hai mục đích sử dụng chung một client nhưng vẫn giữ đúng thứ tự API key của code cũ.
     */
    public enum Usage {
        CHAT,
        QUIZ
    }

    // API keys are supplied by local, untracked configuration.
    // The existing chat/quiz key priority and HTTP implementation are unchanged.
    private static final String API_KEY_1 = com.example.modulelaptrinh.BuildConfig.PROGRAMMING_GEMINI_API_KEY_1;
    private static final String API_KEY_2 = com.example.modulelaptrinh.BuildConfig.PROGRAMMING_GEMINI_API_KEY_2;
    private static final String API_KEY_3 = com.example.modulelaptrinh.BuildConfig.PROGRAMMING_GEMINI_API_KEY_3;

    // Chat cũ ưu tiên 1 -> 2 -> 3; Quiz cũ ưu tiên 3 -> 1 -> 2.
    private static final KeyPool CHAT_KEY_POOL = new KeyPool(API_KEY_1, API_KEY_2, API_KEY_3);
    // Khởi tạo giá trị `QUIZ_KEY_POOL` (quiz key pool) phục vụ cho bước xử lý tiếp theo.
    private static final KeyPool QUIZ_KEY_POOL = new KeyPool(API_KEY_3, API_KEY_1, API_KEY_2);

    private static final int CONNECT_TIMEOUT_MS = 30_000;
    private static final int READ_TIMEOUT_MS = 120_000;
    // Tính/đọc và lưu giá trị `MAX_ATTEMPTS` (max attempts) để sử dụng ở các bước phía sau.
    private static final int MAX_ATTEMPTS = 6;

    // Mỗi instance giữ model/status riêng để Chat và Quiz không ảnh hưởng trạng thái lẫn nhau.
    private String currentModel = com.example.modulelaptrinh.BuildConfig.PROGRAMMING_GEMINI_MODEL;
    // Tính/đọc và lưu giá trị `currentStatus` (current status) để sử dụng ở các bước phía sau.
    private String currentStatus = "GOOD";

    // Thuộc tính `keyPool` lưu đối tượng `KeyPool` mà class cần dùng xuyên suốt nhiều hàm.
    private final KeyPool keyPool;

    /**
     * Tạo client theo đúng nhu cầu Chat hoặc Quiz.
     */
    public GeminiApiClient(Usage usage) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `keyPool` của đối tượng hiện tại.
        this.keyPool = usage == Usage.QUIZ ? QUIZ_KEY_POOL : CHAT_KEY_POOL;
    }

    /** Kết quả trả về sau khi parse response Gemini. */
    public static final class Result {
        public final String text;
        public final int promptTokens;
        public final int candidateTokens;
        public final int totalTokens;
        public final String status;
        public final String finishReason;

        // Constructor `Result`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
        public Result(String text, int promptTokens, int candidateTokens, int totalTokens,
                      String status, String finishReason) {
            // Gán giá trị tham số/đầu vào vào thuộc tính `text` của đối tượng hiện tại.
            this.text = text == null ? "" : text.trim();
            // Gán giá trị tham số/đầu vào vào thuộc tính `promptTokens` của đối tượng hiện tại.
            this.promptTokens = Math.max(0, promptTokens);
            // Gán giá trị tham số/đầu vào vào thuộc tính `candidateTokens` của đối tượng hiện tại.
            this.candidateTokens = Math.max(0, candidateTokens);
            // Gán giá trị tham số/đầu vào vào thuộc tính `totalTokens` của đối tượng hiện tại.
            this.totalTokens = Math.max(0, totalTokens);
            // Gán giá trị tham số/đầu vào vào thuộc tính `status` của đối tượng hiện tại.
            this.status = status == null ? "" : status.trim();
            // Gán giá trị tham số/đầu vào vào thuộc tính `finishReason` của đối tượng hiện tại.
            this.finishReason = finishReason == null ? "" : finishReason.trim();
        }

        /** Gemini trả MAX_TOKENS nghĩa là câu trả lời có khả năng bị cắt giữa chừng. */
        public boolean wasTruncated() {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return "MAX_TOKENS".equalsIgnoreCase(finishReason);
        }

        @Override
        // Hàm `toString()`: Thực hiện chức năng `toString` của class này.
        public String toString() {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return text;
        }
    }

    /**
     * Exception riêng cho lỗi HTTP/API để UI có thể biết status code (401, 429, 5xx...).
     * Được gộp từ file GeminiApiException.java cũ để giảm một file quá ngắn.
     */
    public static final class ApiException extends Exception {
        private final int httpStatus;
        private final String apiStatus;

        // Constructor `ApiException`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
        public ApiException(int httpStatus, String apiStatus, String message) {
            // Gọi hàm `super()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            super(message == null ? "" : message);
            // Gán giá trị tham số/đầu vào vào thuộc tính `httpStatus` của đối tượng hiện tại.
            this.httpStatus = httpStatus;
            // Gán giá trị tham số/đầu vào vào thuộc tính `apiStatus` của đối tượng hiện tại.
            this.apiStatus = apiStatus == null ? "" : apiStatus;
        }

        // Hàm `getHttpStatus()`: Trả về giá trị HttpStatus hiện đang được đối tượng lưu giữ.
        public int getHttpStatus() {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return httpStatus;
        }

        // Hàm `getApiStatus()`: Trả về giá trị ApiStatus hiện đang được đối tượng lưu giữ.
        public String getApiStatus() {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return apiStatus;
        }
    }

    // ===== CẤU HÌNH MODEL =====
    // Hàm `getModelName()`: Trả về giá trị ModelName hiện đang được đối tượng lưu giữ.
    public String getModelName() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return currentModel;
    }

    // Hàm `setModelName()`: Cập nhật giá trị ModelName cho đối tượng hiện tại.
    public void setModelName(String modelName) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (modelName != null && !modelName.trim().isEmpty()) {
            // Cập nhật giá trị `currentModel` (current model) theo trạng thái/kết quả vừa tính được.
            currentModel = modelName.trim();
        }
    }

    // Hàm `getStatus()`: Trả về giá trị Status hiện đang được đối tượng lưu giữ.
    public String getStatus() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return currentStatus;
    }

    /** Cho biết key thứ mấy đang được ưu tiên; không trả trực tiếp giá trị API key. */
    public String getActiveKeyLabel() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return keyPool.getActiveKeyLabel();
    }

    // Hàm `isConfigured()`: Kiểm tra trạng thái/điều kiện và trả về true hoặc false.
    public boolean isConfigured() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return keyPool.size() > 0;
    }

    /** Kiểm tra nhanh kết nối Gemini bằng một prompt rất nhỏ. */
    public Result testConnection() throws Exception {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return generateText(
                "Reply with exactly: OK",
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                Collections.emptyList(),
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                Collections.emptyList(),
                0.0,
                32
        );
    }

    /** Overload tiện lợi cho trường hợp chỉ có tối đa một ảnh. */
    public Result generateText(String prompt, byte[] imageBytes, String imageMimeType,
                               double temperature, int maxOutputTokens) throws Exception {
        // Tính/đọc và lưu giá trị `images` (images) để sử dụng ở các bước phía sau.
        List<byte[]> images = imageBytes == null
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                ? Collections.emptyList()
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                : Collections.singletonList(imageBytes);
        // Tính/đọc và lưu giá trị `mimeTypes` (mime types) để sử dụng ở các bước phía sau.
        List<String> mimeTypes = imageBytes == null
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                ? Collections.emptyList()
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                : Collections.singletonList(imageMimeType);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return generateText(prompt, images, mimeTypes, temperature, maxOutputTokens);
    }

    /**
     * HÀM NETWORK CHÍNH.
     *
     * <p>Đây là hàm nên chỉ cho giáo viên khi được hỏi "đoạn code nào gọi AI?".</p>
     * <ol>
     *     <li>Kiểm tra prompt và API key.</li>
     *     <li>Chọn key đang khả dụng.</li>
     *     <li>Tạo HttpURLConnection và request JSON.</li>
     *     <li>Gửi POST.</li>
     *     <li>Đọc response; lỗi quota thì đổi key và retry.</li>
     *     <li>Response thành công được parse thành Result.</li>
     * </ol>
     */
    public Result generateText(String prompt, List<byte[]> imageBytesList,
                               List<String> imageMimeTypes, double temperature,
                               int maxOutputTokens) throws Exception {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (!isConfigured()) {
            throw new IllegalStateException("No Gemini API key is configured.");
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty.");
        }

        // Tính/đọc và lưu giá trị `safeImages` (safe images) để sử dụng ở các bước phía sau.
        List<byte[]> safeImages = imageBytesList == null
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                ? Collections.emptyList()
                : imageBytesList;
        // Tính/đọc và lưu giá trị `safeMimeTypes` (safe mime types) để sử dụng ở các bước phía sau.
        List<String> safeMimeTypes = imageMimeTypes == null
                // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                ? Collections.emptyList()
                : imageMimeTypes;

        // Tính/đọc và lưu giá trị `lastError` (last error) để sử dụng ở các bước phía sau.
        Exception lastError = null;
        // Tính/đọc và lưu giá trị `maximumAttempts` (maximum attempts) để sử dụng ở các bước phía sau.
        int maximumAttempts = Math.max(MAX_ATTEMPTS, keyPool.size() * 2);

        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int attempt = 0; attempt < maximumAttempts; attempt++) {
            // Chọn API key chưa bị cooldown / vô hiệu hóa.
            int keyIndex = keyPool.nextAvailableKeyIndex();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (keyIndex < 0) {
                // Tính/đọc và lưu giá trị `wait` (wait) để sử dụng ở các bước phía sau.
                long wait = keyPool.shortestCooldown();
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (wait > 0 && wait <= 5_000L) {
                    // Gọi `sleep()` trên `Thread` để thực hiện thao tác tương ứng.
                    Thread.sleep(wait);
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }
                // Thoát khỏi vòng lặp hoặc nhánh switch hiện tại.
                break;
            }

            // Tính/đọc và lưu giá trị `connection` (connection) để sử dụng ở các bước phía sau.
            HttpURLConnection connection = null;
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Endpoint Gemini generateContent. Model hiện tại mặc định là gemini-flash-latest.
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                        + currentModel + ":generateContent";
                // Tạo URL đích của Gemini API từ endpoint đã cấu hình.
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                // Đặt HTTP method cho request; Gemini generateContent sử dụng POST.
                connection.setRequestMethod("POST");
                // Thiết lập thời gian timeout để request không chờ vô hạn khi mạng/API gặp vấn đề.
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                // Thiết lập thời gian timeout để request không chờ vô hạn khi mạng/API gặp vấn đề.
                connection.setReadTimeout(READ_TIMEOUT_MS);
                // Gọi `setDoOutput()` để cập nhật thuộc tính/trạng thái của `connection`.
                connection.setDoOutput(true);
                // Thiết lập HTTP header cần thiết cho request gửi lên API.
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                // Thiết lập HTTP header cần thiết cho request gửi lên API.
                connection.setRequestProperty("Accept", "application/json");
                // Thiết lập HTTP header cần thiết cho request gửi lên API.
                connection.setRequestProperty("x-goog-api-key", keyPool.getKey(keyIndex));

                // buildRequest() đóng gói text + ảnh + generationConfig thành JSON đúng format Gemini.
                JSONObject request = buildRequest(
                        prompt.trim(),
                        safeImages,
                        safeMimeTypes,
                        temperature,
                        maxOutputTokens
                );
                // Tính/đọc và lưu giá trị `requestBytes` (request bytes) để sử dụng ở các bước phía sau.
                byte[] requestBytes = request.toString().getBytes(StandardCharsets.UTF_8);
                // Gọi `setFixedLengthStreamingMode()` để cập nhật thuộc tính/trạng thái của `connection`.
                connection.setFixedLengthStreamingMode(requestBytes.length);

                // Đây là đoạn dữ liệu request thực sự được ghi ra network.
                try (OutputStream output = connection.getOutputStream()) {
                    // Ghi dữ liệu request đã mã hóa thành byte lên HTTP connection.
                    output.write(requestBytes);
                }

                // Đọc HTTP status code để xác định request thành công hay thất bại.
                int statusCode = connection.getResponseCode();
                // Tính/đọc và lưu nội dung phản hồi HTTP để sử dụng ở các bước phía sau.
                String responseBody = readResponse(connection, statusCode);

                // HTTP 2xx = Gemini trả thành công.
                if (statusCode >= 200 && statusCode < 300) {
                    // Gọi `markSuccess()` trên `keyPool` để thực hiện thao tác tương ứng.
                    keyPool.markSuccess(keyIndex);
                    // Cập nhật giá trị `currentStatus` (current status) theo trạng thái/kết quả vừa tính được.
                    currentStatus = attempt == 0 ? "GOOD" : "MEDIUM";
                    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                    return parseFullResponse(responseBody);
                }

                // Tính/đọc và lưu giá trị `apiError` (api error) để sử dụng ở các bước phía sau.
                ApiException apiError = createApiException(statusCode, responseBody);
                // Cập nhật giá trị `lastError` (last error) theo trạng thái/kết quả vừa tính được.
                lastError = apiError;
                // Cập nhật giá trị `currentStatus` (current status) theo trạng thái/kết quả vừa tính được.
                currentStatus = "BUSY";

                // 401/403: key không hợp lệ hoặc không có quyền -> bỏ key đó.
                if (statusCode == 401 || statusCode == 403) {
                    // Gọi `disableKey()` trên `keyPool` để thực hiện thao tác tương ứng.
                    keyPool.disableKey(keyIndex, Long.MAX_VALUE);
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }

                // 429: quota/rate limit -> nghỉ key 60 giây rồi thử key khác.
                if (statusCode == 429) {
                    // Gọi `disableKey()` trên `keyPool` để thực hiện thao tác tương ứng.
                    keyPool.disableKey(keyIndex, System.currentTimeMillis() + 60_000L);
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }

                // 5xx: lỗi phía server -> retry với backoff ngắn.
                if (statusCode >= 500 && statusCode <= 599) {
                    // Gọi `sleep()` trên `Thread` để thực hiện thao tác tương ứng.
                    Thread.sleep(Math.min(8_000L, 1_000L * (attempt + 1L)));
                    // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                    continue;
                }

                throw apiError;
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (IOException networkError) {
                // Lỗi mạng cũng được retry một số lần trước khi trả lỗi về UI.
                lastError = networkError;
                // Cập nhật giá trị `currentStatus` (current status) theo trạng thái/kết quả vừa tính được.
                currentStatus = "BUSY";
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (attempt + 1 < maximumAttempts) {
                    // Gọi `sleep()` trên `Thread` để thực hiện thao tác tương ứng.
                    Thread.sleep(Math.min(4_000L, 750L * (attempt + 1L)));
                }
            // Khối finally luôn chạy để giải phóng/hoàn tất tài nguyên cần thiết.
            } finally {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (connection != null) {
                    // Gọi `disconnect()` trên `connection` để thực hiện thao tác tương ứng.
                    connection.disconnect();
                }
            }
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (lastError instanceof ApiException) {
            throw lastError;
        }
        throw new IllegalStateException(
                "Gemini request failed after multiple attempts.",
                lastError
        );
    }

    /**
     * Parse JSON response của Gemini thành text và token usage.
     */
    private Result parseFullResponse(String responseBody) throws Exception {
        // Tạo/parse JSONObject để làm việc với dữ liệu JSON theo key-value.
        JSONObject root = new JSONObject(responseBody);
        // Khởi tạo giá trị `answer` (answer) phục vụ cho bước xử lý tiếp theo.
        StringBuilder answer = new StringBuilder();
        // Tính/đọc và lưu giá trị `finishReason` (finish reason) để sử dụng ở các bước phía sau.
        String finishReason = "";

        // Lấy mảng JSON nếu tồn tại; trả null thay vì ném lỗi khi thiếu key.
        JSONArray candidates = root.optJSONArray("candidates");
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (candidates != null && candidates.length() > 0) {
            // Tính/đọc và lưu giá trị `firstCandidate` (first candidate) để sử dụng ở các bước phía sau.
            JSONObject firstCandidate = candidates.optJSONObject(0);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (firstCandidate != null) {
                // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                finishReason = firstCandidate.optString("finishReason", "");
                // Tính/đọc và lưu nội dung văn bản để sử dụng ở các bước phía sau.
                JSONObject content = firstCandidate.optJSONObject("content");
                // Lấy mảng JSON nếu tồn tại; trả null thay vì ném lỗi khi thiếu key.
                JSONArray parts = content == null ? null : content.optJSONArray("parts");
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (parts != null) {
                    // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
                    for (int i = 0; i < parts.length(); i++) {
                        // Tính/đọc và lưu giá trị `part` (part) để sử dụng ở các bước phía sau.
                        JSONObject part = parts.optJSONObject(i);
                        // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                        String text = part == null ? "" : part.optString("text", "");
                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (!text.isEmpty()) {
                            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                            if (answer.length() > 0) {
                                // Gọi `append()` trên `answer` để thực hiện thao tác tương ứng.
                                answer.append('\n');
                            }
                            // Gọi `append()` trên `answer` để thực hiện thao tác tương ứng.
                            answer.append(text);
                        }
                    }
                }
            }
        }

        // Tính/đọc và lưu giá trị `text` (text) để sử dụng ở các bước phía sau.
        String text = answer.toString().trim();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (text.isEmpty()) {
            // Tính/đọc và lưu giá trị `feedback` (feedback) để sử dụng ở các bước phía sau.
            JSONObject feedback = root.optJSONObject("promptFeedback");
            // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
            String reason = feedback == null ? "" : feedback.optString("blockReason", "");
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (!reason.isEmpty()) {
                throw new IllegalStateException("Gemini blocked the request: " + reason);
            }
            throw new IllegalStateException("Gemini returned an empty answer.");
        }

        // Tính/đọc và lưu giá trị `usage` (usage) để sử dụng ở các bước phía sau.
        JSONObject usage = root.optJSONObject("usageMetadata");
        // Tính/đọc và lưu prompt gửi tới Gemini để sử dụng ở các bước phía sau.
        int prompt = usage == null ? 0 : usage.optInt("promptTokenCount", 0);
        // Tính/đọc và lưu giá trị `candidate` (candidate) để sử dụng ở các bước phía sau.
        int candidate = usage == null ? 0 : usage.optInt("candidatesTokenCount", 0);
        // Tính/đọc và lưu giá trị `total` (total) để sử dụng ở các bước phía sau.
        int total = usage == null ? 0 : usage.optInt("totalTokenCount", 0);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new Result(text, prompt, candidate, total, currentStatus, finishReason);
    }

    /**
     * Tạo body JSON gửi Gemini. Nếu có ảnh, ảnh được Base64 và đặt vào inline_data.
     */
    private JSONObject buildRequest(String prompt, List<byte[]> imageBytesList,
                                    List<String> imageMimeTypes, double temperature,
                                    int maxOutputTokens) throws Exception {
        // Tạo/parse JSONArray để làm việc với danh sách dữ liệu JSON.
        JSONArray parts = new JSONArray();

        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int i = 0; i < imageBytesList.size(); i++) {
            // Tính/đọc và lưu giá trị `image` (image) để sử dụng ở các bước phía sau.
            byte[] image = imageBytesList.get(i);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (image == null || image.length == 0) {
                // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                continue;
            }
            // Tính/đọc và lưu giá trị `mimeType` (mime type) để sử dụng ở các bước phía sau.
            String mimeType = i < imageMimeTypes.size() ? imageMimeTypes.get(i) : null;
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (mimeType == null || mimeType.trim().isEmpty()) {
                // Cập nhật giá trị `mimeType` (mime type) theo trạng thái/kết quả vừa tính được.
                mimeType = "image/jpeg";
            }

            // Tạo/parse JSONObject để làm việc với dữ liệu JSON theo key-value.
            JSONObject inlineData = new JSONObject()
                    .put("mime_type", mimeType.trim())
                    // Mã hóa dữ liệu ảnh thành Base64 để có thể gửi qua JSON hoặc lưu trong cột TEXT.
                    .put("data", Base64.encodeToString(image, Base64.NO_WRAP));
            // Tạo/parse JSONObject để làm việc với dữ liệu JSON theo key-value.
            parts.put(new JSONObject().put("inline_data", inlineData));
        }

        // Text prompt luôn được thêm sau phần ảnh.
        parts.put(new JSONObject().put("text", prompt));

        // Tạo/parse JSONObject để làm việc với dữ liệu JSON theo key-value.
        JSONObject userContent = new JSONObject()
                .put("role", "user")
                .put("parts", parts);
        // Tạo/parse JSONObject để làm việc với dữ liệu JSON theo key-value.
        JSONObject generationConfig = new JSONObject()
                .put("temperature", Math.max(0.0, Math.min(1.0, temperature)))
                .put("maxOutputTokens", Math.max(32, Math.min(8192, maxOutputTokens)));

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new JSONObject()
                // Tạo/parse JSONArray để làm việc với danh sách dữ liệu JSON.
                .put("contents", new JSONArray().put(userContent))
                .put("generationConfig", generationConfig);
    }

    /** Đọc InputStream khi thành công hoặc ErrorStream khi HTTP lỗi. */
    private String readResponse(HttpURLConnection connection, int statusCode) throws Exception {
        // Tính/đọc và lưu giá trị `stream` (stream) để sử dụng ở các bước phía sau.
        InputStream stream = statusCode >= 200 && statusCode < 300
                // Mở stream phản hồi tương ứng với trạng thái thành công hoặc lỗi của HTTP request.
                ? connection.getInputStream()
                // Mở stream phản hồi tương ứng với trạng thái thành công hoặc lỗi của HTTP request.
                : connection.getErrorStream();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (stream == null) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            // Khởi tạo kết quả trả về sau khi xử lý phục vụ cho bước xử lý tiếp theo.
            StringBuilder result = new StringBuilder();
            String line;
            // Tiếp tục lặp khi điều kiện vẫn còn đúng.
            while ((line = reader.readLine()) != null) {
                // Gọi `append()` trên `result` để thực hiện thao tác tương ứng.
                result.append(line);
            }
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return result.toString();
        }
    }

    /** Chuyển JSON error của Gemini thành exception có HTTP status rõ ràng. */
    private ApiException createApiException(int statusCode, String responseBody) {
        // Tính/đọc và lưu đối tượng/nội dung tin nhắn để sử dụng ở các bước phía sau.
        String message = responseBody == null ? "" : responseBody.trim();
        // Tính/đọc và lưu giá trị `apiStatus` (api status) để sử dụng ở các bước phía sau.
        String apiStatus = "";
        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Tạo/parse JSONObject để làm việc với dữ liệu JSON theo key-value.
            JSONObject error = new JSONObject(message).optJSONObject("error");
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (error != null) {
                // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                message = error.optString("message", message);
                // Đọc chuỗi từ JSON bằng optString để tránh exception khi key không tồn tại.
                apiStatus = error.optString("status", "");
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception ignored) {
            // Nếu body không phải JSON thì giữ nguyên message gốc để debug.
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new ApiException(statusCode, apiStatus, message);
    }

    /**
     * Quản lý một nhóm API key: key hiện tại, cooldown và xoay key khi lỗi quota.
     * Mỗi Usage (CHAT/QUIZ) có KeyPool riêng để giữ đúng hành vi code cũ.
     */
    private static final class KeyPool {
        private final String[] keys;
        private final long[] disabledUntil;
        private int currentKeyIndex;

        KeyPool(String... configuredKeys) {
            // Khởi tạo tập hợp cặp cột-giá trị dùng cho SQLite phục vụ cho bước xử lý tiếp theo.
            List<String> values = new ArrayList<>();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (configuredKeys != null) {
                // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
                for (String key : configuredKeys) {
                    // Gọi hàm `addUniqueKey()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    addUniqueKey(values, key);
                }
            }
            // Cập nhật giá trị `keys` (keys) theo trạng thái/kết quả vừa tính được.
            keys = values.toArray(new String[0]);
            // Cập nhật giá trị `disabledUntil` (disabled until) theo trạng thái/kết quả vừa tính được.
            disabledUntil = new long[keys.length];
        }

        synchronized int size() {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return keys.length;
        }

        synchronized String getKey(int index) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return keys[index];
        }

        synchronized String getActiveKeyLabel() {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return keys.length == 0
                    ? "no key"
                    : "key " + (currentKeyIndex + 1) + "/" + keys.length;
        }

        synchronized int nextAvailableKeyIndex() {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (keys.length == 0) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return -1;
            }
            // Tính/đọc và lưu giá trị `now` (now) để sử dụng ở các bước phía sau.
            long now = System.currentTimeMillis();
            // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
            for (int offset = 0; offset < keys.length; offset++) {
                // Tính/đọc và lưu giá trị `index` (index) để sử dụng ở các bước phía sau.
                int index = (currentKeyIndex + offset) % keys.length;
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (disabledUntil[index] <= now) {
                    // Cập nhật giá trị `currentKeyIndex` (current key index) theo trạng thái/kết quả vừa tính được.
                    currentKeyIndex = index;
                    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                    return index;
                }
            }
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return -1;
        }

        synchronized void markSuccess(int index) {
            // Cập nhật giá trị `currentKeyIndex` (current key index) theo trạng thái/kết quả vừa tính được.
            currentKeyIndex = index;
            disabledUntil[index] = 0L;
        }

        synchronized void disableKey(int index, long until) {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (index < 0 || index >= disabledUntil.length) {
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }
            disabledUntil[index] = until;
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (keys.length > 0) {
                // Cập nhật giá trị `currentKeyIndex` (current key index) theo trạng thái/kết quả vừa tính được.
                currentKeyIndex = (index + 1) % keys.length;
            }
        }

        synchronized long shortestCooldown() {
            // Tính/đọc và lưu giá trị `now` (now) để sử dụng ở các bước phía sau.
            long now = System.currentTimeMillis();
            // Tính/đọc và lưu giá trị `shortest` (shortest) để sử dụng ở các bước phía sau.
            long shortest = Long.MAX_VALUE;
            // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
            for (long value : disabledUntil) {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (value > now && value != Long.MAX_VALUE) {
                    // Cập nhật giá trị `shortest` (shortest) theo trạng thái/kết quả vừa tính được.
                    shortest = Math.min(shortest, value - now);
                }
            }
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return shortest == Long.MAX_VALUE ? -1L : shortest;
        }

        // Hàm `addUniqueKey()`: Thêm dữ liệu/phần tử mới vào trạng thái hoặc danh sách hiện tại.
        private static void addUniqueKey(List<String> keys, String value) {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (value == null) {
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }
            // Tính/đọc và lưu giá trị `key` (key) để sử dụng ở các bước phía sau.
            String key = value.trim();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (!key.isEmpty() && !keys.contains(key)) {
                // Thêm phần tử mới vào danh sách đang quản lý trong bộ nhớ.
                keys.add(key);
            }
        }
    }
}
