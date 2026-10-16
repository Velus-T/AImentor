package com.example.modulelaptrinh.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.modulelaptrinh.MainActivity;
import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;
import com.example.modulelaptrinh.models.QuizQuestion;
import com.example.modulelaptrinh.models.QuizRequest;
import com.example.modulelaptrinh.models.QuizResult;
import com.example.modulelaptrinh.utils.ProgrammingLevelCalculator;
import com.example.modulelaptrinh.utils.ProgrammingQuizGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * TRUNG TÂM CỦA QUIZ PROGRAMMING.
 *
 * <pre>
 * QuizSelectionFragment
 *   -> MainActivity.openQuiz()
 *   -> QuizFragment.initQuiz()
 *   -> ProgrammingQuizGenerator.generateQuiz()
 *   -> GeminiApiClient
 *   -> List&lt;QuizQuestion&gt;
 *   -> showQuestion()/handleOptionClick()
 *   -> finishQuiz()
 *   -> ProgrammingDbHelper.saveQuizResult()
 * </pre>
 *
 * <p>Lưu ý: câu hỏi đang làm nằm trong RAM; DB chỉ persist kết quả cuối + EXP.</p>
 */
public class QuizFragment extends Fragment {
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_TOPIC = "topic";
    private static final String ARG_LEVEL = "level";
    private static final String ARG_QUESTION_COUNT = "question_count";

    private TextView tvProgress, tvDifficulty, tvQuestion, tvExplanation;
    // Tham chiếu `pbQuiz` tới thanh tiến trình trong layout để đọc/cập nhật giao diện.
    private ProgressBar pbQuiz;
    // Thuộc tính `llOptions` lưu đối tượng `LinearLayout` mà class cần dùng xuyên suốt nhiều hàm.
    private LinearLayout llOptions;
    // Tham chiếu `btnNext` tới nút bấm trong layout để đọc/cập nhật giao diện.
    private Button btnNext;
    // Tham chiếu `cardExplanation` tới CardView trong layout để đọc/cập nhật giao diện.
    private CardView cardExplanation;

    // Tham chiếu `optionCards` tới CardView trong layout để đọc/cập nhật giao diện.
    private CardView[] optionCards;
    // Tham chiếu `optionTexts` tới TextView trong layout để đọc/cập nhật giao diện.
    private TextView[] optionTexts;

    // Khởi tạo danh sách câu hỏi quiz phục vụ cho bước xử lý tiếp theo.
    private List<QuizQuestion> questions = new ArrayList<>();
    // Tính/đọc và lưu vị trí câu hỏi hiện tại để sử dụng ở các bước phía sau.
    private int currentIndex = 0;
    // Tính/đọc và lưu số câu trả lời đúng để sử dụng ở các bước phía sau.
    private int correctAnswers = 0;
    // Tính/đọc và lưu tổng EXP nhận được trong quiz hiện tại để sử dụng ở các bước phía sau.
    private int totalExpEarned = 0;
    // Tính/đọc và lưu giá trị `isAnswered` (is answered) để sử dụng ở các bước phía sau.
    private boolean isAnswered = false;
    // Tính/đọc và lưu giá trị `isLoading` (is loading) để sử dụng ở các bước phía sau.
    private boolean isLoading = false;
    // Tính/đọc và lưu giá trị `isFinishingQuiz` (is finishing quiz) để sử dụng ở các bước phía sau.
    private boolean isFinishingQuiz = false;
    private int viewGeneration;

    // Helper `dbHelper` chịu trách nhiệm đọc/ghi SQLite `ProgrammingMentor.db`.
    private ProgrammingDbHelper dbHelper;
    // Đối tượng `quizGenerator` điều phối việc xây prompt, gọi Gemini và parse câu hỏi Quiz.
    private ProgrammingQuizGenerator quizGenerator;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int userId;
    // Biến `progressDialog` giữ dialog dùng để hiển thị trạng thái/thông báo cho người dùng.
    private ProgressDialog progressDialog;

    /** Đóng gói cấu hình Quiz vào Bundle để Fragment nhận lại sau khi được mở. */
    public static QuizFragment newInstance(String subject, String topic, String level, int questionCount) {
        // Tạo Bundle để đóng gói tham số truyền cho Fragment.
        Bundle args = new Bundle();
        // Đưa giá trị vào Bundle theo key để Fragment có thể lấy lại sau đó.
        args.putString(ARG_SUBJECT, subject);
        // Đưa giá trị vào Bundle theo key để Fragment có thể lấy lại sau đó.
        args.putString(ARG_TOPIC, topic);
        // Đưa giá trị vào Bundle theo key để Fragment có thể lấy lại sau đó.
        args.putString(ARG_LEVEL, level);
        // Đưa giá trị vào Bundle theo key để Fragment có thể lấy lại sau đó.
        args.putInt(ARG_QUESTION_COUNT, questionCount);
        // Khởi tạo giá trị `fragment` (fragment) phục vụ cho bước xử lý tiếp theo.
        QuizFragment fragment = new QuizFragment();
        // Gắn Bundle tham số vào Fragment trước khi Fragment được hiển thị.
        fragment.setArguments(args);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return fragment;
    }

    @Nullable
    @Override
    // Hàm `onCreateView()`: Tạo và trả về giao diện của Fragment từ layout XML.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        viewGeneration++;
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        // Cập nhật ID của người dùng hiện tại theo trạng thái/kết quả vừa tính được.
        userId = ((MainActivity) requireActivity()).getCurrentUserId();

        // Gọi hàm `initViews()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        initViews(view);
        // Gọi hàm `initQuiz()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        initQuiz();

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return view;
    }

    // Hàm `initViews()`: Ánh xạ các View XML sang biến Java để code có thể thao tác giao diện.
    private void initViews(View view) {
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvProgress = view.findViewById(R.id.tv_quiz_progress);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvDifficulty = view.findViewById(R.id.tv_question_difficulty);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        pbQuiz = view.findViewById(R.id.pb_quiz);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvQuestion = view.findViewById(R.id.tv_question);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        llOptions = view.findViewById(R.id.ll_options);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        tvExplanation = view.findViewById(R.id.tv_explanation);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        cardExplanation = view.findViewById(R.id.card_explanation);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        btnNext = view.findViewById(R.id.btn_next_quiz);

        optionCards = new CardView[]{
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.cv_option_a),
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.cv_option_b),
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.cv_option_c),
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.cv_option_d)
        };

        optionTexts = new TextView[]{
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.tv_option_a),
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.tv_option_b),
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.tv_option_c),
                // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
                view.findViewById(R.id.tv_option_d)
        };

        // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
        dbHelper = new ProgrammingDbHelper(requireContext());
        // Cập nhật giá trị `quizGenerator` (quiz generator) theo trạng thái/kết quả vừa tính được.
        quizGenerator = new ProgrammingQuizGenerator();
    }

    /**
     * Tạo QuizRequest rồi chạy ProgrammingQuizGenerator ở background thread.
     */
    private void initQuiz() {
        final int generation = viewGeneration;
        // Đọc Bundle tham số đã truyền vào Fragment.
        String subject = getArguments() != null ? getArguments().getString(ARG_SUBJECT, "Lập trình") : "Lập trình";
        // Đọc Bundle tham số đã truyền vào Fragment.
        String topic = getArguments() != null ? getArguments().getString(ARG_TOPIC, "Kiến thức lập trình") : "Kiến thức lập trình";
        // Đọc Bundle tham số đã truyền vào Fragment.
        String level = getArguments() != null ? getArguments().getString(ARG_LEVEL, "Trung bình") : "Trung bình";
        // Đọc Bundle tham số đã truyền vào Fragment.
        int questionCount = getArguments() != null ? getArguments().getInt(ARG_QUESTION_COUNT, 5) : 5;

        // Khởi tạo hộp thoại loading để báo cho người dùng biết tác vụ đang xử lý.
        progressDialog = new ProgressDialog(getContext());
        // Gọi `setMessage()` để cập nhật thuộc tính/trạng thái của `progressDialog`.
        progressDialog.setMessage("Đang tạo câu hỏi...");
        // Gọi `setCancelable()` để cập nhật thuộc tính/trạng thái của `progressDialog`.
        progressDialog.setCancelable(false);
        // Hiển thị dialog/loading lên màn hình.
        progressDialog.show();

        // Gọi hàm `setOptionsEnabled()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setOptionsEnabled(false);
        // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
        btnNext.setEnabled(false);
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvQuestion.setText("Đang tạo câu hỏi...");
        // Cập nhật giá trị `isLoading` (is loading) theo trạng thái/kết quả vừa tính được.
        isLoading = true;

        // Tạo request với số câu hỏi tùy chỉnh
        // Object này chứa subject/topic/level/số câu để generator xây prompt.
        QuizRequest request = new QuizRequest(subject, level, topic, questionCount);

        // Tạo luồng nền để tác vụ nặng/network không chặn UI thread.
        new Thread(() -> {
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Generator gọi Gemini và parse JSON thành List<QuizQuestion>.
                QuizResult result = quizGenerator.generateQuiz(request, userId);
                // Cập nhật danh sách câu hỏi quiz theo trạng thái/kết quả vừa tính được.


                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isAdded() || getView() == null || generation != viewGeneration) return;
                    // Cập nhật giá trị `isLoading` (is loading) theo trạng thái/kết quả vừa tính được.
                    isLoading = false;
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (progressDialog != null && progressDialog.isShowing()) {
                        // Đóng dialog/loading sau khi tác vụ đã hoàn thành hoặc gặp lỗi.
                        progressDialog.dismiss();
                    }
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    questions = result.getQuestions();
                    if (!questions.isEmpty()) {
                        // Gọi hàm `showQuestion()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                        showQuestion(0);
                    // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
                    } else {
                        // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                        Toast.makeText(getContext(), "Không thể tạo quiz, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        // Gọi hàm `requireActivity()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });

            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception e) {
                // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isAdded() || getView() == null || generation != viewGeneration) return;
                    // Cập nhật giá trị `isLoading` (is loading) theo trạng thái/kết quả vừa tính được.
                    isLoading = false;
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (progressDialog != null && progressDialog.isShowing()) {
                        // Đóng dialog/loading sau khi tác vụ đã hoàn thành hoặc gặp lỗi.
                        progressDialog.dismiss();
                    }
                    // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                    Toast.makeText(getContext(), "Lỗi tạo quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Gọi hàm `requireActivity()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                });
            }
        // Khởi chạy luồng nền vừa tạo.
        }).start();

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        btnNext.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (isLoading) return;

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (isAnswered) {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (currentIndex < questions.size() - 1) {
                    // Tăng `currentIndex` thêm 1 để chuyển/cập nhật sang trạng thái tiếp theo.
                    currentIndex++;
                    // Gọi hàm `showQuestion()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    showQuestion(currentIndex);
                // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
                } else {
                    // Gọi hàm `finishQuiz()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    finishQuiz();
                }
            }
        });
    }

    /** Bind một QuizQuestion lên UI: prompt, 4 options, difficulty và EXP reward. */
    private void showQuestion(int index) {
        // Tính/đọc và lưu giá trị `question` (question) để sử dụng ở các bước phía sau.
        QuizQuestion question = questions.get(index);

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvProgress.setText(getString(R.string.quiz_question_count_format, index + 1, questions.size()));
        // Gọi `setProgress()` để cập nhật thuộc tính/trạng thái của `pbQuiz`.
        pbQuiz.setProgress((int) Math.round(((index + 1) * 100.0) / questions.size()));
        
        // Lấy chuỗi từ resources và lưu vào `difficultyName` để hỗ trợ đa ngôn ngữ.
        String difficultyName = getString(question.getDifficulty().getDisplayNameResId());
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvDifficulty.setText(getString(R.string.quiz_difficulty_format, difficultyName, question.getExpReward()));

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvQuestion.setText(question.getPrompt());

        // Tính/đọc và lưu danh sách các lựa chọn trả lời để sử dụng ở các bước phía sau.
        List<String> options = question.getOptions();
        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int i = 0; i < optionTexts.length; i++) {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (i < options.size()) {
                // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
                optionTexts[i].setText(options.get(i));
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                optionCards[i].setVisibility(View.VISIBLE);

                // Đổi màu nền CardView để phản hồi trực quan trạng thái đúng/sai hoặc lựa chọn.
                optionCards[i].setCardBackgroundColor(
                        getResources().getColor(R.color.bg_secondary, null)
                );
                optionTexts[i].setTextColor(
                        getResources().getColor(R.color.text_primary, null)
                );

                // Tính/đọc và lưu giá trị `optionIndex` (option index) để sử dụng ở các bước phía sau.
                final int optionIndex = i;
                // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
                optionCards[i].setOnClickListener(v -> handleOptionClick(optionIndex));
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                optionCards[i].setVisibility(View.GONE);
            }
        }

        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        cardExplanation.setVisibility(View.GONE);
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExplanation.setText("");

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (index == questions.size() - 1) {
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            btnNext.setText("Xem kết quả");
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            btnNext.setText("Tiếp theo");
        }
        // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
        btnNext.setEnabled(false);

        // Gọi hàm `setOptionsEnabled()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setOptionsEnabled(true);
        // Cập nhật giá trị `isAnswered` (is answered) theo trạng thái/kết quả vừa tính được.
        isAnswered = false;
    }

    /**
     * Chấm đáp án: so sánh A/B/C/D với correctAnswer; đúng thì cộng correctAnswers + EXP.
     */
    private void handleOptionClick(int index) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (isAnswered || isLoading) return;

        // Tính/đọc và lưu giá trị `question` (question) để sử dụng ở các bước phía sau.
        QuizQuestion question = questions.get(currentIndex);
        // Tính/đọc và lưu đáp án người dùng vừa chọn để sử dụng ở các bước phía sau.
        String selectedOption = getOptionLetter(index);
        // Tính/đọc và lưu đáp án đúng để sử dụng ở các bước phía sau.
        String correctAnswer = question.getCorrectAnswer();

        // Cập nhật giá trị `isAnswered` (is answered) theo trạng thái/kết quả vừa tính được.
        isAnswered = true;
        // Gọi hàm `setOptionsEnabled()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setOptionsEnabled(false);

        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int i = 0; i < optionCards.length; i++) {
            // Tính/đọc và lưu giá trị `optionLetter` (option letter) để sử dụng ở các bước phía sau.
            String optionLetter = getOptionLetter(i);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (optionLetter.equals(correctAnswer)) {
                // Đổi màu nền CardView để phản hồi trực quan trạng thái đúng/sai hoặc lựa chọn.
                optionCards[i].setCardBackgroundColor(
                        getResources().getColor(R.color.success, null)
                );
                optionTexts[i].setTextColor(
                        getResources().getColor(R.color.programming_white, null)
                );
            // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
            } else if (i == index && !optionLetter.equals(correctAnswer)) {
                // Đổi màu nền CardView để phản hồi trực quan trạng thái đúng/sai hoặc lựa chọn.
                optionCards[i].setCardBackgroundColor(
                        getResources().getColor(R.color.error, null)
                );
                optionTexts[i].setTextColor(
                        getResources().getColor(R.color.programming_white, null)
                );
            }
        }

        // EXP lúc này mới nằm trong biến RAM totalExpEarned, CHƯA ghi database.
        if (selectedOption.equals(correctAnswer)) {
            // Tăng `correctAnswers` thêm 1 để chuyển/cập nhật sang trạng thái tiếp theo.
            correctAnswers++;
            // Cộng thêm giá trị mới vào `totalExpEarned` và lưu lại tổng mới.
            totalExpEarned += question.getExpReward();
        }

        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        tvExplanation.setText(question.getExplanation());
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        cardExplanation.setVisibility(View.VISIBLE);

        // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
        btnNext.setEnabled(true);
    }

    // Hàm `getOptionLetter()`: Trả về giá trị OptionLetter hiện đang được đối tượng lưu giữ.
    private String getOptionLetter(int index) {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return String.valueOf((char) ('A' + index));
    }

    // Hàm `setOptionsEnabled()`: Cập nhật giá trị OptionsEnabled cho đối tượng hiện tại.
    private void setOptionsEnabled(boolean enabled) {
        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (CardView card : optionCards) {
            // Gọi `setClickable()` để cập nhật thuộc tính/trạng thái của `card`.
            card.setClickable(enabled);
            // Gọi `setFocusable()` để cập nhật thuộc tính/trạng thái của `card`.
            card.setFocusable(enabled);
        }
    }

    /**
     * Kết thúc Quiz: lưu attempt + EXP. Đây mới là lúc Quiz persist xuống SQLite.
     */
    private void finishQuiz() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (isFinishingQuiz || questions.isEmpty()) {
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }
        // Cập nhật giá trị `isFinishingQuiz` (is finishing quiz) theo trạng thái/kết quả vừa tính được.
        isFinishingQuiz = true;
        // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
        btnNext.setEnabled(false);
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        btnNext.setText("Đang lưu kết quả...");

        // Đọc Bundle tham số đã truyền vào Fragment.
        String subject = getArguments() != null ? getArguments().getString(ARG_SUBJECT, "Lập trình") : "Lập trình";
        // Đọc Bundle tham số đã truyền vào Fragment.
        String level = getArguments() != null ? getArguments().getString(ARG_LEVEL, "Trung bình") : "Trung bình";
        // Đọc Bundle tham số đã truyền vào Fragment.
        String topic = getArguments() != null ? getArguments().getString(ARG_TOPIC, "Kiến thức lập trình") : "Kiến thức lập trình";

        // Khởi tạo dữ liệu request chuẩn bị gửi đi phục vụ cho bước xử lý tiếp theo.
        QuizRequest request = new QuizRequest(subject, level, topic, questions.size());
        // Khởi tạo kết quả trả về sau khi xử lý phục vụ cho bước xử lý tiếp theo.
        QuizResult result = new QuizResult(request, questions, correctAnswers, totalExpEarned);

        // Tạo luồng nền để tác vụ nặng/network không chặn UI thread.
        new Thread(() -> {
            // INSERT vào programming_quiz_attempts; nếu thành công helper sẽ update user_progress.
            // Kết quả dialog dùng tổng EXP toàn Programming để level khớp Home/Progress.
            long attemptId = dbHelper.saveQuizResult(userId, subject, level, topic,
                    // Gọi `size()` trên `questions` để thực hiện thao tác tương ứng.
                    questions.size(), correctAnswers, totalExpEarned);
            // Query tổng EXP của người dùng trên toàn bộ subject Programming.
            int totalExp = attemptId == -1 ? 0 : dbHelper.getTotalExpAllSubjects(userId);

            new Handler(Looper.getMainLooper()).post(() -> {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (!isAdded()) {
                    // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                    return;
                }
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (attemptId == -1) {
                    // Cập nhật giá trị `isFinishingQuiz` (is finishing quiz) theo trạng thái/kết quả vừa tính được.
                    isFinishingQuiz = false;
                    // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
                    btnNext.setEnabled(true);
                    // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
                    btnNext.setText("Thử lưu lại");
                    // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                    Toast.makeText(requireContext(),
                            "Không thể lưu kết quả. EXP chưa được cộng.",
                            Toast.LENGTH_LONG).show();
                    // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                    return;
                }
                // Gọi hàm `showResult()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                showResult(result, totalExp);
            });
        // Khởi chạy luồng nền vừa tạo.
        }).start();
    }

    // Hàm `showResult()`: Cập nhật giao diện để hiển thị trạng thái/nội dung tương ứng.
    private void showResult(QuizResult result, int totalExp) {
        // Tính/đọc và lưu mức độ khó để sử dụng ở các bước phía sau.
        int level = ProgrammingLevelCalculator.getLevel(totalExp);
        // Lấy chuỗi từ resources và lưu vào `title` để hỗ trợ đa ngôn ngữ.
        String title = getString(ProgrammingLevelCalculator.getTitleResId(level));
        
        // Lấy chuỗi từ resources và lưu vào `message` để hỗ trợ đa ngôn ngữ.
        String message = getString(R.string.quiz_result_correct, result.getCorrectAnswers(), result.getTotalQuestions()) + "\n" +
                getString(R.string.quiz_result_accuracy, result.getAccuracyPercent()) + "\n" +
                getString(R.string.quiz_result_exp, result.getExpEarned()) + "\n" +
                getString(R.string.quiz_result_total_exp, totalExp) + "\n" +
                // Gọi hàm `getString()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                getString(R.string.quiz_result_level, level, title);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.quiz_result_dialog_title)
                .setMessage(message)
                .setPositiveButton(R.string.quiz_result_back, (dialog, which) -> {
                    // Lấy FragmentManager để thực hiện chuyển/replace Fragment.
                    requireActivity().getSupportFragmentManager().popBackStack();
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (getActivity() instanceof MainActivity
                            && ((MainActivity) getActivity()).getSupportActionBar() != null) {
                        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_quiz);
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override public void onDestroyView() {
        viewGeneration++;
        if (progressDialog != null) { progressDialog.dismiss(); progressDialog = null; }
        super.onDestroyView();
    }

    @Override
    // Hàm `onDestroy()`: Giải phóng tài nguyên khi đối tượng/màn hình bị hủy.
    public void onDestroy() {
        // Gọi implementation `onDestroy()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onDestroy();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (progressDialog != null && progressDialog.isShowing()) {
            // Đóng dialog/loading sau khi tác vụ đã hoàn thành hoặc gặp lỗi.
            progressDialog.dismiss();
        }
    }
}
