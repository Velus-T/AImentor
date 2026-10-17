# Rà soát logic toàn app — 17/10/2026

## Phạm vi và phương pháp

Đọc các luồng trong app shell, data-core, ui-core và bốn module Toán, Khoa học, Lịch sử, Lập trình. Đối chiếu đường đi từ màn hình đến dịch vụ/parser/database; rà soát quyền sở hữu dữ liệu, trạng thái Activity/Fragment, lỗi đọc/ghi, chấm điểm và nguồn nội dung. Chạy lại kiểm thử sẵn có, thêm ca tái hiện các lỗi mới bằng JSON giả lập, repository giả và trigger SQLite gây lỗi có kiểm soát. Không gửi yêu cầu Gemini thật vì bản Git không có khóa API.

## Lỗi đã xác nhận và sửa

| Mức | Lỗi trước khi sửa | Xử lý |
|---|---|---|
| Cao | Lịch sử chọn 15 câu nhưng parser cắt còn tối đa 10; tạo đề luôn báo thiếu | Parser nhận đủ 15; kiểm thử trực tiếp đường parse JSON của service |
| Cao | Math QuizQuestion biến đáp án không hợp lệ thành A | Giữ đáp án không hợp lệ ở trạng thái rỗng; từ chối đề AI trước khi lưu vào catalog/bank |
| Cao | Science chấp nhận câu AI thiếu lựa chọn/đáp án/lời giải, loại câu lạ và cặp ghép không hợp lệ | Kiểm tra cấu trúc trước khi lưu; đáp án đúng/sai có thứ tự cố định; đề trùng/thiếu bị từ chối |
| Cao | Science TRUE_FALSE dùng `False` ở model nhưng bank yêu cầu `FALSE`, gây lỗi lưu đề | Adapter chuẩn hóa mã đáp án; kiểm thử parse → adapter → bank → chấm lại |
| Vừa | Lịch sử hiểu numeric correctAnswer 1/2/3 thành chỉ số 0-based, lệch đáp án | Tách correctIndex 0–3 với correctAnswer số hiển thị 1–4 |
| Vừa | Câu trả lời số với exponent vượt giới hạn có thể làm BigDecimal ném lỗi khi nộp bài | Trả về không đúng thay vì làm crash; giữ quy tắc dấu âm và dấu thập phân |
| Vừa | Bookmark Science báo đã lưu dù insert trả -1, hoặc báo đã bỏ dù bản ghi còn tồn tại | Kiểm tra kết quả insert và kiểm tra trạng thái sau delete; truyền lỗi về UI hiện có |
| Vừa | Study-plan repository nuốt lỗi mã hóa/lưu nên màn hình báo thành công hoặc đánh dấu đã hoàn thành giả | Lưu có kiểm tra kết quả; use case báo lỗi; cơ chế rollback checkbox hiện có nhận được lỗi thật |
| Vừa | Chat Lập trình tạo session mới khi recreate; có thể lặp gửi câu mở đầu | Lưu session ID, trạng thái câu mở đầu và URI ảnh chờ; khôi phục đúng session |
| Vừa | Chat Lịch sử quay lại conversation ID trong Intent cũ sau khi bấm chat mới rồi recreate | Lưu và ưu tiên ID đang hoạt động |
| Vừa | Chat Toán mất transcript khi recreate; callback có thể cập nhật màn hình cũ hoặc lưu theo tài khoản hiện tại thay vì lúc gửi | Giữ transcript, bản nháp/URI; snapshot chủ sở hữu và chặn callback cũ; không tự gửi lại yêu cầu bị gián đoạn |
| Vừa | Chat Toán không gửi ngữ cảnh, câu hỏi tiếp nối như “giải thích bước 2” thiếu thông tin | Gửi ngữ cảnh giới hạn theo số tin và độ dài cùng câu hỏi hiện tại |
| Vừa | Đọc ảnh Toán giải mã kích thước đầy đủ, dễ tăng bộ nhớ quá mức | Đọc bounds, lấy mẫu giới hạn kích thước và recycle bitmap |
| Vừa | Chat Lịch sử xóa ô nhập mà chưa kiểm tra lưu câu hỏi; các câu trả lời lưu lỗi chỉ bị bỏ qua | Lưu trước khi xóa bản nháp, báo lỗi và giữ ảnh/chữ; cảnh báo khi câu trả lời chỉ hiện trên màn hình nhưng chưa được lưu |
| Vừa | Ghi chú Toán/Lịch sử/Science có thể crash khi UPDATE ném lỗi | Giữ dialog/bản nháp, báo lỗi, cho retry; thêm ca UPDATE lỗi → recreate → retry |
| Vừa | Ghi chú nhập trong dialog bị mất khi recreate | NoteEditorState giữ ID, tiêu đề, nội dung, phân loại; không đưa dữ liệu này vào database trước khi người dùng lưu |
| Vừa | Thanh điều hướng tạo lại Fragment đã khôi phục; Science làm mất dialog/bản nháp | Giữ Fragment cùng loại đang có, tránh thay tab trùng; áp dụng ở bốn module |
| Vừa | Đổi tab hoặc Back từ quiz Lập trình bỏ bài chưa nộp mà không hỏi | Dùng chung xác nhận rời bài; tiếp tục giữ bài, rời mới bỏ |
| Vừa | Ghi chú Science lưu tên môn đã dịch; đổi ngôn ngữ rồi sửa có thể chọn nhầm môn | Lưu mã cố định, đọc được nhãn Việt/Anh cũ, dịch nhãn khi hiển thị |
| Thấp | Đọc ghi chú lỗi có thể hiện danh sách rỗng như không có dữ liệu | Giữ phần đang hiển thị và báo không đọc được dữ liệu |
| Thấp | Reminder đến đúng lúc vừa tắt vẫn có thể chạy receiver và thông báo | Receiver kiểm tra lại trạng thái bật/quyền thông báo trước khi hiển thị |

Kiểm tra cấu trúc đề dùng hợp đồng chung trong data-core; lớp kiểm tra riêng Science giữ các loại điền/ghép/đúng-sai. Dữ liệu môn, persistence và điều phối UI vẫn có trách nhiệm riêng; không đưa logic database vào lớp hiển thị chung.

## Nhóm đã rà soát, chưa thấy lỗi mới trong các ca được kiểm tra

- Đăng ký/đăng nhập, đổi hồ sơ/mật khẩu, BCrypt và nâng cấp mật khẩu cũ; đăng xuất xóa session điều hướng. Quy tắc chỉ Gmail là quyết định sản phẩm hiện tại, không tự bỏ.
- Phân tách dữ liệu theo tài khoản, owner của bank/quiz Toán, cập nhật ghi chú có điều kiện user ID; import database cũ có transaction/rollback.
- Cùng chính sách EXP/cấp độ, lưu attempt và điểm nguyên tử của Toán/Lập trình, tổng điểm từ attempt của Science/Lịch sử; nộp lặp trong UI và recreate sau nộp.
- Timer Toán dùng deadline theo elapsedRealtime, không tự reset khi recreate; timeout cho phép câu chưa làm và khóa sửa khi retry lưu.
- Máy tính: thứ tự phép tính, số âm, ngoặc, chia 0, căn âm, lũy thừa, các nút bình phương/căn lặp và kéo bong bóng.
- Mạch điện: nối tiếp/song song, R tương đương, dòng nhánh, điện áp, công suất và công tắc; mô hình là DC lý tưởng hai điện trở, không phải bộ mô phỏng mạch bất kỳ.
- Lịch sử: lọc theo ngày/epoch, tìm nội dung, nhóm thời gian và mở đúng dữ liệu; chat/câu hỏi độc lập được giữ theo mô hình môn.
- Chuyển ngôn ngữ/theme, system insets, trạng thái khi bàn phím mở; nguồn AI/câu mẫu/cache được phân biệt.

## Giới hạn cần bàn giao

1. Chưa kiểm tra mạng Gemini thật: timeout, quota, khóa/model, ảnh thực tế, chất lượng và tính đúng của kiến thức. Parser đúng không bảo đảm đáp án AI đúng.
2. Xác thực/đồng bộ máy chủ, quyền từng API, bảo vệ API key trong server và idempotency nộp bài xuyên sự cố chết tiến trình vẫn là backend. Đây là app dùng dữ liệu cục bộ.
3. Chat AI đang chạy có thể bị gián đoạn khi Activity bị hủy. Bản sửa không tự gọi lại gây phí/lặp; cần mô hình tác vụ bền vững hoặc backend nếu muốn tiếp tục qua đóng app.
4. Chưa có chính sách xác minh chủ sở hữu cho bộ đề/dữ liệu Toán cũ owner 0; giữ dữ liệu, không tự gán tài khoản.
5. Kho mẫu Science không đủ 10/15 câu từng nhóm; phải chọn 5 câu mẫu, đề đã lưu hoặc AI. Không nhân bản câu để đủ số.
6. Chưa kiểm tra điện thoại thật, mọi schema lịch sử hoặc dataset cực lớn. Lint vẫn còn cảnh báo cũ. Không kết luận app không còn lỗi chỉ vì kiểm thử qua.

## Kiểm chứng và bàn giao

Kết quả cuối: 97/97 unit test, 10/10 ca Android mới ở cấu hình thường, và 48/48 ca Android toàn bộ ở cấu hình 320dp/font 1.3/tối/ba nút/offline đều qua. Debug/release/AndroidTest build thành công; lint toàn project có 0 lỗi và 476 cảnh báo. Chi tiết mốc kiểm chứng nằm trong WORK_IN_PROGRESS.md. Log cục bộ có hậu tố `-verification.log`; không đưa log hoặc khóa API lên Git.
