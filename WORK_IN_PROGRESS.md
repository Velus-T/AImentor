# Tiến độ tạm dừng — 17/10/2026

Người dùng vừa yêu cầu **tạm dừng**. Không tiếp tục sửa code, push hoặc tạo tác vụ cloud cho đến khi người dùng yêu cầu tiếp tục.

## Trạng thái
- Bản WIP cục bộ; chưa hoàn tất và chưa đẩy lượt thay đổi mới lên GitHub.
- Chưa khởi chạy tác vụ cloud. Phiên này chạy local, không tiếp tục khi máy tắt.
- Remote công bố gần nhất đã biết: a0e190a. Local có mốc 0403548 và checkpoint WIP mới.
- API key vẫn trống; model names là cấu hình, không phải secret. Không thực hiện cuộc gọi Gemini thật.

## Yêu cầu cần giữ
1. Hoàn tất nghiên cứu UX và áp dụng đánh giá khen/chê app học tập; xem UX_RESEARCH.md.
2. Lịch sử chat theo cách ChatGPT: gọn, tìm kiếm, nhóm thời gian, mở lại, menu rõ ràng, đồng nhất bốn môn. Không gộp câu hỏi đơn thành hội thoại giả.
3. Môn Lịch sử bổ sung những gì hữu ích còn thiếu so với Khoa học; giữ đặc trưng môn, không chép công cụ khoa học.
4. Bốn môn chung cách tạo quiz, bố cục làm bài/kết quả và tính EXP; tham khảo Science. Lựa chọn số câu **5, 10, 15**, mặc định 5.
5. Mỗi đề Gemini tạo thành công phải lưu/phân loại vào bank theo môn/phạm vi/cấp học/chủ đề/độ khó, gồm đáp án/lời giải; có thể chọn ôn lại không cần mạng.
6. Giữ mọi chức năng, công thức Toán, timer, câu đúng/sai/điền/ghép của Science, máy tính bong bóng chỉ ở Toán; theme/ngôn ngữ/responsive/system insets phải kiểm tra kỹ.

## Đã sửa trong checkpoint hiện tại
- UX Home, inline auth errors, AI-unconfigured draft protection, pinned calculator display, note write failure recovery (xem UX_RESEARCH.md).
- History home: chủ đề Việt Nam/thế giới tạo bản nháp không tự gửi; lối vào EXP/quiz, ghi chú, xem toàn bộ chat.
- History conversations: tìm tiêu đề/nội dung gần nhất, nhóm ngày, menu mở/xóa, chat mới; màu theo theme; bỏ giới hạn 100 khi tìm. Chưa chuẩn hóa ba lịch sử chat khác.
- QuizPolicy trong data-core: reward 5/10/20, curve level 120 +25 mỗi cấp, max20. Giữ nguyên EXP đã lưu; level Programming được tính lại theo curve mới.
- QuizCard, QuizBank (cùng AImentor.db, 2 bảng mentor_quiz_bank_sets/questions, version module riêng); transaction, SHA256 dedup, account ownership, metadata, difficulty index.
- QuizSetupView/QuizFormView/QuizQuestionCard dùng chung ui-core; tạo đề, lựa chọn đáp án, nộp, lời giải, retry save, state Bundle; render công thức qua KaTeX như nguồn cũ.
- History/Science/Programming player dùng shared page. Math creation dùng shared setup trên trang catalog cũ, player shared form, vẫn có timer và toàn bộ lời giải.
- Generators History/Programming/Science nhận context và lưu bank trước callback success; Math cũng lưu bank sau legacy full set.
- UI mở bank để ôn lại; vẫn dùng persistence/EXP riêng hiện có của mỗi module.
- Programming attempt và EXP ghi trong cùng transaction.
- Math schema version9 thêm owner_id cho quiz sets; đề AI/BANK mới thuộc người tạo, đề cũ owner0 bảo toàn vì không có thông tin chủ sở hữu đáng tin cậy.
- Science built-in fallback có cờ local, nhãn ôn tổng hợp, không gắn nhãn AI mới; không lưu built-in thành đề Gemini. Matching pairs được đưa vào prompt hiển thị; chấm text không bỏ dấu âm/dấu thập phân.

## Kiểm tra thực tế
- shared-quiz-check-verification.log: assembleDebug, assembleDebugAndroidTest, testDebugUnitTest, lintDebug toàn project PASS trước lượt đổi số câu cuối.
- quiz-counts-verification.log: assembleDebug, assembleDebugAndroidTest, testDebugUnitTest PASS sau 5/10/15.
- SharedQuizBankTest máy giả lập compact: 6 ca, 5 đạt, 1 lỗi Espresso RootViewWithoutFocus ở historyCanPracticeSavedQuestionsWithoutCallingAI. Chưa xác định đây là lỗi đồng bộ test hay giao diện thật. Science offline, bank persist/dedup/owner isolation, rollback, form retry/no-double-award, Programming transaction đã đạt.
- Suite cũ 26 ca trước quiz: normal26 đạt; compact còn flaky calculator landscape. Không coi đó là xác nhận cho quiz mới.
- Chưa chạy Gemini thật, chưa xác minh độ đúng nội dung do AI tạo (API trống).
- Logs *-verification.log bị ignore và chỉ lưu local; test source đã lưu trong Git.

## Việc cần làm ngay khi tiếp tục
1. Kiểm tra kỹ diff WIP, định dạng các class mới; bỏ code/resources cũ không còn dùng sau khi xác minh không mất chức năng.
2. Science request đã nâng giới hạn đến15 nhưng LocalAdaptiveQuizBank hiện mỗi môn/cấp chỉ khoảng7 câu. Không lặp câu/giả đủ10/15: cần nguồn đề bank phù hợp hoặc thông báo thiếu câu rõ ràng; quyết định mở rộng bank mẫu phải đúng kiến thức/cấp học. AI và đề cached15 đã có cấu trúc hỗ trợ, chưa kiểm thử end-to-end15.
3. Bổ sung test offline Math + Programming qua UI thật và test 5/10/15, metadata/ownership, theme vi/en, rotation/process restoration, math formulas, input/IME. Test fixtures phải xóa sau chạy.
4. Ổn định lifecycle/window focus trong SharedQuizBankTest; kiểm tra error root History. Các test hiện chờ startActivitySync/idle nhưng chưa chờ windowfocus.
5. Rà callback/đổi màn hình/đổi tài khoản lúc generate; bankContext application, user snapshot. Kiểm tra lỗi ghi bank riêng (Science SaveException tránh fallback che lỗi).
6. Hoàn thiện bank picker: hiện là danh sách có metadata, chưa có tìm kiếm/bộ lọc tương tác. Không bỏ sót bộ đề cũ khi nâng cấp.
7. Rà shared form submit failure/rotation/idempotency; hiện guard double tap và Bundle, chưa token idempotency xuyên crash đúng giữa commit và saved-state. Kiểm tra script Math owner migration và alias filters, seed shared owner0.
8. Hoàn tất chat histories Math/Programming/Science (Math epoch filter sai; Science nút search/filter cũ điều hướng nhầm, allItems chưa clear/onResume refresh; còn ở code cũ).
9. Full debug/release/unit/lint/instrumentation, kiểm tra app-specific crash và screenshot. Không cài APK khi instrumentation đang chạy.
10. Cập nhật reports và APK ../AIMentor_QA/AImentor-debug.apk sau khi đã pass. APK QA hiện vẫn bản a0e190a.
11. Chỉ push khi người dùng tiếp tục và work đã review. Không đánh dấu bản này là hoàn tất.

## Môi trường local / cloud
- JDK21: C:/Users/ACER/.jdks/jbr-21.0.11
- Android SDK: C:/Users/ACER/AppData/Local/Android/Sdk; compileSdk36.
- Local dùng gradlew.bat. Cloud Linux dùng bash ./gradlew, cần JDK21 + SDK và Android build dependencies. Không copy local.properties lên Git.
- Kiểm thử emulator/ADB local không tự chuyển sang cloud. Cloud chưa được khởi chạy.
- Nếu người dùng yêu cầu cloud, cần checkpoint được push có chủ đích, tạo môi trường Cloud cho Velus-T/AImentor và yêu cầu đọc file này; không hứa cloud đang chạy khi chưa có xác nhận.
