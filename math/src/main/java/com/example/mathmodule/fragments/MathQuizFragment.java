package com.example.mathmodule.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathmodule.QuizPlayActivity;
import com.example.mathmodule.R;
import com.example.mathmodule.adapter.QuizSetAdapter;
import com.example.mathmodule.api.MathGeminiApiClient;
import com.example.mathmodule.databases.QuizQuestion;
import com.example.mathmodule.databases.QuizSet;
import com.example.mathmodule.databases.SqliteDbHelper;
import com.example.mathmodule.util.MathEducationLevel;
import com.example.mathmodule.util.MathLevelCalculator;
import com.example.mathmodule.util.MathQuizDifficulty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
 * Role: Fragment for Math, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public class MathQuizFragment extends Fragment {

    private RecyclerView recyclerView;
    private QuizSetAdapter adapter;
    private List<QuizSet> quizSetList;
    private SqliteDbHelper dbHelper;
    private MathGeminiApiClient aiClient;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
    private TextView txtUserExp;
    private ProgressBar progressUserExp;

    /*
     * Responsibility: Handles the Android callback or event `onCreateView` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_math_quiz, container, false);
        view.findViewById(R.id.btn_math_module_back)
                .setOnClickListener(v -> requireActivity().finish());

        dbHelper = new SqliteDbHelper(requireContext());
        dbHelper.seedQuizDataIfNeeded(requireContext());
        aiClient = new MathGeminiApiClient();

        recyclerView = view.findViewById(R.id.recycler_quiz_sets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        txtUserExp = view.findViewById(R.id.txt_user_exp);
        progressUserExp = view.findViewById(R.id.progress_user_exp);

        setupFilters(view);
        updateUserExpUI();
        view.findViewById(R.id.btn_ai_generate_entry)
                .setOnClickListener(v -> showAiConfigDialog());
        return view;
    }

    /*
     * Responsibility: Handles the Android callback or event `onResume` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateUserExpUI();
    }

    /*
     * Responsibility: Updates or synchronizes `update user exp ui` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateUserExpUI() {
        if (dbHelper == null || txtUserExp == null || progressUserExp == null) return;
        int exp = dbHelper.getUserExp();
        int level = MathLevelCalculator.getLevel(exp);
        int inside = MathLevelCalculator.getInsideLevel(exp);
        int required = MathLevelCalculator.getExpToNextLevel(level);

        String detail = level >= MathLevelCalculator.MAX_LEVEL
                ? getString(R.string.math_quiz_level_max)
                : getString(R.string.math_quiz_level_detail, inside, required, MathLevelCalculator.getRemaining(exp));

        String title = getString(MathLevelCalculator.getTitleResId(level));

        txtUserExp.setText(getString(R.string.math_quiz_level_format, level, title, exp, detail));
        progressUserExp.setMax(level >= MathLevelCalculator.MAX_LEVEL ? 1 : Math.max(1, required));
        progressUserExp.setProgress(level >= MathLevelCalculator.MAX_LEVEL ? 1 : inside);
    }

    /*
     * Responsibility: Reads or loads data for `load quiz sets` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void loadQuizSets(String category) {
        if ("Tất cả".equals(category)) {
            quizSetList = dbHelper.getAllQuizSets();
        } else if ("AI tạo".equals(category)) {
            quizSetList = dbHelper.getQuizSetsByCategory("AI Generated");
        } else {
            quizSetList = dbHelper.getQuizSetsByCategory(category);
        }

        adapter = new QuizSetAdapter(quizSetList, quizSet -> {
            Intent intent = new Intent(requireContext(), QuizPlayActivity.class);
            intent.putExtra("QUIZ_SET_ID", quizSet.getId());
            intent.putExtra("QUIZ_SET_TITLE", quizSet.getTitle());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    /*
     * Responsibility: Configures `setup filters` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupFilters(View view) {
        Button btnAll = view.findViewById(R.id.btn_filter_all);
        Button btnAlgebra = view.findViewById(R.id.btn_filter_algebra);
        Button btnGeometry = view.findViewById(R.id.btn_filter_geometry);
        Button btnAi = view.findViewById(R.id.btn_filter_ai);
        Button[] buttons = {btnAll, btnAlgebra, btnGeometry, btnAi};

        View.OnClickListener listener = selected -> {
            for (Button button : buttons) {
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.math_button_inactive_bg)));
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.math_button_inactive_text));
            }
            Button active = (Button) selected;
            active.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.math_quiz_primary)));
            active.setTextColor(ContextCompat.getColor(requireContext(), R.color.math_active_text));

            if (selected.getId() == R.id.btn_filter_algebra) loadQuizSets(getString(R.string.math_formula_filter_algebra));
            else if (selected.getId() == R.id.btn_filter_geometry) loadQuizSets(getString(R.string.math_formula_filter_geometry));
            else if (selected.getId() == R.id.btn_filter_ai) loadQuizSets(getString(R.string.math_quiz_filter_ai));
            else loadQuizSets(getString(R.string.math_history_filter_all));
        };
        for (Button button : buttons) button.setOnClickListener(listener);
        btnAll.performClick();
    }

    /*
     * Responsibility: Displays or binds data from `show ai config dialog` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showAiConfigDialog() {
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_ai_quiz_config, null, false);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        EditText edtTopic = dialogView.findViewById(R.id.edt_quiz_topic);
        Spinner spinnerCount = dialogView.findViewById(R.id.spinner_question_count);
        Spinner spinnerEducation = dialogView.findViewById(R.id.spinner_education_level);
        Button btnGenerate = dialogView.findViewById(R.id.btn_generate_ai_quiz);

        btnGenerate.setOnClickListener(v -> {
            String topic = edtTopic.getText().toString().trim();
            if (topic.isEmpty()) {
                edtTopic.setError(getString(R.string.math_quiz_config_error_topic));
                edtTopic.requestFocus();
                return;
            }
            int count;
            try {
                count = Integer.parseInt(spinnerCount.getSelectedItem().toString().split(" ")[0]);
            } catch (Exception ignored) {
                count = 5;
            }
            MathEducationLevel educationLevel = MathEducationLevel.fromDisplayName(
                    spinnerEducation.getSelectedItem().toString());
            dialog.dismiss();
            generateQuizWithAI(topic, count, educationLevel);
        });
        dialog.show();
    }

    /*
     * Responsibility: Generates data or content for `generate quiz with ai` based on the input and module rules.
     * Purpose: Keeping content generation separate makes the strategy easier to change without affecting presentation logic.
     */
    private void generateQuizWithAI(String topic, int count,
                                    MathEducationLevel educationLevel) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage(getString(R.string.math_quiz_ai_progress, educationLevel.getDisplayName()));
        progressDialog.setCancelable(false);
        progressDialog.show();

        String prompt = "Bạn là chuyên gia giáo dục toán học. Hãy tạo " + count
                + " câu hỏi trắc nghiệm về chủ đề: " + topic + ". "
                + "Đối tượng: " + educationLevel.getPromptDescription() + ". "
                + "Nội dung, ký hiệu và phương pháp giải phải phù hợp chính xác với cấp học này. "
                + "Mỗi câu phải có difficulty riêng và chỉ nhận một trong ba giá trị EASY, MEDIUM, HARD. "
                + "EASY là kiến thức trực tiếp hoặc một bước; MEDIUM cần áp dụng 2-3 bước; "
                + "HARD cần suy luận sâu hoặc nhiều bước nhưng không vượt quá cấp học đã chọn. "
                + "Với bộ đề từ 5 câu trở lên, hãy tạo hỗn hợp khoảng 30% EASY, 40% MEDIUM và 30% HARD. "
                + "Trả về DUY NHẤT JSON thô, không có markdown. Cấu trúc bắt buộc: "
                + "{\"set_title\":\"Đề " + escapePrompt(topic)
                + " (AI tạo)\",\"education_level\":\""
                + educationLevel.getDisplayName()
                + "\",\"difficulty\":\"Hỗn hợp\",\"questions\":["
                + "{\"difficulty\":\"EASY\",\"question_text\":\"...\","
                + "\"option_a\":\"...\",\"option_b\":\"...\","
                + "\"option_c\":\"...\",\"option_d\":\"...\","
                + "\"correct_answer\":\"A\",\"explanation\":\"...\"}]}. "
                + "Dùng LaTeX bọc bằng $ hoặc $$. correct_answer chỉ được là A, B, C hoặc D. "
                + "Mỗi lời giải phải rõ ràng và kiểm tra lại đáp án trước khi trả về.";

        aiExecutor.execute(() -> {
            try {
                MathGeminiApiClient.Result result = aiClient.generateText(
                        prompt,
                        null,
                        null,
                        0.3,
                        8192
                );

                JSONObject quizJson = new JSONObject(cleanJson(result.text));
                JSONArray questionArray = quizJson.getJSONArray("questions");
                if (questionArray.length() == 0) {
                    throw new JSONException("Danh sách câu hỏi trống");
                }

                List<QuizQuestion> questions = new ArrayList<>();
                for (int i = 0; i < questionArray.length(); i++) {
                    JSONObject q = questionArray.getJSONObject(i);
                    questions.add(new QuizQuestion(
                            0,
                            0,
                            q.getString("question_text"),
                            q.getString("option_a"),
                            q.getString("option_b"),
                            q.getString("option_c"),
                            q.getString("option_d"),
                            q.getString("correct_answer"),
                            q.getString("explanation"),
                            MathQuizDifficulty.fromApiValue(
                                    q.optString("difficulty", "MEDIUM")
                            )
                    ));
                }

                String title = quizJson.optString(
                        "set_title",
                        "Đề " + topic + " (AI tạo)"
                ).trim();
                String setDifficulty = quizJson.optString(
                        "difficulty",
                        summarizeDifficulty(questions)
                ).trim();
                String returnedLevel = quizJson.optString(
                        "education_level",
                        educationLevel.getDisplayName()
                ).trim();
                MathEducationLevel safeLevel =
                        MathEducationLevel.fromDisplayName(returnedLevel);

                long setId = dbHelper.insertFullQuizSet(
                        title,
                        "AI Generated",
                        setDifficulty,
                        safeLevel.getDisplayName(),
                        questions
                );
                if (setId <= 0) {
                    throw new IllegalStateException("Không thể lưu bộ đề");
                }

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    loadQuizSets(getString(R.string.math_quiz_filter_ai));
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.math_quiz_ai_success, questions.size(), safeLevel.getDisplayName()),
                            Toast.LENGTH_LONG
                    ).show();
                });
            } catch (Exception error) {
                String message;
                if (error instanceof JSONException) {
                    message = getString(R.string.math_quiz_error_json, error.getMessage());
                } else {
                    message = MathGeminiApiClient.toUserMessage(
                            error,
                            getString(R.string.math_quiz_error_api)
                    );
                }
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /*
     * Responsibility: Performs the `run on ui thread` responsibility of `MathQuizFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void runOnUiThread(Runnable action) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) action.run();
        });
    }

    /*
     * Responsibility: Performs the `clean json` responsibility of `MathQuizFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String cleanJson(String text) {
        String value = text == null ? "" : text.trim();
        if (value.startsWith("```")) {
            int firstLine = value.indexOf('\n');
            int end = value.lastIndexOf("```");
            if (firstLine >= 0 && end > firstLine) value = value.substring(firstLine + 1, end).trim();
        }
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start >= 0 && end > start) return value.substring(start, end + 1);
        return value;
    }

    /*
     * Responsibility: Performs the `summarize difficulty` responsibility of `MathQuizFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String summarizeDifficulty(List<QuizQuestion> questions) {
        boolean easy = false, medium = false, hard = false;
        for (QuizQuestion q : questions) {
            switch (q.getDifficulty()) {
                case EASY: easy = true; break;
                case HARD: hard = true; break;
                default: medium = true;
            }
        }
        int types = (easy ? 1 : 0) + (medium ? 1 : 0) + (hard ? 1 : 0);
        if (types > 1) return "Hỗn hợp";
        if (hard) return "Khó";
        if (easy) return "Dễ";
        return "Trung bình";
    }

    /*
     * Responsibility: Performs the `escape prompt` responsibility of `MathQuizFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String escapePrompt(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    @Override
    public void onDestroy() {
        aiExecutor.shutdownNow();
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

}
