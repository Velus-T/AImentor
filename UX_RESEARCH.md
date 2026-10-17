# Nghiên cứu UX và đánh giá ứng dụng học tập

Ngày đối chiếu: 16–17/10/2026. Phạm vi: giao diện và độ tin cậy của thao tác hiện có trong AIMentor.

## Cách đọc kết quả

Đây là nghiên cứu tài liệu và mẫu đánh giá công khai, không phải khảo sát người dùng AIMentor. Các đánh giá do người dùng tự chọn viết và trang cửa hàng chỉ hiển thị một phần. Không suy ra tỷ lệ người gặp lỗi, xếp hạng ứng dụng hoặc mức tăng hiệu quả học tập từ mẫu này. Nhận xét của người đánh giá được xem là trải nghiệm họ báo cáo, không phải sự thật đã kiểm chứng về mọi phiên bản.

## Những điểm người dùng khen và muốn tránh

| Ứng dụng, nguồn và ngày đánh giá | Quan sát định tính | Quyết định cho AIMentor |
| --- | --- | --- |
| [Khan Academy, Google Play](https://play.google.com/store/apps/details?id=org.khanacademy.android&hl=en_US): Annie Vasilyev, 30/08/2025; Kit M, 23/05/2024 | Khen cấu trúc học và giải thích khái niệm; có nhận xét giao diện chưa luôn dễ dùng | Đưa hành động học lên trước; giữ các môn, quiz và công cụ; mở thống kê khi cần |
| [Khan Academy, Google Play](https://play.google.com/store/apps/details?id=org.khanacademy.android&hl=en_US): Victoria Caylor, 09/07/2026 | Khen phản hồi mượt nhưng báo lớp scratch-pad che phần nhập đáp án | Kiểm tra máy tính nổi không che điều hướng; giữ kết quả ngoài vùng cuộn bàn phím |
| [Photomath, Google Play](https://play.google.com/store/apps/details?id=com.microblink.photomath&hl=en_US): jedi Master, 18/05/2020; một Google user, 19/02/2020; Katie Bunker, 30/07/2020 | Khen giải từng bước và nhập bằng ảnh; có đề nghị ẩn đáp án khi dạy học | Giữ luồng ảnh/văn bản; sửa nhãn ảnh khớp hành động thực tế. Tự giải trước khi xem đáp án là đề xuất tiếp theo, chưa triển khai. Các đánh giá này cũ, không dùng để mô tả chất lượng Photomath hiện tại |
| [Quizlet, Trustpilot](https://www.trustpilot.com/review/www.quizlet.com): Jim, ngày trải nghiệm 02/10/2026 | Khen thao tác đơn giản, tổ chức bộ thẻ/thư mục | Giữ bố cục gọn, điều hướng rõ; chưa thêm bộ thẻ hay spaced repetition |
| [Quizlet, Google Play](https://play.google.com/store/apps/details?id=com.quizlet.quizletandroid&hl=en_US): Jessica Powell, 05/08/2026 | Báo mất nội dung đã soạn khi không lưu được | Kiểm tra kết quả ghi thực; giữ editor và bản nháp khi lỗi; thử lưu lại sau khi lỗi được gỡ |
| [Quizlet, Google Play](https://play.google.com/store/apps/details?id=com.quizlet.quizletandroid&hl=en_US): Hannah Soules, 24/07/2026; Laura Steinmetz, 01/06/2026 | Phản ánh giới hạn trả phí và thao tác vuốt khó | Giữ nút thao tác nhìn thấy được. Báo rõ AI chưa sẵn sàng trước khi gửi; không thêm paywall, hạn ngạch hay màn hình nâng cấp trong vòng này |

## Căn cứ UX chung

- [NN/g — Progressive Disclosure](https://www.nngroup.com/articles/progressive-disclosure/): ưu tiên thao tác chính, mở chi tiết theo yêu cầu. Suy luận áp dụng: trang chủ cần giúp bắt đầu học trước khi đọc biểu đồ.
- [NN/g — Error-Message Guidelines](https://www.nngroup.com/articles/error-message-guidelines/): chỉ rõ lỗi và cách sửa, giữ công sức đã nhập, tránh báo lỗi quá sớm. Áp dụng: lỗi cạnh ô nhập sau khi gửi; giữ nội dung khi sửa lỗi.
- [NN/g — Recognition and Recall](https://www.nngroup.com/articles/recognition-and-recall/): các lựa chọn nhìn thấy được giảm yêu cầu nhớ. Áp dụng: nút chọn môn, mở lại môn gần đây, xem/thu gọn thống kê và mở lại máy tính.
- [NN/g — Mobile UX](https://www.nngroup.com/articles/mobile-ux/): tương tác di động bị gián đoạn và cần giữ trạng thái. Áp dụng: kiểm tra tái tạo màn hình khi đang soạn và giữ kết quả tính khi cuộn.
- [Android — Accessibility for Views](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views): vùng chạm tối thiểu được khuyến nghị là 48dp. Áp dụng: nút Lưu ghi chú và các thao tác chính có vùng chạm phù hợp.

## Thay đổi đã áp dụng

### Trang chủ

Có nút chọn môn ngay phía trên. Nếu có hoạt động đã lưu với thời gian xác định, nút mở lại đúng môn và hiển thị thời điểm gần nhất. Đây là mở môn, không tuyên bố khôi phục chính xác bài đang dở. Lối vào môn học dùng chung SubjectNavigator để không lệch tài khoản giữa các màn hình.

Thống kê đầy đủ vẫn tồn tại dưới nút xem/thu gọn; trạng thái mở được phục hồi sau đổi theme. Người chưa có hoạt động/EXP nhận hướng dẫn bắt đầu, không phải các biểu đồ trống thiếu giải thích. Trường hợp tải dữ liệu thất bại giữ thông báo thử lại, không diễn giải thành chưa học.

### Đăng nhập và đăng ký

FormFeedback báo lỗi tại đúng trường sau khi bấm gửi, chuyển focus đến trường cần sửa và bỏ lỗi cũ của trường khi người dùng nhập lại. Không xóa các trường khác. Điều kiện Gmail được giải thích trước khi gửi; quy tắc mật khẩu và tài khoản không bị âm thầm thay đổi.

### Công cụ nổi

Máy tính chỉ có ở Toán, vẫn kéo/bám cạnh/ẩn/thu gọn/mở lại được. Kết quả nằm ngoài vùng cuộn bàn phím, giữ trong tầm nhìn trên màn hình ngắn hoặc khi xoay ngang. Có nút hiển thị rõ để thu gọn/ẩn, không bắt người dùng chỉ dùng cử chỉ bí mật. Nhãn chọn ảnh Toán đã sửa khớp thư viện ảnh đang mở.

### Tính trung thực và giữ dữ liệu

Chat Toán/Lịch sử/Lập trình hiển thị thông báo nếu bản cài chưa cấu hình AI. Bấm gửi không xóa câu hỏi và không thêm tin nhắn người dùng giả vào luồng hội thoại. Thông báo không tự nhận đã kiểm tra kết nối mạng. Khoa học vẫn dùng được nguồn tham khảo cục bộ và có nhãn nguồn riêng.

Editor ghi chú Lập trình giữ autosave khi Back nhưng chỉ thoát sau khi ghi thành công hoặc người dùng chủ động bỏ thay đổi. Lỗi INSERT/UPDATE giữ nội dung, báo rõ chưa lưu và cho thử lại. Đổi giao diện/tạo lại activity không làm mất văn bản đang soạn trong ca đã kiểm tra. Không có cơ chế sao lưu đám mây mới.

## Kiểm chứng và giới hạn

ResearchUxTest kiểm tra người mới/quay lại, lỗi biểu mẫu, trạng thái thống kê, giữ câu hỏi ở ba module, giữ kết quả máy tính khi cuộn/xoay ngang, lưu ghi chú, autosave Back, mô phỏng lỗi INSERT/UPDATE, thử lại và không sửa bản ghi của tài khoản khác. Các trigger mô phỏng chỉ nhắm tài khoản thử nghiệm và được tháo sau test.

Các phép thử tự động xác nhận hành vi kỹ thuật; chưa chứng minh người dùng thật học nhanh hơn hoặc hài lòng hơn. API công khai để trống nên không đánh giá chất lượng giải thích AI hoặc độ chính xác đọc ảnh. Chưa bảo đảm bản nháp sống qua force-stop/gỡ cài đặt; chưa kiểm tra đầy đủ mọi bộ chọn ảnh/thiết bị thật.

## Hướng tiếp theo

1. Cho học sinh/phụ huynh thử các nhiệm vụ: tìm môn, gửi câu hỏi, dùng máy tính, sửa và lưu ghi chú. Đo thời gian tìm được thao tác, nhầm bấm, số lần cần trợ giúp; chưa cài theo dõi hành vi tự động.
2. Với backend và nội dung đã được kiểm chứng, cân nhắc gợi ý từng bước, tự giải trước khi mở lời giải và luyện lại câu sai. Không thêm nút hứa tính năng chưa có logic.
3. Kiểm tra thiết bị thật, TalkBack, giữ ảnh đính kèm khi đổi cấu hình, bản nháp khi tiến trình bị hệ điều hành thu hồi và các tình huống mạng/quota khi có API thử nghiệm.
4. Nếu có thương mại hóa, trình bày giới hạn trước khi người dùng đầu tư công soạn nội dung; thiết kế xuất/sao lưu dữ liệu trước khi thêm hạn chế quyền truy cập.
