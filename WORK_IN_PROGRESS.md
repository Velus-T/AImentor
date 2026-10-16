# Tiến độ tạm dừng — 16/10/2026

Người dùng yêu cầu tạm dừng và lưu tiến độ. Không tiếp tục công việc cho đến khi người dùng yêu cầu.

## Mốc đã công bố
- GitHub master: a0e190a — sửa thao tác bình phương/căn và kiểm tra kéo máy tính.
- Lượt cải tiến UX hiện tại được lưu cục bộ; chưa đẩy các thay đổi dang dở lên GitHub.

## Yêu cầu đang thực hiện
Nghiên cứu hành vi người dùng qua internet và áp dụng vào AIMentor. Sau đó xem đánh giá khen/chê các app học tập, học các điểm tốt và sửa hoặc tránh các vấn đề bị chê. Giữ đầy đủ chức năng, nhất quán Việt/Anh và sáng/tối; kiểm tra kỹ. Không thêm backend mới.

## Đã thay đổi
1. Trang chủ có hành động chọn môn ở phía trên; người có hoạt động gần đây được mở lại đúng môn theo dữ liệu thực, không hứa tiếp tục chính xác bài đang dở.
2. Thống kê chi tiết có nút mở/thu gọn; giữ toàn bộ thống kê cũ và phục hồi trạng thái mở sau đổi theme. Có hướng dẫn khi chưa có hoạt động/EXP.
3. SubjectNavigator dùng chung lối vào module từ Trang chủ và Danh mục.
4. FormFeedback báo lỗi tại ô cần sửa, chuyển focus đúng ô, xóa lỗi của ô khi sửa; không báo lỗi trước khi gửi. Đăng ký giải thích yêu cầu Gmail ngay trước khi nhập.
5. Máy tính giữ phần kết quả ngoài vùng cuộn bàn phím để không mất dấu phép tính trên màn hình ngắn.
6. AiAvailabilityNotice dùng chung cho chat Toán/Lịch sử/Lập trình: thông báo khi chưa cấu hình AI và chặn gửi trước khi xóa câu hỏi/ảnh. Khoa học vẫn có nguồn tham khảo cục bộ nên không chặn luồng đó.
7. NoteEditorActivity Lập trình được rút gọn; lưu/Back chỉ báo thành công khi database thực sự ghi được. Khi thất bại, giữ nội dung, cho tiếp tục sửa hoặc chủ động bỏ thay đổi. Vẫn giữ autosave khi Back. updateNote trả boolean. Nút Lưu có vùng chạm tối thiểu 48dp.
8. Toán không tự mở lại trình chọn ảnh khi activity được tạo lại.

## Đã kiểm tra
- Vòng UX đầu: assembleDebug/AndroidTest, app/math lint đều qua.
- ResearchUxTest (5 ca) + CalculatorInteractionDebugTest (2 ca): OK 7 tests. Bao gồm form, người mới/quay lại, đúng tài khoản, thống kê sau đổi theme, kết quả máy tính sau cuộn.
- Vòng sửa theo đánh giá người dùng: assembleDebug/AndroidTest và lint app/programming/math/history đều qua. Log: review-improvements-verification.log.
- Chưa chạy kiểm tra trên máy giả lập cho các thay đổi mới nhất về AI notice và bảo vệ ghi chú. Chưa chạy toàn bộ suite trên bản UX hiện tại.
- API vẫn trống; không thực hiện yêu cầu AI có phí.

## Nguồn nghiên cứu
- https://www.nngroup.com/articles/progressive-disclosure/
- https://www.nngroup.com/articles/error-message-guidelines/
- https://www.nngroup.com/articles/recognition-and-recall/
- https://www.nngroup.com/articles/empty-state-interface-design/
- https://www.nngroup.com/articles/mobile-ux/
- https://developer.android.com/guide/topics/ui/accessibility/views/apps-views
- Khan Academy: https://play.google.com/store/apps/details?id=org.khanacademy.android&hl=en_US
  Annie Vasilyev (30/08/2025): khen cấu trúc môn học, luyện tập; Victoria Caylor (09/07/2026): khen phản hồi mượt nhưng lớp scratch-pad che ô nhập; Kit M (23/05/2024): khen giải thích khái niệm, nhận xét giao diện chưa luôn dễ dùng.
- Photomath: https://play.google.com/store/apps/details?id=com.microblink.photomath&hl=en_US
  Các đánh giá hiển thị từ 2020 khen giải thích từng bước, nhập bằng ảnh; một phụ huynh muốn ẩn đáp án để tự học. Ghi rõ tuổi của các đánh giá, không mô tả như đánh giá mới.
- Quizlet: https://play.google.com/store/apps/details?id=com.quizlet.quizletandroid&hl=en_US
  Jessica Powell (05/08/2026): phản ánh mất công soạn vì không lưu; Hannah Soules (24/07/2026): phản ánh hạn chế bản miễn phí; Laura Steinmetz (01/06/2026): phản ánh paywall và vuốt khó.
- https://www.trustpilot.com/review/www.quizlet.com
  Jim (trải nghiệm 02/10/2026): khen thao tác đơn giản và tổ chức bộ thẻ/thư mục. Trang có mẫu đánh giá tự chọn, không đại diện toàn bộ người dùng.
Các quan sát trên là mẫu định tính và giả thuyết thiết kế, chưa phải dữ liệu đo từ người dùng AIMentor. Không dùng số sao/tỷ lệ để khẳng định tác động của thay đổi.

## Việc tiếp theo khi được yêu cầu tiếp tục
1. Bổ sung kiểm tra giữ câu hỏi khi AI trống cho cả 3 môn, không tạo tin nhắn giả/không mất ảnh.
2. Kiểm tra ghi chú: thêm/sửa/Back, lỗi INSERT/UPDATE, không ghi đè tài khoản khác, giữ nội dung sau đổi theme/rotate, không đóng editor khi ghi thất bại. Có thể dùng trigger SQLite trong fixture riêng để mô phỏng thất bại, luôn tháo trigger sau test.
3. Kiểm tra và phục hồi preferences của ResearchUxTest (calculator prefs hiện chưa được snapshot/restore); bảo đảm cleanup math_history chạy được trên cài mới.
4. Rà chuyển màn hình/keyboard/portrait-landscape; kết quả máy tính cố định và bàn phím cuộn phải luôn thao tác được. Điều chỉnh các test cũ nếu keypad giờ cần scrollTo.
5. Định dạng các lớp mới/sửa bằng formatter Java có tại C:/Users/ACER/AppData/Local/Temp/aimentor-google-java-format-1.24.0.jar; giữ kiểu newline của file theo Git để tránh diff lớn.
6. Viết báo cáo nghiên cứu UX với nguồn, ngày đánh giá, điểm khen/chê, quyết định đã áp dụng/giới hạn và kế hoạch kiểm chứng với người dùng thật. Không tuyên bố đã thêm spaced repetition, cloud sync hoặc nâng độ chính xác AI.
7. Chạy debug/release, 74 unit tests và toàn bộ instrumentation (hiện 20 ca nếu thêm ResearchUxTest vào 15 ca cũ), lint; thử nhỏ/1.3 font/ba nút và Việt/Anh/sáng/tối. Lưu ảnh và đọc log crash.
8. Sau khi đạt, cập nhật báo cáo, đối chiếu remote, commit hoàn thiện và push theo yêu cầu trước đó; cập nhật APK ở ../AIMentor_QA/AImentor-debug.apk. Hiện APK đó vẫn là bản a0e190a đã công bố.

## Môi trường
Repo AI_Mentor_GitHub, branch master. JDK C:/Users/ACER/.jdks/jbr-21.0.11. SDK C:/Users/ACER/AppData/Local/Android/Sdk. AVD Medium_Phone_2, emulator-5556. Python C:/Users/ACER/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe.
Không sửa sources/ của project mirror. Không có subagent hay goal đang hoạt động.
