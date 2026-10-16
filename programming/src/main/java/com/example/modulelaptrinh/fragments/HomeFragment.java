package com.example.modulelaptrinh.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.modulelaptrinh.MainActivity;
import com.example.modulelaptrinh.ProgrammingChatActivity;
import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;
import com.example.modulelaptrinh.utils.ProgrammingLevelCalculator;

import java.util.HashMap;
import java.util.Map;

/**
 * HOME CỦA PROGRAMMING MODULE.
 *
 * <p>File này xử lý 3 nhóm chức năng:</p>
 * <ul>
 *   <li>Chọn ngôn ngữ -> tạo Chat Session -> mở ProgrammingChatActivity.</li>
 *   <li>Ô nhập câu hỏi nhanh -> tạo session và mở Chat.</li>
 *   <li>Đọc EXP/Quiz count/Accuracy từ ProgrammingDbHelper để hiển thị progress.</li>
 * </ul>
 */
public class HomeFragment extends Fragment {
    private LinearLayout llPython, llJava, llKotlin, llJavascript;
    private LinearLayout llCpp, llCsharp, llPhp, llSwift;
    private LinearLayout llGo, llRust, llTypescript, llRuby;
    private LinearLayout llExpand, llCollapse, row2, row3, llSendButton;
    // Tham chiếu `cardExp` tới CardView trong layout để đọc/cập nhật giao diện.
    private CardView cardExp;
    private TextView tvExpandLabel, tvLangCount;

    // EXP Card views
    private TextView tvExpLevel, tvExpTitle, tvExpTotal, tvExpProgress, tvExpPercent;
    private TextView tvQuizCount, tvAccuracy, tvTotalExpAll;
    // Tham chiếu `pbExp` tới thanh tiến trình trong layout để đọc/cập nhật giao diện.
    private ProgressBar pbExp;

    // Tham chiếu `etQuestionInput` tới ô nhập liệu trong layout để đọc/cập nhật giao diện.
    private EditText etQuestionInput;
    // Helper `dbHelper` chịu trách nhiệm đọc/ghi SQLite `ProgrammingMentor.db`.
    private ProgrammingDbHelper dbHelper;

    // Tính/đọc và lưu giá trị `isExpanded` (is expanded) để sử dụng ở các bước phía sau.
    private boolean isExpanded = false;
    // Tính/đọc và lưu giá trị `isProcessing` (is processing) để sử dụng ở các bước phía sau.
    private boolean isProcessing = false;

    // Khởi tạo giá trị `languageQuestions` (language questions) phục vụ cho bước xử lý tiếp theo.
    private Map<String, String> languageQuestions = new HashMap<>();
    // Tính/đọc và lưu giá trị `totalLanguages` (total languages) để sử dụng ở các bước phía sau.
    private int totalLanguages = 12;
    // Tính/đọc và lưu giá trị `completedLanguages` (completed languages) để sử dụng ở các bước phía sau.
    private int completedLanguages = 4;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int userId;

    @Nullable
    @Override
    // Hàm `onCreateView()`: Tạo và trả về giao diện của Fragment từ layout XML.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = inflater.inflate(R.layout.fragment_programming_home, container, false);
        // Cập nhật ID của người dùng hiện tại theo trạng thái/kết quả vừa tính được.
        userId = ((MainActivity) requireActivity()).getCurrentUserId();

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

        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("Python", "Tôi muốn học Python từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("Java", "Tôi muốn học Java từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("Kotlin", "Tôi muốn học Kotlin để phát triển Android.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("JavaScript", "Tôi muốn học JavaScript từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("C++", "Tôi muốn học C++ từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("C#", "Tôi muốn học C# từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("PHP", "Tôi muốn học PHP từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("Swift", "Tôi muốn học Swift để phát triển iOS.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("Go", "Tôi muốn học Go từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("Rust", "Tôi muốn học Rust từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("TypeScript", "Tôi muốn học TypeScript từ cơ bản đến nâng cao.");
        // Gọi `put()` trên `languageQuestions` để thực hiện thao tác tương ứng.
        languageQuestions.put("Ruby", "Tôi muốn học Ruby từ cơ bản đến nâng cao.");

        // Language views
        llPython = view.findViewById(R.id.ll_python);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llJava = view.findViewById(R.id.ll_java);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llKotlin = view.findViewById(R.id.ll_kotlin);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llJavascript = view.findViewById(R.id.ll_javascript);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llCpp = view.findViewById(R.id.ll_cpp);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llCsharp = view.findViewById(R.id.ll_csharp);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llPhp = view.findViewById(R.id.ll_php);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llSwift = view.findViewById(R.id.ll_swift);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llGo = view.findViewById(R.id.ll_go);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llRust = view.findViewById(R.id.ll_rust);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llTypescript = view.findViewById(R.id.ll_typescript);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llRuby = view.findViewById(R.id.ll_ruby);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        row2 = view.findViewById(R.id.row_2);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        row3 = view.findViewById(R.id.row_3);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llExpand = view.findViewById(R.id.ll_expand);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llCollapse = view.findViewById(R.id.ll_collapse);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExpandLabel = view.findViewById(R.id.tv_expand_label);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvLangCount = view.findViewById(R.id.tv_lang_count);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        etQuestionInput = view.findViewById(R.id.et_question_input);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llSendButton = view.findViewById(R.id.ll_send_button);

        // EXP Card views - SỬA: dùng CardView
        cardExp = view.findViewById(R.id.card_exp);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExpLevel = view.findViewById(R.id.tv_exp_level);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExpTitle = view.findViewById(R.id.tv_exp_title);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExpTotal = view.findViewById(R.id.tv_exp_total);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExpProgress = view.findViewById(R.id.tv_exp_progress);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExpPercent = view.findViewById(R.id.tv_exp_percent);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        pbExp = view.findViewById(R.id.pb_exp);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvQuizCount = view.findViewById(R.id.tv_quiz_count);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvAccuracy = view.findViewById(R.id.tv_accuracy);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvTotalExpAll = view.findViewById(R.id.tv_total_exp_all);

        // Gọi hàm `updateLangCount()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        updateLangCount();
        // Gọi hàm `updateExpData()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        updateExpData();
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        row2.setVisibility(View.GONE);
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        row3.setVisibility(View.GONE);
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        llCollapse.setVisibility(View.GONE);

        // Gọi hàm `setupExpandCollapse()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupExpandCollapse();
        // Gọi hàm `setupLanguageClicks()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupLanguageClicks();
        // Gọi hàm `setupChatInput()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupChatInput();
        // Gọi hàm `setupExpCardClick()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupExpCardClick();

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return view;
    }

    /**
     * Đọc user_progress + programming_quiz_attempts để dựng thẻ EXP.
     */
    private void updateExpData() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (dbHelper == null) return;

        // Tổng EXP đã persist trong bảng user_progress.
        int totalExp = dbHelper.getTotalExpAllSubjects(userId);
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

        // Quiz count và accuracy được tính từ bảng programming_quiz_attempts.
        int quizCount = dbHelper.getQuizAttemptCountAllSubjects(userId);
        // Query và tính tỷ lệ trả lời đúng từ lịch sử Quiz đã lưu.
        double accuracy = dbHelper.getAverageAccuracyAllSubjects(userId);
        // Query tổng EXP của người dùng trên toàn bộ subject Programming.
        int totalExpAll = dbHelper.getTotalExpAllSubjects(userId);

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExpLevel.setText(getString(R.string.prog_level_format, level));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExpTitle.setText(title);
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExpTotal.setText(getString(R.string.prog_exp_format, totalExp));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExpProgress.setText(getString(R.string.prog_exp_detail_format, expInLevel, expToNext));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExpPercent.setText(getString(R.string.prog_percent_format, progressPercent));
        // Gọi `setProgress()` để cập nhật thuộc tính/trạng thái của `pbExp`.
        pbExp.setProgress(progressPercent);

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvQuizCount.setText(String.valueOf(quizCount));
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvAccuracy.setText(accuracy > 0 ? getString(R.string.prog_percent_format, (int)accuracy) : "0%");
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvTotalExpAll.setText(String.valueOf(totalExpAll));
    }

    // Hàm `setupExpCardClick()`: Cập nhật giá trị upExpCardClick cho đối tượng hiện tại.
    private void setupExpCardClick() {
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        cardExp.setOnClickListener(v -> {
            // Khởi tạo giá trị `fragment` (fragment) phục vụ cho bước xử lý tiếp theo.
            ProgressDetailFragment fragment = new ProgressDetailFragment();
            // Lấy FragmentManager để thực hiện chuyển/replace Fragment.
            requireActivity().getSupportFragmentManager()
                    // Bắt đầu transaction để nhóm nhiều thao tác database thành một khối nguyên tử.
                    .beginTransaction()
                    // Thay Fragment hiện tại trong container bằng Fragment mới.
                    .replace(R.id.fragment_container, fragment)
                    // Đưa thao tác Fragment vào back stack để nút Back có thể quay lại màn hình trước.
                    .addToBackStack("progress")
                    // Commit transaction để Android thực sự áp dụng thay đổi Fragment.
                    .commit();

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.prog_quiz_progress_title);
            }
        });
    }

    // Hàm `updateLangCount()`: Cập nhật dữ liệu/trạng thái hiện tại theo giá trị mới.
    private void updateLangCount() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (isExpanded) {
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            tvLangCount.setText(getString(R.string.prog_lang_count_format, totalLanguages, totalLanguages));
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            tvLangCount.setText(getString(R.string.prog_lang_count_format, completedLanguages, totalLanguages));
        }
    }

    // Hàm `setupExpandCollapse()`: Cập nhật giá trị upExpandCollapse cho đối tượng hiện tại.
    private void setupExpandCollapse() {
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llExpand.setOnClickListener(v -> {
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            row2.setVisibility(View.VISIBLE);
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            row3.setVisibility(View.VISIBLE);
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            llExpand.setVisibility(View.GONE);
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            llCollapse.setVisibility(View.VISIBLE);
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            tvExpandLabel.setText(R.string.collapse);
            // Cập nhật giá trị `isExpanded` (is expanded) theo trạng thái/kết quả vừa tính được.
            isExpanded = true;
            // Gọi hàm `updateLangCount()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            updateLangCount();
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llCollapse.setOnClickListener(v -> {
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            row2.setVisibility(View.GONE);
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            row3.setVisibility(View.GONE);
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            llCollapse.setVisibility(View.GONE);
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            llExpand.setVisibility(View.VISIBLE);
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            tvExpandLabel.setText(R.string.see_more);
            // Cập nhật giá trị `isExpanded` (is expanded) theo trạng thái/kết quả vừa tính được.
            isExpanded = false;
            // Gọi hàm `updateLangCount()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            updateLangCount();
        });
    }

    /**
     * Khi bấm Python/Java/...: xác định language -> tạo session DB -> lưu welcome -> mở Chat.
     */
    private void setupLanguageClicks() {
        // Tính/đọc và lưu giá trị `listener` (listener) để sử dụng ở các bước phía sau.
        View.OnClickListener listener = v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (isProcessing) {
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (getActivity() == null || getContext() == null) {
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }

            // Cập nhật giá trị `isProcessing` (is processing) theo trạng thái/kết quả vừa tính được.
            isProcessing = true;

            // Tính/đọc và lưu giá trị `lang` (lang) để sử dụng ở các bước phía sau.
            String lang = "";
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (v == llPython) lang = "Python";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llJava) lang = "Java";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llKotlin) lang = "Kotlin";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llJavascript) lang = "JavaScript";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llCpp) lang = "C++";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llCsharp) lang = "C#";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llPhp) lang = "PHP";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llSwift) lang = "Swift";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llGo) lang = "Go";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llRust) lang = "Rust";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llTypescript) lang = "TypeScript";
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            else if (v == llRuby) lang = "Ruby";

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (!lang.isEmpty()) {
                // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
                try {
                    // Tính/đọc và lưu giá trị `question` (question) để sử dụng ở các bước phía sau.
                    String question = languageQuestions.getOrDefault(lang, "I want to learn " + lang + " from basic to advanced.");

                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (dbHelper == null) {
                        // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
                        dbHelper = new ProgrammingDbHelper(getContext());
                    }

                    // INSERT một record mới vào bảng sessions.
                    long sessionId = dbHelper.createNewSession(userId);

                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (sessionId == -1) {
                        // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                        Toast.makeText(getContext(), R.string.prog_chat_error_create_session, Toast.LENGTH_SHORT).show();
                        // Cập nhật giá trị `isProcessing` (is processing) theo trạng thái/kết quả vừa tính được.
                        isProcessing = false;
                        // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                        return;
                    }

                    // Lấy chuỗi từ resources và lưu vào `welcomeMessage` để hỗ trợ đa ngôn ngữ.
                    String welcomeMessage = getString(R.string.prog_chat_welcome_format, lang);
                    // Welcome là message của AI nên isUser=false.
                    dbHelper.insertMessage(sessionId, welcomeMessage, false, null);

                    // Tạo Intent để điều hướng sang Activity khác hoặc thực hiện một action Android.
                    Intent intent = new Intent(getActivity(), ProgrammingChatActivity.class);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra("SESSION_ID", sessionId);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra("QUESTION", question);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra("LANGUAGE", lang);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra(MainActivity.EXTRA_USER_ID, userId);
                    // Yêu cầu Android mở Activity/action được mô tả bởi Intent.
                    startActivity(intent);

                // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
                } catch (Exception e) {
                    // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
                    e.printStackTrace();
                    // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            // Cập nhật giá trị `isProcessing` (is processing) theo trạng thái/kết quả vừa tính được.
            isProcessing = false;
        };

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llPython.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llJava.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llKotlin.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llJavascript.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llCpp.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llCsharp.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llPhp.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llSwift.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llGo.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llRust.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llTypescript.setOnClickListener(listener);
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llRuby.setOnClickListener(listener);
    }

    /** Xử lý ô hỏi nhanh ở Home; luồng DB giống khi chọn một language card. */
    private void setupChatInput() {
        etQuestionInput.addTextChangedListener(new TextWatcher() {
            @Override
            // Hàm `beforeTextChanged()`: Thực hiện chức năng `beforeTextChanged` của class này.
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            // Hàm `onTextChanged()`: Thực hiện chức năng `onTextChanged` của class này.
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            // Hàm `afterTextChanged()`: Thực hiện chức năng `afterTextChanged` của class này.
            public void afterTextChanged(Editable s) {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (llSendButton != null) {
                    // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                    llSendButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
            }
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        llSendButton.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (isProcessing) return;

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (getActivity() == null || getContext() == null) {
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }

            // Đọc nội dung hiện tại người dùng đã nhập/đang hiển thị trên View.
            String question = etQuestionInput.getText().toString().trim();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (!question.isEmpty()) {
                // Cập nhật giá trị `isProcessing` (is processing) theo trạng thái/kết quả vừa tính được.
                isProcessing = true;
                // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
                try {
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (dbHelper == null) {
                        // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
                        dbHelper = new ProgrammingDbHelper(getContext());
                    }

                    // Tạo một phiên chat mới trong bảng sessions và nhận lại sessionId.
                    long sessionId = dbHelper.createNewSession(userId);

                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (sessionId == -1) {
                        // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                        Toast.makeText(getContext(), "Lỗi tạo phiên chat", Toast.LENGTH_SHORT).show();
                        // Cập nhật giá trị `isProcessing` (is processing) theo trạng thái/kết quả vừa tính được.
                        isProcessing = false;
                        // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                        return;
                    }

                    // Tính/đọc và lưu giá trị `welcomeMessage` (welcome message) để sử dụng ở các bước phía sau.
                    String welcomeMessage = "Xin chào! Tôi là AI Mentor Lập trình. Bạn có câu hỏi nào không?";
                    // Gọi tầng database để persist tin nhắn hiện tại vào bảng messages.
                    dbHelper.insertMessage(sessionId, welcomeMessage, false, null);

                    // Tạo Intent để điều hướng sang Activity khác hoặc thực hiện một action Android.
                    Intent intent = new Intent(getActivity(), ProgrammingChatActivity.class);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra("SESSION_ID", sessionId);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra("QUESTION", question);
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra("LANGUAGE", "Lập trình");
                    // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
                    intent.putExtra(MainActivity.EXTRA_USER_ID, userId);
                    // Yêu cầu Android mở Activity/action được mô tả bởi Intent.
                    startActivity(intent);
                    // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
                    etQuestionInput.setText("");

                // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
                } catch (Exception e) {
                    // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
                    e.printStackTrace();
                    // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // Cập nhật giá trị `isProcessing` (is processing) theo trạng thái/kết quả vừa tính được.
                isProcessing = false;
            }
        });
    }

    @Override
    // Hàm `onResume()`: Làm mới dữ liệu khi màn hình quay lại trạng thái đang hiển thị.
    public void onResume() {
        // Gọi implementation `onResume()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onResume();
        // Gọi hàm `updateExpData()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        updateExpData();
    }
}
