package com.example.modulelaptrinh.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modulelaptrinh.NoteEditorActivity;
import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.adapters.ProgrammingNoteAdapter;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;
import com.example.modulelaptrinh.database.ProgrammingNote;

import java.util.List;

/**
 * DANH SÁCH NOTES.
 * File này READ/SEARCH/DELETE notes; việc INSERT/UPDATE nằm ở NoteEditorActivity.
 * Database: bảng notes trong ProgrammingMentor.db, luôn lọc theo user_id.
 */
public class NoteFragment extends Fragment {

    // Tham chiếu `rvNotes` tới RecyclerView trong layout để đọc/cập nhật giao diện.
    private RecyclerView rvNotes;
    // Tham chiếu `etSearchNote` tới ô nhập liệu trong layout để đọc/cập nhật giao diện.
    private EditText etSearchNote;
    private ImageView ivAddNote, ivExportNotes;
    // Adapter `adapter` nối dữ liệu Java với RecyclerView tương ứng.
    private ProgrammingNoteAdapter adapter;
    // Helper `dbHelper` chịu trách nhiệm đọc/ghi SQLite `ProgrammingMentor.db`.
    private ProgrammingDbHelper dbHelper;
    // Danh sách `noteList` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<ProgrammingNote> noteList;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int userId;

    // FIX: Dùng ActivityResultLauncher để nhận kết quả
    private final ActivityResultLauncher<Intent> noteEditorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // FIX: Reload lại danh sách sau khi lưu
                    loadNotes();
                    // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                    Toast.makeText(getContext(), "Đã cập nhật danh sách ghi chú", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    // Hàm `onCreateView()`: Tạo và trả về giao diện của Fragment từ layout XML.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        // Cập nhật ID của người dùng hiện tại theo trạng thái/kết quả vừa tính được.
        userId = ((com.example.modulelaptrinh.MainActivity) requireActivity()).getCurrentUserId();

        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        rvNotes = view.findViewById(R.id.rv_notes);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        etSearchNote = view.findViewById(R.id.et_search_note);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        ivAddNote = view.findViewById(R.id.iv_add_note);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        ivExportNotes = view.findViewById(R.id.iv_export_notes);

        // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
        dbHelper = new ProgrammingDbHelper(getContext());

        // Gọi hàm `setupRecyclerView()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupRecyclerView();
        // Gọi hàm `loadNotes()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        loadNotes();

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        ivAddNote.setOnClickListener(v -> openNoteEditor(null));

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        ivExportNotes.setOnClickListener(v -> shareNotes());

        etSearchNote.addTextChangedListener(new TextWatcher() {
            @Override
            // Hàm `beforeTextChanged()`: Thực hiện chức năng `beforeTextChanged` của class này.
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            // Hàm `onTextChanged()`: Thực hiện chức năng `onTextChanged` của class này.
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            // Hàm `afterTextChanged()`: Thực hiện chức năng `afterTextChanged` của class này.
            public void afterTextChanged(Editable s) {
                // Gọi hàm `searchNotes()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                searchNotes(s.toString());
            }
        });

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return view;
    }

    // Hàm `setupRecyclerView()`: Cập nhật giá trị upRecyclerView cho đối tượng hiện tại.
    private void setupRecyclerView() {
        adapter = new ProgrammingNoteAdapter(null, new ProgrammingNoteAdapter.OnNoteClickListener() {
            @Override
            // Hàm `onNoteClick()`: Thực hiện chức năng `onNoteClick` của class này.
            public void onNoteClick(ProgrammingNote note) {
                // Gọi hàm `openNoteEditor()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                openNoteEditor(note);
            }

            @Override
            // Hàm `onDeleteClick()`: Thực hiện chức năng `onDeleteClick` của class này.
            public void onDeleteClick(int position, ProgrammingNote note) {
                // Gọi `deleteNote()` trên `dbHelper` để thực hiện thao tác tương ứng.
                dbHelper.deleteNote(userId, note.getId());
                // Gọi hàm `loadNotes()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                loadNotes();
                // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                Toast.makeText(getContext(), "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
            }
        });

        // Thiết lập cách RecyclerView sắp xếp các item trên màn hình.
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        // Gắn Adapter vào RecyclerView để biến danh sách dữ liệu thành các item giao diện.
        rvNotes.setAdapter(adapter);
    }

    /** SELECT * FROM notes WHERE user_id = currentUser. */
    private void loadNotes() {
        // Đọc danh sách ghi chú thuộc user hiện tại từ SQLite.
        noteList = dbHelper.getAllNotes(userId);
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (adapter != null) {
            // Gọi `updateData()` trên `adapter` để thực hiện thao tác tương ứng.
            adapter.updateData(noteList);
        }
    }

    /** Search title/content nhưng vẫn bắt buộc lọc theo userId. */
    private void searchNotes(String keyword) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (keyword.isEmpty()) {
            // Gọi hàm `loadNotes()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            loadNotes();
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Cập nhật danh sách ghi chú theo trạng thái/kết quả vừa tính được.
            noteList = dbHelper.searchNotes(userId, keyword);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (adapter != null) {
                // Gọi `updateData()` trên `adapter` để thực hiện thao tác tương ứng.
                adapter.updateData(noteList);
            }
        }
    }

    /** Share dùng ACTION_SEND, không ghi thêm dữ liệu vào database. */
    private void shareNotes() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (noteList == null || noteList.isEmpty()) {
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(getContext(), R.string.note_share_empty, Toast.LENGTH_SHORT).show();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }
        // Lấy chuỗi từ resources và lưu vào `content` để hỗ trợ đa ngôn ngữ.
        StringBuilder content = new StringBuilder(getString(R.string.note_share_header) + "\n\n");
        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (ProgrammingNote note : noteList) {
            content.append("• ").append(note.getTitle() == null ? getString(R.string.note_share_default_title) : note.getTitle())
                    .append("\n")
                    .append(note.getContent() == null ? "" : note.getContent())
                    .append("\n\n");
        }
        // Tạo Intent để điều hướng sang Activity khác hoặc thực hiện một action Android.
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // Gọi `setType()` để cập nhật thuộc tính/trạng thái của `shareIntent`.
        shareIntent.setType("text/plain");
        // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.note_share_subject));
        // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
        shareIntent.putExtra(Intent.EXTRA_TEXT, content.toString().trim());
        // Yêu cầu Android mở Activity/action được mô tả bởi Intent.
        startActivity(Intent.createChooser(shareIntent, getString(R.string.note_share_chooser_title)));
    }

    /**
     * Mở NoteEditorActivity; nếu edit thì truyền NOTE_ID/title/content, nếu create thì để trống.
     */
    private void openNoteEditor(ProgrammingNote existingNote) {
        // Tạo Intent để điều hướng sang Activity khác hoặc thực hiện một action Android.
        Intent intent = new Intent(getActivity(), NoteEditorActivity.class);
        // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
        intent.putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID, userId);
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (existingNote != null) {
            // FIX: Dùng putExtra với int
            intent.putExtra("NOTE_ID", existingNote.getId());
            // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
            intent.putExtra("NOTE_TITLE", existingNote.getTitle());
            // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
            intent.putExtra("NOTE_CONTENT", existingNote.getContent());
        }
        // FIX: Dùng launcher thay vì startActivityForResult
        noteEditorLauncher.launch(intent);
    }
    @Override
    // Hàm `onResume()`: Làm mới dữ liệu khi màn hình quay lại trạng thái đang hiển thị.
    public void onResume() {
        // Gọi implementation `onResume()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onResume();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (dbHelper != null) {
            // Gọi hàm `loadNotes()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            loadNotes();
        }
    }

}
