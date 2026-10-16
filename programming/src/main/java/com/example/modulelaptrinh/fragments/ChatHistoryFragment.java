package com.example.modulelaptrinh.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modulelaptrinh.ProgrammingChatActivity;
import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.adapters.ProgrammingChatHistoryAdapter;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;
import com.example.modulelaptrinh.database.SessionItem;

import java.util.List;

/**
 * MÀN HÌNH LỊCH SỬ CHAT.
 * Đọc bảng sessions theo userId; khi mở một item sẽ truyền SESSION_ID sang ProgrammingChatActivity,
 * còn Delete sẽ xóa session và toàn bộ messages thuộc session đó.
 */
public class ChatHistoryFragment extends Fragment {
    // Tham chiếu `rvChatHistory` tới RecyclerView trong layout để đọc/cập nhật giao diện.
    private RecyclerView rvChatHistory;
    // Tham chiếu `tvEmptyHistory` tới TextView trong layout để đọc/cập nhật giao diện.
    private TextView tvEmptyHistory;
    // Tham chiếu `ivDeleteAll` tới ImageView trong layout để đọc/cập nhật giao diện.
    private ImageView ivDeleteAll;
    // Adapter `adapter` nối dữ liệu Java với RecyclerView tương ứng.
    private ProgrammingChatHistoryAdapter adapter;
    // Helper `dbHelper` chịu trách nhiệm đọc/ghi SQLite `ProgrammingMentor.db`.
    private ProgrammingDbHelper dbHelper;
    // Danh sách `sessionList` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<SessionItem> sessionList;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int userId;

    @Nullable
    @Override
    // Hàm `onCreateView()`: Tạo và trả về giao diện của Fragment từ layout XML.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = inflater.inflate(R.layout.fragment_chat_history, container, false);
        // Cập nhật ID của người dùng hiện tại theo trạng thái/kết quả vừa tính được.
        userId = ((com.example.modulelaptrinh.MainActivity) requireActivity()).getCurrentUserId();

        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        rvChatHistory = view.findViewById(R.id.rv_chat_history);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvEmptyHistory = view.findViewById(R.id.tv_empty_history);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        ivDeleteAll = view.findViewById(R.id.iv_delete_all);

        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
            dbHelper = new ProgrammingDbHelper(getContext());
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
            e.printStackTrace();
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(getContext(), "Lỗi khởi tạo database", Toast.LENGTH_SHORT).show();
        }

        // Gọi hàm `setupRecyclerView()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupRecyclerView();
        // Gọi hàm `loadHistory()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        loadHistory();

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        ivDeleteAll.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (dbHelper != null) {
                com.example.modulelaptrinh.util.ConfirmDeletion.show(requireContext(), R.string.program_delete_all_chats,
                        () -> dbHelper.deleteAllSessions(userId), this::loadHistory);
            }
        });

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return view;
    }

    // Hàm `setupRecyclerView()`: Cập nhật giá trị upRecyclerView cho đối tượng hiện tại.
    private void setupRecyclerView() {
        adapter = new ProgrammingChatHistoryAdapter(null, new ProgrammingChatHistoryAdapter.OnDeleteClickListener() {
            @Override
            // Hàm `onDeleteClick()`: Thực hiện chức năng `onDeleteClick` của class này.
            public void onDeleteClick(int position, SessionItem session) {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (dbHelper != null && session != null) {
                    com.example.modulelaptrinh.util.ConfirmDeletion.show(requireContext(), R.string.program_delete_chat,
                            () -> dbHelper.deleteSession(session.getId(), userId), ChatHistoryFragment.this::loadHistory);
                }
            }

            @Override
            // Hàm `onItemClick()`: Thực hiện chức năng `onItemClick` của class này.
            public void onItemClick(int position, SessionItem session) {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (getActivity() == null || session == null) return;

                // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
                try {
                    // Tạo Intent để điều hướng sang Activity khác hoặc thực hiện một action Android.
                    Intent intent = new Intent(getActivity(), ProgrammingChatActivity.class);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra("SESSION_ID", session.getId());
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID, userId);
                    // Yêu cầu Android mở Activity/action được mô tả bởi Intent.
                    startActivity(intent);
                // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
                } catch (Exception e) {
                    // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
                    e.printStackTrace();
                    // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                    Toast.makeText(getContext(), "Lỗi mở chat", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Thiết lập cách RecyclerView sắp xếp các item trên màn hình.
        rvChatHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        // Gắn Adapter vào RecyclerView để biến danh sách dữ liệu thành các item giao diện.
        rvChatHistory.setAdapter(adapter);
    }

    /** SELECT các session của đúng user hiện tại và cập nhật adapter. */
    private void loadHistory() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (dbHelper == null) {
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
                dbHelper = new ProgrammingDbHelper(getContext());
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception e) {
                // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
                e.printStackTrace();
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }
        }

        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Đọc danh sách session chat thuộc user hiện tại từ SQLite.
            sessionList = dbHelper.getAllSessions(userId);

            // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
            for (SessionItem session : sessionList) {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (session.getName().equals("Cuộc trò chuyện mới")) {
                    // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
                    try {
                        // Đọc toàn bộ message thuộc session để khôi phục nội dung cuộc trò chuyện.
                        List<com.example.modulelaptrinh.ProgrammingChatMessage> messages = dbHelper.getMessagesBySession(session.getId());
                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (!messages.isEmpty()) {
                            // Tính/đọc và lưu giá trị `firstMessage` (first message) để sử dụng ở các bước phía sau.
                            String firstMessage = messages.get(0).getContent();
                            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                            if (firstMessage != null && !firstMessage.isEmpty()) {
                                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                                if (firstMessage.length() > 30) {
                                    // Cập nhật giá trị `firstMessage` (first message) theo trạng thái/kết quả vừa tính được.
                                    firstMessage = firstMessage.substring(0, 30) + "...";
                                }
                                // Gọi `setName()` để cập nhật thuộc tính/trạng thái của `session`.
                                session.setName(firstMessage);
                            }
                        }
                    // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
                    } catch (Exception e) {
                        // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
                        e.printStackTrace();
                    }
                }
            }

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (sessionList.isEmpty()) {
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                tvEmptyHistory.setVisibility(View.VISIBLE);
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                rvChatHistory.setVisibility(View.GONE);
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                ivDeleteAll.setVisibility(View.GONE);
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                tvEmptyHistory.setVisibility(View.GONE);
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                rvChatHistory.setVisibility(View.VISIBLE);
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                ivDeleteAll.setVisibility(View.VISIBLE);
                // Gọi `updateData()` trên `adapter` để thực hiện thao tác tương ứng.
                adapter.updateData(sessionList);
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
            e.printStackTrace();
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(getContext(), "Lỗi tải lịch sử", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    // Hàm `onResume()`: Làm mới dữ liệu khi màn hình quay lại trạng thái đang hiển thị.
    public void onResume() {
        // Gọi implementation `onResume()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onResume();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (dbHelper != null) {
            // Gọi hàm `loadHistory()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            loadHistory();
        }
    }

}
