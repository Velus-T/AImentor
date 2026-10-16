package com.example.modulelaptrinh.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.modulelaptrinh.MainActivity;
import com.example.modulelaptrinh.R;

/**
 * MÀN HÌNH CẤU HÌNH QUIZ.
 * User nhập topic và số câu (3-15), sau đó startQuiz() truyền dữ liệu sang MainActivity.openQuiz().
 */
public class QuizSelectionFragment extends Fragment {

    // Tham chiếu `edtTopic` tới ô nhập liệu trong layout để đọc/cập nhật giao diện.
    private EditText edtTopic;
    // Tham chiếu `tvQuestionCount` tới TextView trong layout để đọc/cập nhật giao diện.
    private TextView tvQuestionCount;
    private ImageView ivDecrease, ivIncrease;
    // Tham chiếu `btnStartQuiz` tới nút bấm trong layout để đọc/cập nhật giao diện.
    private Button btnStartQuiz;

    // Tính/đọc và lưu số lượng câu hỏi cần tạo để sử dụng ở các bước phía sau.
    private int questionCount = 5;
    // Tính/đọc và lưu giá trị `MIN_QUESTIONS` (min questions) để sử dụng ở các bước phía sau.
    private final int MIN_QUESTIONS = 3;
    // Tính/đọc và lưu giá trị `MAX_QUESTIONS` (max questions) để sử dụng ở các bước phía sau.
    private final int MAX_QUESTIONS = 15;

    @Nullable
    @Override
    // Hàm `onCreateView()`: Tạo và trả về giao diện của Fragment từ layout XML.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = inflater.inflate(R.layout.fragment_quiz_selection, container, false);

        // Gọi hàm `initViews()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        initViews(view);
        // Gọi hàm `setupListeners()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupListeners();

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return view;
    }

    // Hàm `initViews()`: Ánh xạ các View XML sang biến Java để code có thể thao tác giao diện.
    private void initViews(View view) {
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        edtTopic = view.findViewById(R.id.edt_topic);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvQuestionCount = view.findViewById(R.id.tv_question_count);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        ivDecrease = view.findViewById(R.id.iv_decrease);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        ivIncrease = view.findViewById(R.id.iv_increase);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        btnStartQuiz = view.findViewById(R.id.btn_start_quiz);
    }

    // Hàm `setupListeners()`: Đăng ký các sự kiện tương tác của người dùng cho các View.
    private void setupListeners() {
        // Điều khiển tăng/giảm số lượng câu hỏi.
        ivDecrease.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (questionCount > MIN_QUESTIONS) {
                // Giảm `questionCount` đi 1 nhưng vẫn được giới hạn bởi điều kiện phía trên.
                questionCount--;
                // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
                tvQuestionCount.setText(String.valueOf(questionCount));
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                Toast.makeText(getContext(), "Tối thiểu " + MIN_QUESTIONS + " câu hỏi", Toast.LENGTH_SHORT).show();
            }
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        ivIncrease.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (questionCount < MAX_QUESTIONS) {
                // Tăng `questionCount` thêm 1 để chuyển/cập nhật sang trạng thái tiếp theo.
                questionCount++;
                // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
                tvQuestionCount.setText(String.valueOf(questionCount));
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                Toast.makeText(getContext(), "Tối đa " + MAX_QUESTIONS + " câu hỏi", Toast.LENGTH_SHORT).show();
            }
        });

        // Bắt đầu Quiz khi người dùng bấm nút.
        btnStartQuiz.setOnClickListener(v -> startQuiz());
    }

    /** Validate topic rồi bắt đầu luồng tạo Quiz AI. */
    private void startQuiz() {
        // Đọc nội dung hiện tại người dùng đã nhập/đang hiển thị trên View.
        String topic = edtTopic.getText().toString().trim();

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (topic.isEmpty()) {
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(getContext(), R.string.quiz_topic_empty, Toast.LENGTH_SHORT).show();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (getActivity() instanceof MainActivity) {
            // Level mặc định là "Trung bình" vì đã bỏ chọn cấp độ
            // Từ đây dữ liệu đi: MainActivity.openQuiz() -> QuizFragment.newInstance().
            ((MainActivity) getActivity()).openQuiz("Lập trình", topic, "Trung bình", questionCount);
        }
    }
}
