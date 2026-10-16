package com.example.modulelaptrinh.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.modulelaptrinh.MainActivity;

/**
 * CỔNG VÀO PROGRAMMING MODULE TỪ APP CHÍNH.
 * App chính chỉ cần gọi {@code ProgrammingFeature.open(context, userId)}.
 * File này tạo Intent mở MainActivity và truyền userId sang module.
 */
public final class ProgrammingFeature {
    // Constructor `ProgrammingFeature`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    private ProgrammingFeature() {
    }

    // Hàm `open()`: Điều hướng/mở màn hình hoặc chức năng tương ứng.
    public static void open(Context context, int userId) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (context == null) {
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // MainActivity là màn hình root của Programming Module.
        Intent intent = new Intent(context, MainActivity.class);
        // Truyền ID tài khoản để database lọc dữ liệu đúng user.
        intent.putExtra(MainActivity.EXTRA_USER_ID, Math.max(0, userId));
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (!(context instanceof Activity)) {
            // Gọi `addFlags()` trên `intent` để thực hiện thao tác tương ứng.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        // Yêu cầu Android mở Activity/action được mô tả bởi Intent.
        context.startActivity(intent);
    }
}
