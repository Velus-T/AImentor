# Báo cáo quiz, lịch sử và bàn giao backend — 17/10/2026

## Chức năng và cách tổ chức

Bốn môn dùng chung phần tạo quiz, làm bài, giải thích và kết quả trong `ui-core`. Các module vẫn chịu trách nhiệm chuyển đổi dữ liệu môn học, gọi dịch vụ và lưu kết quả. `data-core` chứa chính sách EXP/cấp độ, bộ lọc thời gian, dữ liệu câu hỏi và ngân hàng đề. Cách chia này giảm việc sửa cùng một hành vi ở bốn nơi.

- Số câu tạo đề: 5, 10, 15; mặc định 5. EXP mỗi câu đúng theo độ khó: 5/10/20; cấp độ tối đa 20, ngưỡng tăng theo chính sách chung. EXP đã có được giữ nguyên.
- Giữ câu chọn đáp án, điền và ghép; giữ dấu âm, dấu thập phân khi chấm câu điền. Ghép cho phép đổi thứ tự cặp nhưng không chấp nhận thiếu/trùng/sai cặp.
- Khi lưu bài thất bại, giữ câu trả lời và cho thử lại. Không cộng lại EXP khi bấm nộp nhiều lần hoặc tái tạo màn hình đã nộp.
- Đề Gemini được lưu theo tài khoản, môn, phạm vi, cấp học, chủ đề, nguồn và thời gian; toàn bộ đề được lưu trong giao dịch. Chống trùng ở cấp bộ đề có cùng metadata và nội dung, chưa phải loại trùng ngữ nghĩa từng câu.
- Ngân hàng cho tìm kiếm/lọc và mở lại không cần gọi AI. Nội dung câu hỏi giữ ngôn ngữ lúc tạo; chuyển ngôn ngữ giao diện không tự dịch câu hỏi.
- Công thức dùng KaTeX đóng gói trong app, không tải thư viện từ CDN. Trang HTML phải có doctype để tránh lỗi quirks mode khi render offline.
- Science phân biệt đề AI và câu mẫu cục bộ. Kho mẫu hiện không đủ 10/15 câu cho từng nhóm; app báo thiếu thay vì lặp câu hoặc giả số lượng. Người dùng có thể chọn 5 câu mẫu hoặc mở đề đã lưu.
- Toán giữ bộ đề có sẵn, bộ lọc và thời gian làm bài. Máy tính bong bóng chỉ xuất hiện trong môn Toán.

## Lịch sử và giao diện

Bốn môn dùng bố cục danh sách, nhóm ngày, tìm kiếm và bộ lọc thời gian chung. Tìm kiếm hỗ trợ không dấu và nội dung đã lưu. Lập trình/Lịch sử mở đúng hội thoại; Toán/Khoa học giữ mô hình câu hỏi đã lưu của từng môn, không tạo hội thoại giả để trông giống ChatGPT.

Các menu mở/xóa và chức năng mở rộng lời giải được giữ. Loại bỏ tiêu đề trùng, nút quay lại quá lớn và nút xóa toàn bộ nổi bật quá mức. Khi bàn phím mở, thanh điều hướng dưới nhường chỗ cho vùng nhập. Bộ lọc xử lý cả timestamp dạng epoch và định dạng cũ.

Ngôn ngữ Android 13 trở lên cần dùng LocaleManager qua context để lựa chọn vẫn áp dụng được trước khi có Activity. Android cũ tiếp tục dùng AppCompat. Việc chuyển theme/ngôn ngữ phải giữ bài đang làm và không cộng điểm lần nữa.

## Dữ liệu và giới hạn

Ngân hàng nằm trong SQLite `AImentor.db`, chưa đồng bộ máy chủ. Module ngân hàng phiên bản 2 thêm loại câu hỏi; Math phiên bản 9 thêm chủ sở hữu bộ đề. Bộ đề Math cũ không xác định chủ sở hữu được bảo toàn với owner 0, chưa tự gán cho một tài khoản.

API key trong bản Git vẫn trống. Chưa kiểm tra chất lượng câu hỏi bằng Gemini thật hoặc chạy cuộc gọi có phí. Kiểm tra trên giả lập không thay thế thử trên điện thoại thật và máy tính bảng. Kiểm thử chống nộp lặp trong UI không bảo đảm chống lặp khi tiến trình chết đúng giữa lúc commit dữ liệu và lưu trạng thái; backend cần mã lượt nộp và xử lý idempotency.

## Backend cần làm tiếp

1. Xác thực trên máy chủ, băm mật khẩu, quản lý phiên, quên mật khẩu và quyền sở hữu dữ liệu cho từng API.
2. Đưa API key Gemini lên máy chủ; giới hạn yêu cầu, quản lý model theo môn, timeout và chi phí.
3. Kiểm tra cấu trúc, số lượng, đáp án, nguồn và chất lượng câu hỏi trước khi công bố; ngân hàng có phiên bản/kiểm duyệt/báo lỗi.
4. Lưu lượt làm và EXP trong cùng giao dịch; mã lượt nộp duy nhất để gửi lại không cộng trùng; server là nơi xác nhận điểm.
5. Đồng bộ lịch sử, ghi chú, đề đã tải và tiến độ; hàng đợi offline, chính sách xung đột và phân trang/tìm kiếm.
6. Migration dữ liệu cũ, sao lưu/phục hồi, xóa tài khoản/dữ liệu, giám sát lỗi và công cụ quản trị nội dung.

## Kiểm chứng

Kết quả cuối: debug/release/AndroidTest build thành công; 84 unit test và 38/38 ca Android trên màn hình 320dp, font 1.3, điều hướng ba nút, offline đều qua. Hai ca bổ sung với bàn phím thật cũng qua. Lint toàn project không có lỗi, còn 475 cảnh báo. Các log `*-verification.log` là dữ liệu kiểm tra cục bộ, không đưa lên Git. Chi tiết mốc kiểm chứng nằm trong WORK_IN_PROGRESS.md.