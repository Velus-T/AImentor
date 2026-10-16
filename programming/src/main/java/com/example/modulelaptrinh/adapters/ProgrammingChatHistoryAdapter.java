package com.example.modulelaptrinh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.database.SessionItem;

import java.util.List;

/** Bind SessionItem lên RecyclerView History và phát callback Open/Delete. */
public class ProgrammingChatHistoryAdapter extends RecyclerView.Adapter<ProgrammingChatHistoryAdapter.HistoryViewHolder> {
    // Danh sách `sessionList` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<SessionItem> sessionList;
    // Thuộc tính `listener` lưu đối tượng `OnDeleteClickListener` mà class cần dùng xuyên suốt nhiều hàm.
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, SessionItem session);
        void onItemClick(int position, SessionItem session);
    }

    // Constructor `ProgrammingChatHistoryAdapter`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingChatHistoryAdapter(List<SessionItem> sessionList, OnDeleteClickListener listener) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `sessionList` của đối tượng hiện tại.
        this.sessionList = sessionList;
        // Gán giá trị tham số/đầu vào vào thuộc tính `listener` của đối tượng hiện tại.
        this.listener = listener;
    }

    // Hàm `updateData()`: Cập nhật dữ liệu/trạng thái hiện tại theo giá trị mới.
    public void updateData(List<SessionItem> newList) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `sessionList` của đối tượng hiện tại.
        this.sessionList = newList;
        // Thông báo Adapter rằng dữ liệu đã thay đổi để RecyclerView vẽ lại danh sách.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    // Hàm `onCreateViewHolder()`: Tạo ViewHolder mới từ layout item của RecyclerView.
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new HistoryViewHolder(view);
    }

    @Override
    // Hàm `onBindViewHolder()`: Đưa dữ liệu của một item vào ViewHolder để RecyclerView hiển thị.
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        // Tính/đọc và lưu giá trị `session` (session) để sử dụng ở các bước phía sau.
        SessionItem session = sessionList.get(position);

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        holder.tvChatQuestion.setText(session.getName());
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        holder.tvChatTime.setText(session.getTimestamp());
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        holder.tvChatAnswer.setVisibility(View.GONE);

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        holder.ivDeleteSession.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (listener != null) {
                // Gọi `onDeleteClick()` trên `listener` để thực hiện thao tác tương ứng.
                listener.onDeleteClick(position, session);
            }
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        holder.itemView.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (listener != null) {
                // Gọi `onItemClick()` trên `listener` để thực hiện thao tác tương ứng.
                listener.onItemClick(position, session);
            }
        });
    }

    @Override
    // Hàm `getItemCount()`: Trả về số lượng item hiện có để RecyclerView biết cần hiển thị bao nhiêu dòng.
    public int getItemCount() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return sessionList != null ? sessionList.size() : 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatQuestion, tvChatAnswer, tvChatTime;
        ImageView ivDeleteSession;

        // Constructor `HistoryViewHolder`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
        public HistoryViewHolder(@NonNull View itemView) {
            // Gọi hàm `super()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            super(itemView);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvChatQuestion = itemView.findViewById(R.id.tv_chat_question);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvChatAnswer = itemView.findViewById(R.id.tv_chat_answer);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvChatTime = itemView.findViewById(R.id.tv_chat_time);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ivDeleteSession = itemView.findViewById(R.id.iv_delete_session);
        }
    }
}
