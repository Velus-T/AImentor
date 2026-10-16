package com.example.modulelaptrinh;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.modulelaptrinh.database.ProgrammingDbHelper;

/**
 * MÀN HÌNH TẠO/SỬA NOTE.
 *
 * <p>noteId == -1 -> INSERT note mới; noteId != -1 -> UPDATE note cũ.</p>
 * <p>Database: ProgrammingDbHelper -> bảng notes.</p>
 */
public class NoteEditorActivity extends AppCompatActivity {

    // Tham chiếu `etNoteTitle` tới ô nhập liệu trong layout để đọc/cập nhật giao diện.
    private EditText etNoteTitle;
    // Tham chiếu `etNoteContent` tới ô nhập liệu trong layout để đọc/cập nhật giao diện.
    private EditText etNoteContent;
    // Tham chiếu `btnBack` tới ImageView trong layout để đọc/cập nhật giao diện.
    private ImageView btnBack;
    // Tham chiếu `btnSave` tới TextView trong layout để đọc/cập nhật giao diện.
    private TextView btnSave;
    // Tham chiếu `tvToolbarTitle` tới TextView trong layout để đọc/cập nhật giao diện.
    private TextView tvToolbarTitle;
    // Helper `dbHelper` chịu trách nhiệm đọc/ghi SQLite `ProgrammingMentor.db`.
    private ProgrammingDbHelper dbHelper;
    // Tính/đọc và lưu ID của ghi chú đang mở để sử dụng ở các bước phía sau.
    private int noteId = -1;
    // Tính/đọc và lưu giá trị `isSaved` (is saved) để sử dụng ở các bước phía sau.
    private boolean isSaved = false;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int userId;

    @Override
    // Hàm `onCreate()`: Khởi tạo Activity và chuẩn bị các thành phần cần thiết khi màn hình được tạo.
    protected void onCreate(Bundle savedInstanceState) {
        // Gọi implementation `onCreate()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onCreate(savedInstanceState);
        // Gắn file layout XML làm giao diện cho Activity này.
        setContentView(R.layout.activity_note_editor);

        // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
        dbHelper = new ProgrammingDbHelper(this);
        // Lấy Intent đã dùng để mở Activity nhằm đọc các tham số được truyền vào.
        userId = Math.max(0, getIntent().getIntExtra(MainActivity.EXTRA_USER_ID, 0));

        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        etNoteTitle = findViewById(R.id.et_note_title);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        etNoteContent = findViewById(R.id.et_note_content);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        btnBack = findViewById(R.id.iv_back);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        btnSave = findViewById(R.id.btn_save);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);

        // Lấy Intent đã dùng để mở Activity nhằm đọc các tham số được truyền vào.
        noteId = getIntent().getIntExtra("NOTE_ID", -1);
        // Lấy Intent đã dùng để mở Activity nhằm đọc các tham số được truyền vào.
        String title = getIntent().getStringExtra("NOTE_TITLE");
        // Lấy Intent đã dùng để mở Activity nhằm đọc các tham số được truyền vào.
        String content = getIntent().getStringExtra("NOTE_CONTENT");

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (noteId != -1 && title != null) {
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            tvToolbarTitle.setText(R.string.note_edit_title);
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            etNoteTitle.setText(title);
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            etNoteContent.setText(content);
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            tvToolbarTitle.setText(R.string.note_new_title);
        }

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        btnBack.setOnClickListener(v -> {
            // Gọi hàm `saveNoteIfChanged()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            saveNoteIfChanged();
            // Gọi hàm `setResult()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            setResult(RESULT_OK);
            // Gọi hàm `finish()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            finish();
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        btnSave.setOnClickListener(v -> {
            // Gọi hàm `saveNote()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            saveNote();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            // Hàm `handleOnBackPressed()`: Thực hiện chức năng `handleOnBackPressed` của class này.
            public void handleOnBackPressed() {
                // Gọi hàm `saveNoteIfChanged()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                saveNoteIfChanged();
                // Gọi hàm `setResult()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                setResult(RESULT_OK);
                // Gọi hàm `finish()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                finish();
            }
        });
    }

    /**
     * Auto-save khi bấm Back. Nếu đã save hoặc title/content đều rỗng thì không ghi DB.
     */
    private void saveNoteIfChanged() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (isSaved) return;
        
        // Đọc nội dung hiện tại người dùng đã nhập/đang hiển thị trên View.
        String title = etNoteTitle.getText().toString().trim();
        // Đọc nội dung hiện tại người dùng đã nhập/đang hiển thị trên View.
        String content = etNoteContent.getText().toString().trim();

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (title.isEmpty() && content.isEmpty()) {
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (noteId != -1) {
            // Có ID -> UPDATE record cũ, không tạo note mới.
            dbHelper.updateNote(userId, noteId, title, content);
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Chưa có ID -> INSERT record mới vào notes.
            long id = dbHelper.insertNote(userId, title, content);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (id != -1) {
                // Cập nhật ID của ghi chú đang mở theo trạng thái/kết quả vừa tính được.
                noteId = (int) id;
            }
        }
        // Cập nhật giá trị `isSaved` (is saved) theo trạng thái/kết quả vừa tính được.
        isSaved = true;
        // Gọi hàm `setResult()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setResult(RESULT_OK);
    }

    /** Save thủ công; validate rỗng rồi chọn INSERT hoặc UPDATE theo noteId. */
    private void saveNote() {
        // Đọc nội dung hiện tại người dùng đã nhập/đang hiển thị trên View.
        String title = etNoteTitle.getText().toString().trim();
        // Đọc nội dung hiện tại người dùng đã nhập/đang hiển thị trên View.
        String content = etNoteContent.getText().toString().trim();

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (title.isEmpty() && content.isEmpty()) {
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(this, R.string.note_empty_warning, Toast.LENGTH_SHORT).show();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (noteId != -1) {
            // Gọi tầng database để UPDATE ghi chú đã tồn tại của đúng user.
            dbHelper.updateNote(userId, noteId, title, content);
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show();
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Gọi tầng database để INSERT ghi chú mới vào bảng notes.
            long id = dbHelper.insertNote(userId, title, content);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (id != -1) {
                // Cập nhật ID của ghi chú đang mở theo trạng thái/kết quả vừa tính được.
                noteId = (int) id;
                // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show();
            }
        }
        // Cập nhật giá trị `isSaved` (is saved) theo trạng thái/kết quả vừa tính được.
        isSaved = true;
        // Gọi hàm `setResult()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setResult(RESULT_OK);
        // Gọi hàm `finish()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        finish();
    }
}
