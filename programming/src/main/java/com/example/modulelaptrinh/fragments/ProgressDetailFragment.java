package com.example.modulelaptrinh.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.modulelaptrinh.MainActivity;
import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;
import com.example.modulelaptrinh.utils.ProgrammingLevelCalculator;

/**
 * MÀN HÌNH CHI TIẾT EXP/PROGRESS.
 * Không tự tạo EXP; chỉ đọc dữ liệu đã lưu từ ProgrammingDbHelper rồi tính level/progress để hiển thị.
 */
public class ProgressDetailFragment extends Fragment {

    private TextView tvLevel, tvTitle, tvTotalExp, tvExpProgress, tvProgressPercent;
    private TextView tvQuizCount, tvAccuracy, tvTotalExpAll;
    // Tham chiếu `ivBack` tới ImageView trong layout để đọc/cập nhật giao diện.
    private ImageView ivBack;
    // Thuộc tính `progressBar` lưu đối tượng `View` mà class cần dùng xuyên suốt nhiều hàm.
    private View progressBar;

    // Helper `dbHelper` chịu trách nhiệm đọc/ghi SQLite `ProgrammingMentor.db`.
    private ProgrammingDbHelper dbHelper;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int userId;

    @Nullable
    @Override
    // Hàm `onCreateView()`: Tạo và trả về giao diện của Fragment từ layout XML.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = inflater.inflate(R.layout.fragment_progress_detail, container, false);
        // Cập nhật ID của người dùng hiện tại theo trạng thái/kết quả vừa tính được.
        userId = ((MainActivity) requireActivity()).getCurrentUserId();

        // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
        dbHelper = new ProgrammingDbHelper(requireContext());

        // Gọi hàm `initViews()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        initViews(view);
        // Gọi hàm `loadProgressData()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        loadProgressData();
        // Gọi hàm `setupListeners()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupListeners();

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return view;
    }

    // Hàm `initViews()`: Ánh xạ các View XML sang biến Java để code có thể thao tác giao diện.
    private void initViews(View view) {
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        ivBack = view.findViewById(R.id.iv_back);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvLevel = view.findViewById(R.id.tv_level);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvTitle = view.findViewById(R.id.tv_title);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvTotalExp = view.findViewById(R.id.tv_total_exp);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExpProgress = view.findViewById(R.id.tv_exp_progress);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        progressBar = view.findViewById(R.id.progress_bar_exp);

        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvQuizCount = view.findViewById(R.id.tv_quiz_count);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvAccuracy = view.findViewById(R.id.tv_accuracy);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvTotalExpAll = view.findViewById(R.id.tv_total_exp_all);
    }

    // Hàm `setupListeners()`: Đăng ký các sự kiện tương tác của người dùng cho các View.
    private void setupListeners() {
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        ivBack.setOnClickListener(v -> {
            // Lấy FragmentManager để thực hiện chuyển/replace Fragment.
            requireActivity().getSupportFragmentManager().popBackStack();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("AI Mentor");
            }
        });
    }

    /** Đọc total_exp, quiz count, accuracy theo userId. */
    private void loadProgressData() {
        // Query tổng EXP của người dùng trên toàn bộ subject Programming.
        int totalExp = dbHelper.getTotalExpAllSubjects(userId);
        // Tính/đọc và lưu giá trị `totalExpAll` (total exp all) để sử dụng ở các bước phía sau.
        int totalExpAll = totalExp;

        // Tính/đọc và lưu mức độ khó để sử dụng ở các bước phía sau.
        int level = ProgrammingLevelCalculator.getLevel(totalExp);
        // Lấy chuỗi từ resources và lưu vào `title` để hỗ trợ đa ngôn ngữ.
        String title = getString(ProgrammingLevelCalculator.getTitleResId(level));
        // Tính/đọc và lưu giá trị `progressPercent` (progress percent) để sử dụng ở các bước phía sau.
        int progressPercent = ProgrammingLevelCalculator.getProgressPercent(totalExp);
        // Tính/đọc và lưu giá trị `expToNext` (exp to next) để sử dụng ở các bước phía sau.
        int expToNext = ProgrammingLevelCalculator.getExpToNextLevel(level);
        // Tính/đọc và lưu giá trị `expInLevel` (exp in level) để sử dụng ở các bước phía sau.
        int expInLevel = ProgrammingLevelCalculator.getExpInsideCurrentLevel(totalExp);

        // Query số lần người dùng đã hoàn thành Quiz.
        int quizCount = dbHelper.getQuizAttemptCountAllSubjects(userId);
        // Query và tính tỷ lệ trả lời đúng từ lịch sử Quiz đã lưu.
        double accuracy = dbHelper.getAverageAccuracyAllSubjects(userId);

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvLevel.setText(String.valueOf(level));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvTitle.setText(title);
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvTotalExp.setText(getString(R.string.prog_exp_format, totalExp));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExpProgress.setText(getString(R.string.prog_exp_detail_format, expInLevel, expToNext));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvProgressPercent.setText(getString(R.string.prog_percent_format, progressPercent));

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvQuizCount.setText(String.valueOf(quizCount));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvAccuracy.setText(accuracy > 0 ? getString(R.string.prog_percent_format, (int)accuracy) : "0%");
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvTotalExpAll.setText(String.valueOf(totalExpAll));
    }
}
