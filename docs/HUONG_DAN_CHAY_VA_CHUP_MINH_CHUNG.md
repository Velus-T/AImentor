# Hướng dẫn chạy và chụp minh chứng — Science Module

## 1. Phạm vi project

Bản này chỉ bổ sung module `com.example.aimentor.science`. Không sửa logic Login, Registration, Menu, Home, Category hoặc module của thành viên khác.

Các file chung bắt buộc phải có thay đổi nhỏ:

- `AndroidManifest.xml`: đăng ký Activity, Camera/Internet và FileProvider cho Science.
- Gradle version catalog/build file: thêm RecyclerView cho giao diện chat.

## 2. Mở project

1. Giải nén file ZIP.
2. Mở thư mục gốc `AI-Mentor-Science-Reviewed` bằng Android Studio.
3. Chờ Gradle Sync.
4. Cài Android SDK 36 nếu IDE yêu cầu.
5. Chạy trên thiết bị/emulator API 24 trở lên.

## 3. Nối module Science vào giao diện chung

Để không sửa code của thành viên khác, bản bàn giao không tự thay đổi `CategoryFragment` hay `MenuActivity`.

Thành viên quản lý màn cần mở Science thêm một trong các đoạn ở:

`docs/SCIENCE_INTEGRATION_SNIPPET.md`

Đích cần mở là:

```java
ScienceActivity.class
```

## 4. Nhập Gemini API ngay trong app

Không sửa `local.properties` và không hard-code key.

1. Mở Science.
2. Nhấn **Settings · API needed**.
3. Dán Gemini API key.
4. Giữ model mặc định hoặc nhập model hợp lệ.
5. Nhấn **Test connection**.
6. Nhấn **Save**.

Key được mã hóa bằng Android Keystore. Tuy nhiên, đây vẫn là phương án coursework/demo. Bản production nên gọi AI qua backend và dùng key đã giới hạn quyền.

Không có key hoặc mất mạng:

- Chat dùng câu trả lời fallback cục bộ.
- Quiz dùng ngân hàng câu hỏi theo môn/cấp học và vẫn tạo biến thể.
- Study Planner dùng kế hoạch cục bộ.
- Phân tích nội dung ảnh bằng AI cần kết nối remote.

## 5. Luồng demo nên quay/chụp

### Chat AI

1. Màn ChatGPT-style với nền động nhẹ.
2. Chuyển Physics → Chemistry → Biology và chứng minh mỗi tab giữ hội thoại riêng.
3. Hỏi tiếp kiểu “giải thích lại bước 2”.
4. Upload ảnh từ thư viện.
5. Chụp ảnh bằng camera.
6. Copy và Bookmark câu trả lời.
7. Chemistry hiển thị công thức có chỉ số dưới/trên.

### API Settings

1. Màn nhập key/model.
2. Test Connection.
3. Save và quay lại Chat thấy trạng thái `API ✓`.
4. Remove key và chứng minh fallback vẫn chạy.

### Adaptive Quiz

1. Chọn môn, cấp học, chủ đề, 3–5 câu.
2. Tạo hai bộ liên tiếp để chứng minh câu thay đổi.
3. Làm điểm thấp rồi tạo lại: Support mode.
4. Làm điểm cao rồi tạo lại: Challenge mode.
5. Chứng minh cấp học không bị đổi.
6. Xoay màn hình để chứng minh đáp án không mất.

### AI Study Planner

1. Hiển thị giờ hiện tại trước khi nhấn Generate.
2. Nhấn Generate và chụp `created` + `First session` cùng thời điểm.
3. Hiển thị các ngày tiếp theo cùng giờ.
4. Tick hoàn thành, đóng/mở lại để chứng minh lưu dữ liệu.
5. Tắt mạng hoặc xóa key để chứng minh local fallback.

### Các chức năng Science còn lại

- History: tìm kiếm và lọc Lý/Hóa/Sinh.
- Bookmark.
- Circuit Diagram.
- Periodic Table đủ 118 nguyên tố.
- Science Progress.
- Light/Dark Mode với checkbox/radio/spinner tương phản rõ.
- Battery Saver hoặc tắt Animation để chứng minh nền động dừng.

## 6. Minh chứng SOLID

Chụp các package/classes:

- `domain/service/ScienceAnswerService`
- `domain/service/GenerativeAiClient`
- `domain/service/ScienceTutorPromptStrategy`
- `domain/policy/AdaptiveQuizPolicy`
- `domain/usecase/*`
- `data/remote/GeminiApiClient`
- `data/remote/ResilientScienceService`
- `data/repository/*`
- `di/ScienceModule`

## 7. Chạy kiểm thử

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Sau đó chạy thủ công toàn bộ `docs/TEST_PLAN.md` trên thiết bị/emulator và ghi kết quả thật. Không điền giả survey hoặc kết quả test.
