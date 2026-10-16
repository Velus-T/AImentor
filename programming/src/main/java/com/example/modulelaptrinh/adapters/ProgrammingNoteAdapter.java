package com.example.modulelaptrinh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.database.ProgrammingNote;

import java.util.List;

/** Bind ProgrammingNote lên RecyclerView và phát callback Edit/Delete. */
public class ProgrammingNoteAdapter extends RecyclerView.Adapter<ProgrammingNoteAdapter.NoteViewHolder> {
    // Danh sách `noteList` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<ProgrammingNote> noteList;
    // Thuộc tính `listener` lưu đối tượng `OnNoteClickListener` mà class cần dùng xuyên suốt nhiều hàm.
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(ProgrammingNote note);
        void onDeleteClick(int position, ProgrammingNote note);
    }

    // Constructor `ProgrammingNoteAdapter`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingNoteAdapter(List<ProgrammingNote> noteList, OnNoteClickListener listener) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `noteList` của đối tượng hiện tại.
        this.noteList = noteList;
        // Gán giá trị tham số/đầu vào vào thuộc tính `listener` của đối tượng hiện tại.
        this.listener = listener;
    }

    // Hàm `updateData()`: Cập nhật dữ liệu/trạng thái hiện tại theo giá trị mới.
    public void updateData(List<ProgrammingNote> newList) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `noteList` của đối tượng hiện tại.
        this.noteList = newList;
        // Thông báo Adapter rằng dữ liệu đã thay đổi để RecyclerView vẽ lại danh sách.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    // Hàm `onCreateViewHolder()`: Tạo ViewHolder mới từ layout item của RecyclerView.
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new NoteViewHolder(view);
    }

    @Override
    // Hàm `onBindViewHolder()`: Đưa dữ liệu của một item vào ViewHolder để RecyclerView hiển thị.
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // Tính/đọc và lưu giá trị `note` (note) để sử dụng ở các bước phía sau.
        ProgrammingNote note = noteList.get(position);
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        holder.tvNoteTitle.setText(note.getTitle());
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        holder.tvNotePreview.setText(note.getContent());
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        holder.tvNoteDate.setText(note.getTimestamp());

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        holder.itemView.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (listener != null) listener.onNoteClick(note);
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        holder.ivDeleteNote.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (listener != null) listener.onDeleteClick(position, note);
        });
    }

    @Override
    // Hàm `getItemCount()`: Trả về số lượng item hiện có để RecyclerView biết cần hiển thị bao nhiêu dòng.
    public int getItemCount() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return noteList != null ? noteList.size() : 0;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoteTitle, tvNotePreview, tvNoteDate;
        ImageView ivDeleteNote;

        // Constructor `NoteViewHolder`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
        public NoteViewHolder(@NonNull View itemView) {
            // Gọi hàm `super()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            super(itemView);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvNoteTitle = itemView.findViewById(R.id.tv_note_title);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvNotePreview = itemView.findViewById(R.id.tv_note_preview);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvNoteDate = itemView.findViewById(R.id.tv_note_date);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ivDeleteNote = itemView.findViewById(R.id.iv_delete_note);
        }
    }
}
