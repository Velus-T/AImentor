# Kiểm thử Gemini thực tế bằng API cũ — 17/10/2026

Người dùng cho phép dùng lại API cũ để test. Khóa được đọc từ bản ZIP gốc đã cung cấp, không đưa vào source của ca kiểm thử hoặc báo cáo.

## Kết quả

- Tìm được 7 khóa: 3 Toán, 1 Lịch sử, 3 Lập trình.
- 7/7 khóa gọi danh sách model của Google thành công (HTTP 200); danh sách có `gemini-flash-latest`, đúng model trong cấu hình app.
- ZIP không có khóa Science riêng. Phiên test tạm dùng pool khóa Toán cho Science; không khẳng định đã kiểm tra một bộ khóa Science độc lập.
- 8/8 ca Android gọi Gemini thật thành công, tổng thời gian 181,603 giây.

| Ca | Kết quả |
|---|---|
| Chat Toán qua MathGeminiApiClient | Có phản hồi; mẫu phương trình 2x + 3 = 7 cho x = 2 |
| Chat Lịch sử qua HistoryGeminiApiClient | Có phản hồi; mẫu Điện Biên Phủ trả năm 1954 |
| Chat Lập trình qua GeminiApiClient | Có phản hồi cho câu hỏi vòng lặp Python |
| Chat Science qua GeminiApiClient | Có phản hồi cho câu hỏi định luật Ohm |
| Quiz Toán, luồng tạo đề trên màn hình thật | Đủ 5 câu, lưu bank, đọc lại dữ liệu và kiểm tra đáp án với bộ chấm chung |
| Quiz Lịch sử, luồng tạo đề trên màn hình thật | Đủ 15 câu, lưu bank, đọc lại dữ liệu và kiểm tra đáp án với bộ chấm chung |
| Quiz Lập trình, luồng tạo đề trên màn hình thật | Đủ 5 câu, lưu bank, đọc lại dữ liệu và kiểm tra đáp án với bộ chấm chung |
| Quiz Science, luồng tạo đề trên màn hình thật | Đủ 5 câu, lưu bank; mẫu có trắc nghiệm, điền, đúng/sai và ghép |

Các ca quiz chờ bản ghi bank theo đúng tài khoản test. Vì thế Science không được coi là thành công nếu chỉ trả câu mẫu fallback mà chưa có đề Gemini trong bank.

## Bảo vệ cấu hình và cách chạy lại

`LiveGeminiSmokeTest` chỉ chạy khi truyền `mentorLiveAi=true`; chạy mặc định bỏ qua cả 8 ca, không gọi mạng. Đây là kiểm thử tùy chọn có thể dùng quota, không ghép việc gọi AI có phí vào suite offline mặc định.

Trong phiên này, API được điền tạm khi biên dịch bản debug riêng; khối finally khôi phục nguyên file `api.properties` trống ngay sau build. Sau test đã biên dịch/cài lại APK công khai trống và xóa hai APK riêng chứa cấu hình thử. Đã kiểm tra 10 trường API trong file công khai và BuildConfig sinh ra của bốn module đều trống. Không công bố khóa hoặc APK chứa khóa lên GitHub.

Tài khoản và dữ liệu database tạo riêng cho test được dọn sau từng ca. Các phản hồi không chứa khóa được lưu cục bộ tại `../AIMentor_QA/live-api-2026-10-17/live-ai/`, phục vụ đọc lại và kiểm tra nội dung.

## Giới hạn

Đây là smoke test một lượt thành công, không chứng minh mọi lần gọi đều thành công hoặc mọi đáp án AI đều đúng. Chưa chủ động gây lỗi quota/429/503/timeout, chưa test upload ảnh với API thật, chưa benchmark tốc độ hoặc kiểm tra chi phí thanh toán. Chưa chạy trên điện thoại thật. Các kiểm thử offline và 97 unit test của lượt audit trước vẫn là nhóm kiểm chứng riêng.

Đã xem mẫu nội dung trả về. Với Lịch sử có đối chiếu thêm thông tin phân khu/cứ điểm từ [Ban Quản lý di tích tỉnh Điện Biên](https://bqldt.svhttdl.dienbien.gov.vn/portal/pages/2020-7-2/Di-tich-cau-Muong-Thanhm12uc3jpg96j.aspx); việc này không thay thế kiểm duyệt toàn bộ ngân hàng câu hỏi.

Log cục bộ: `live-old-api-probe-verification.log`, `live-old-api-actual-build-verification.log`, `live-old-api-runtime-verification.log`, `live-api-restore-public-build-verification.log`, `live-api-default-optout-verification.log`.
