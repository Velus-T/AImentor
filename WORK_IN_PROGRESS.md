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


## API cũ — kiểm thử thật hoàn tất 17/10/2026

Người dùng cho phép thử lại API cũ. Chi tiết LIVE_API_TEST_2026-10-17.md.
- 7/7 khóa cũ gọi danh sách model HTTP 200.
- live-old-api-runtime-verification.log: 8/8 ca Gemini thật PASS (181,603 giây): chat bốn môn; quiz Toán/Science/Lập trình 5 câu và Lịch sử 15 câu, đều lưu bank.
- Science tạm dùng pool khóa Toán vì ZIP không có khóa riêng.
- Test chỉ chạy khi mentorLiveAi=true; mặc định 8 ca được bỏ qua (đã xác nhận), không gọi mạng.
- Đã phục hồi API công khai trống và BuildConfig bốn module trống; đã cài lại APK trống, xóa APK chứa khóa và đóng giả lập.
- Chưa thử ảnh, các lỗi mạng/quota cưỡng bức hoặc kiểm duyệt toàn bộ kiến thức AI.

## Hoàn tất streak quiz, bỏ thời gian và nghiên cứu mã nguồn mở — 17/10/2026

- Streak chỉ tính quiz đã trả lời đầy đủ, nộp/lưu thành công; chat không được tính. Cả bốn môn và ôn offline dùng chung quy tắc; mỗi ngày một lần, giữ kỷ lục khi đứt chuỗi.
- Trang chủ có chuỗi hiện tại/kỷ lục/lịch 7 ngày và XP quiz tuần tách khỏi level. Chưa có giải đấu nhiều người; đã ghi rõ trạng thái và bàn giao cơ chế XP theo tuần/ghép nhóm/thăng-xuống hạng cho backend. Không có leaderboard theo level cũ trong nguồn để gỡ.
- Đã bỏ hoàn toàn countdown, deadline và tự nộp quiz Toán; form chung chỉ cho nộp khi trả lời đủ. Bundle cũ có timeout vẫn chỉnh sửa được.
- Áp dụng ý tưởng phù hợp từ AnkiDroid/Moodle/LibreLingo: tiến độ cá nhân gọn, giữ ôn offline, thêm xem riêng câu sai sau quiz; lọc/khôi phục không cộng XP lại. Chưa triển khai spaced repetition.
- Sửa thêm lỗi nút ẩn máy tính Toán bị cắt khi font lớn: vùng phím cuộn nhường chiều cao cho các nút xếp dọc.
- `learning-responsive-verification.log`: debug, release, APK test, toàn bộ unit tests và lint PASS. **110 unit tests, 0 thất bại; lint toàn project 0 lỗi, 479 cảnh báo.**
- `learning-target-runtime-verification.log`: **10/10 PASS** ở cấu hình thường.
- `learning-calculator-target-verification.log`: **8/8 PASS** sau sửa máy tính và kiểm tra điểm ghi streak Khoa học ngay khi nộp.
- `learning-full-compact-final-runtime-verification.log`: **58 ca chức năng PASS, 8 live API chủ động bỏ qua** (runner ghi OK 66 tests), 190,949 giây; màn hình 320dp/font 1.3/ba nút/offline, tiếng Anh + chế độ tối ở các ca có cấu hình này.
- Lượt full đầu phát hiện nút máy tính bị cắt; đã sửa, xác minh trực tiếp và chạy lại toàn bộ thành công. Lượt đầu bị kết thúc trước khi hoàn tất, không tính là đạt.
- Đã xem ảnh lịch streak tiếng Anh/tối trên màn hình nhỏ: 7 cột vừa chiều ngang, đọc được, card cuộn nằm trên điều hướng.
- API: 10 trường key trong file cấu hình và BuildConfig các môn đều trống; không gọi Gemini trong lượt này. APK bàn giao được cập nhật ở `../AIMentor_QA/AImentor-debug.apk`.

Quy tắc, nghiên cứu, giới hạn và hợp đồng dữ liệu giải đấu đề xuất: [STREAK_AND_LEAGUES_2026-10-17.md](STREAK_AND_LEAGUES_2026-10-17.md). Giải đấu, đồng bộ streak nhiều máy và chống chỉnh đồng hồ/XP vẫn thuộc backend.
