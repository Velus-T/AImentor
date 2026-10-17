# Tiến độ AIMentor — 17/10/2026

Người dùng đã tiếp tục sửa trên máy, không dùng cloud. Backend máy chủ để bàn giao riêng.

## Đã hoàn thiện trong lượt sửa
- Bốn môn dùng chung tạo quiz 5/10/15 câu, bố cục làm bài/kết quả và chính sách EXP/cấp độ.
- Ngân hàng đề theo tài khoản, phân loại, tìm kiếm/lọc và ôn offline; lưu nguyên đề trong giao dịch và chống trùng bộ đề.
- Câu điền/ghép giữ ngữ nghĩa dấu âm, thập phân và loại câu hỏi khi lưu/mở lại.
- Lịch sử bốn môn dùng bố cục, tìm kiếm không dấu/toàn nội dung và nhóm/bộ lọc ngày chung; giữ mô hình hội thoại/câu hỏi thật.
- Công thức KaTeX đóng gói offline; sửa doctype HTML để công thức thực sự render khi mất mạng.
- Android 13+ dùng LocaleManager qua context, xử lý lựa chọn trước khi có Activity và đồng bộ lựa chọn ngôn ngữ của hệ thống.
- Bàn phím nhường vùng hiển thị; giữ bản nháp khi AI chưa cấu hình hoặc lưu ghi chú thất bại.
- Giữ máy tính bong bóng chỉ ở Toán, timer và công cụ mạch điện Science.

## Kiểm chứng mới nhất
- resumed-fixes-verification.log: assembleDebug, assembleDebugAndroidTest, testDebugUnitTest, lintDebug toàn project PASS; 84 unit test, 0 thất bại, 0 lỗi.
- resumed-locale-build-verification.log: debug, release và APK kiểm thử PASS sau bản sửa LocaleManager cuối.
- resumed-locale-lint-verification.log: lintDebug app PASS sau sửa LocaleManager; cảnh báo lint cũ vẫn còn.
- resumed-target-clean-runtime-verification.log: 2/2 PASS — máy tính xoay ngang và đổi ngôn ngữ/theme giữ bài, không cộng EXP lặp.
- resumed-ime-runtime-verification.log: 2/2 PASS với bàn phím thật, kiểm tra giữ câu nhập và lịch sử cả bốn môn.
- resumed-full-compact-runtime-verification.log: PASS 38/38 ca trên màn hình 320dp, font 1.3, điều hướng ba nút, offline (155,77 giây).
- Lượt chạy target đầu bị System UI ANR của giả lập che app; đã đóng hộp thoại và chạy lại thành công. Không coi kết quả đó là lỗi AIMentor.

## Giới hạn và bàn giao
API key vẫn trống; chưa gọi Gemini thật. Science mẫu cục bộ chưa đủ 10/15 câu từng nhóm, nên báo thiếu và cho chọn 5 hoặc đề đã lưu. Bank hiện ở SQLite, chưa đồng bộ máy chủ. Chưa xác nhận trên điện thoại thật/tablet. Chống nộp lặp xuyên sự cố chết tiến trình cần idempotency phía backend. Bộ đề Toán cũ owner 0 được bảo toàn, chưa tự gán người dùng.

Chi tiết chức năng và danh sách backend: QUIZ_AND_HISTORY_REPORT.md. Nghiên cứu UX và nguồn tham khảo: UX_RESEARCH.md. Bản APK bàn giao: ../AIMentor_QA/AImentor-debug.apk (đã cập nhật từ bản build đã kiểm tra).

## Git
Remote được fetch lại ngày 17/10/2026, origin/master vẫn a0e190a. Nhánh làm việc codex/quiz-bank-unification; bản sửa 6ba21e7 đã đẩy fast-forward lên origin/master; không force push.

## Rà soát logic tiếp theo — 17/10/2026

Người dùng yêu cầu kiểm tra logic toàn bộ app. Chi tiết: LOGIC_AUDIT_2026-10-17.md.

- Đã sửa các nhánh parser/đáp án AI, false-save success, khôi phục chat/ghi chú/tab, xác nhận rời quiz, mã phân loại Science, giới hạn ảnh Toán và kiểm tra reminder.
- logic-audit-final-build-verification.log: debug, release, APK kiểm thử, 97 unit test và lintDebug toàn project PASS; 0 lỗi lint, còn 476 cảnh báo.
- logic-audit-new-cases-device-verification.log: 10/10 ca mới PASS (24,529 giây), gồm parse/service/bank, repository lỗi, recreate, retry và xác nhận rời quiz.
- logic-audit-all-compact-device-verification.log: PASS 48/48 ca (155,186 giây) ở 320dp, font 1.3, điều hướng ba nút, tối, offline.
- Mốc Git đầu lượt: 7566cfe, remote chưa có thay đổi mới. Lượt audit đã hoàn tất kiểm thử; bản sửa và báo cáo được lưu trong commit tiếp theo của nhánh, cập nhật origin/master bằng fast-forward.
- API key vẫn trống; không gọi Gemini thật, không thêm backend máy chủ.

APK bàn giao ../AIMentor_QA/AImentor-debug.apk đã cập nhật từ bản build cuối; SHA256 7799FD7B0D43E4BA66FDE2C7B4663486191FAC6E0814881BC1C15BDAE0265E70.
