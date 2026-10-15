# Tài khoản dùng thử

Các tài khoản dưới đây được tạo tự động khi mở cơ sở dữ liệu trong bản **debug**
(Run trong Android Studio hoặc APK debug). Không cần gỡ app hay xóa dữ liệu.

| Tên đăng nhập | Mật khẩu |
| --- | --- |
| hocvien01 | Mentor01!2026 |
| hocvien02 | Mentor02!2026 |
| hocvien03 | Mentor03!2026 |

Cả ba có cùng quyền người dùng (`role = 1`); app chưa có luồng phân quyền riêng
cho giáo viên hoặc quản trị viên. Đây là các tài khoản mẫu mới, không phải thông
tin đăng nhập của người đã đăng ký trước đó. Tài khoản cũ trùng tên được giữ
nguyên, bao gồm mật khẩu; quá trình tạo mẫu không ghi đè thông tin của họ.

Bản release không tự tạo các tài khoản này. Nếu một thiết bị đã chạy bản debug,
dữ liệu mẫu đã có trên thiết bị vẫn tồn tại khi cập nhật sang release.

# Giao diện đăng nhập / đăng ký

- Dùng chung phần thương hiệu, thẻ biểu mẫu, kiểu ô nhập và nút bấm.
- Ô nhập Material có nhãn nổi, trạng thái focus và báo lỗi ngay dưới ô.
- Mật khẩu có nút hiện/ẩn; nút Done trên bàn phím gửi biểu mẫu.
- Biểu mẫu cuộn và giới hạn chiều rộng để đọc dễ hơn trên máy tính bảng.
- Có nội dung tiếng Việt khi thiết bị dùng tiếng Việt, tiếng Anh mặc định.
- Trở lại đăng nhập không tạo thêm chuỗi màn hình đăng nhập/đăng ký.
