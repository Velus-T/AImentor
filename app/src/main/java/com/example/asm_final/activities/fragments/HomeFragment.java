package com.example.asm_final.activities.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.asm_final.R;
import com.example.asm_final.activities.database.DatabaseHelper;
import com.example.asm_final.utils.ExpLevelCalculator;
import com.example.asm_final.utils.RecentActivity;
import com.example.asm_final.utils.RecentActivityAdapter;
import com.example.asm_final.utils.SimplePieChartView;
import com.example.mathmodule.util.MathUserSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Role: Fragment for App Shell, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public class HomeFragment extends Fragment {

    private TextView txtUsername, tvTotalLevel, tvLevelTitle, tvExpDetail;
    private ProgressBar pbTotal;
    
    // Subject mini stats
    private View miniMath, miniScience, miniHistory, miniProg;
    
    // Advanced stats
    private SimplePieChartView pieChart;
    private ProgressBar pbTimeMath, pbTimeScience, pbTimeHistory, pbTimeProg;
    private RecyclerView rvRecent;
    
    private String username;
    private int userId;

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString("USERNAME");
        }
    }

    /*
     * Responsibility: Handles the Android callback or event `onCreateView` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        txtUsername = view.findViewById(R.id.txt_home_content);
        tvTotalLevel = view.findViewById(R.id.tv_total_level_value);
        tvLevelTitle = view.findViewById(R.id.tv_level_title);
        tvExpDetail = view.findViewById(R.id.tv_exp_detail);
        pbTotal = view.findViewById(R.id.pb_total_exp);

        miniMath = view.findViewById(R.id.mini_math);
        miniScience = view.findViewById(R.id.mini_science);
        miniHistory = view.findViewById(R.id.mini_history);
        miniProg = view.findViewById(R.id.mini_prog);

        pieChart = view.findViewById(R.id.pie_chart_exp);
        pbTimeMath = view.findViewById(R.id.pb_time_math);
        pbTimeScience = view.findViewById(R.id.pb_time_science);
        pbTimeHistory = view.findViewById(R.id.pb_time_history);
        pbTimeProg = view.findViewById(R.id.pb_time_prog);
        
        rvRecent = view.findViewById(R.id.rv_recent_activity);
        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupData();
        return view;
    }

    /*
     * Responsibility: Configures `setup data` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupData() {
        if (username == null || username.isEmpty()) {
            txtUsername.setText(R.string.home_default_user);
            return;
        }

        txtUsername.setText(username);
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        userId = dbHelper.getUserId(username);
        MathUserSession.setUserId(requireContext(), userId);

        // --- FETCH EXP ---
        int mathExp = 0;
        try (com.example.mathmodule.databases.SqliteDbHelper db = new com.example.mathmodule.databases.SqliteDbHelper(requireContext())) {
            mathExp = db.getUserExp();
        } catch (Exception ignored) {}

        int historyExp = 0;
        try (com.example.historymodule.databases.SqliteDbHelper db = new com.example.historymodule.databases.SqliteDbHelper(requireContext())) {
            historyExp = db.getTotalQuizExp(userId);
        } catch (Exception ignored) {}

        int programmingExp = 0;
        try (com.example.modulelaptrinh.database.ProgrammingDbHelper db = new com.example.modulelaptrinh.database.ProgrammingDbHelper(requireContext())) {
            programmingExp = db.getTotalExpAllSubjects(userId);
        } catch (Exception ignored) {}

        int scienceExp = 0;
        try (com.brightpath.science.data.local.ScienceDbHelper db = new com.brightpath.science.data.local.ScienceDbHelper(requireContext())) {
            scienceExp = db.getTotalExp(userId);
        } catch (Exception ignored) {}

        int totalExp = mathExp + historyExp + programmingExp + scienceExp;
        
        // --- UPDATE UI TOTAL ---
        int level = ExpLevelCalculator.getLevel(totalExp);
        tvTotalLevel.setText(getString(R.string.home_level_prefix) + level);
        tvLevelTitle.setText(ExpLevelCalculator.getLevelTitleResId(level));
        pbTotal.setProgress(ExpLevelCalculator.getProgressPercentage(totalExp));
        tvExpDetail.setText(getString(R.string.home_exp_remaining, ExpLevelCalculator.getExpToNextLevel(totalExp)));

        // --- UPDATE MINI CARDS ---
        updateMiniStat(miniMath, getString(R.string.cat_math), R.drawable.ic_cat_math, mathExp);
        updateMiniStat(miniScience, getString(R.string.cat_science), R.drawable.ic_cat_science, scienceExp);
        updateMiniStat(miniHistory, getString(R.string.cat_history), R.drawable.ic_cat_history, historyExp);
        updateMiniStat(miniProg, getString(R.string.cat_it), R.drawable.ic_cat_programming, programmingExp);

        // --- UPDATE PIE CHART ---
        List<SimplePieChartView.Slice> slices = new ArrayList<>();
        slices.add(new SimplePieChartView.Slice(mathExp, ContextCompat.getColor(requireContext(), R.color.cat_math_text)));
        slices.add(new SimplePieChartView.Slice(scienceExp, ContextCompat.getColor(requireContext(), R.color.cat_science_text)));
        slices.add(new SimplePieChartView.Slice(historyExp, ContextCompat.getColor(requireContext(), R.color.cat_history_text)));
        slices.add(new SimplePieChartView.Slice(programmingExp, ContextCompat.getColor(requireContext(), R.color.cat_it_text)));
        pieChart.setData(slices);

        // --- UPDATE LEARNING TIME (ESTIMATED) ---
        int scienceSeconds = 0;
        try (com.brightpath.science.data.local.ScienceDbHelper db = new com.brightpath.science.data.local.ScienceDbHelper(requireContext())) {
             SQLiteDatabase sdb = db.getReadableDatabase();
             try (Cursor c = sdb.rawQuery("SELECT SUM(review_seconds) FROM science_history WHERE user_id=?", new String[]{String.valueOf(userId)})) {
                 if (c.moveToFirst()) scienceSeconds = c.getInt(0);
             }
        } catch (Exception ignored) {}
        
        int mathSec = mathExp * 10;
        int histSec = historyExp * 10;
        int progSec = programmingExp * 10;
        int scienceTotalSec = scienceSeconds + (scienceExp * 5);

        int maxSec = Math.max(1, Math.max(mathSec, Math.max(histSec, Math.max(progSec, scienceTotalSec))));
        pbTimeMath.setProgress(mathSec * 100 / maxSec);
        pbTimeScience.setProgress(scienceTotalSec * 100 / maxSec);
        pbTimeHistory.setProgress(histSec * 100 / maxSec);
        pbTimeProg.setProgress(progSec * 100 / maxSec);

        // --- FETCH RECENT ACTIVITY ---
        List<RecentActivity> activities = new ArrayList<>();
        fetchMathActivities(activities);
        fetchScienceActivities(activities);
        fetchHistoryActivities(activities);
        fetchProgrammingActivities(activities);
        
        activities.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        if (activities.size() > 10) activities = activities.subList(0, 10);
        
        rvRecent.setAdapter(new RecentActivityAdapter(activities));
    }

    /*
     * Responsibility: Updates or synchronizes `update mini stat` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateMiniStat(View root, String name, int icon, int exp) {
        ((ImageView)root.findViewById(R.id.iv_mini_icon)).setImageResource(icon);
        ((TextView)root.findViewById(R.id.tv_mini_name)).setText(name);
        ((TextView)root.findViewById(R.id.tv_mini_exp)).setText(getString(R.string.exp_unit, exp));
        ((ProgressBar)root.findViewById(R.id.pb_mini_stat)).setProgress((exp % 200) * 100 / 200);
    }

    /*
     * Responsibility: Reads or loads data for `fetch math activities` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void fetchMathActivities(List<RecentActivity> list) {
        try (com.example.mathmodule.databases.SqliteDbHelper db = new com.example.mathmodule.databases.SqliteDbHelper(requireContext())) {
            List<com.example.mathmodule.databases.MathHistoryItem> history = db.getAllHistory();
            for (int i = 0; i < Math.min(3, history.size()); i++) {
                com.example.mathmodule.databases.MathHistoryItem item = history.get(i);
                list.add(new RecentActivity(RecentActivity.Subject.MATH, RecentActivity.Type.CHAT, 
                    getString(R.string.activity_solve_math), item.getQuestion(), System.currentTimeMillis() - (i * 3600000L)));
            }
        } catch (Exception ignored) {}
    }

    /*
     * Responsibility: Reads or loads data for `fetch science activities` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void fetchScienceActivities(List<RecentActivity> list) {
        try (com.brightpath.science.data.local.ScienceDbHelper db = new com.brightpath.science.data.local.ScienceDbHelper(requireContext())) {
            SQLiteDatabase sdb = db.getReadableDatabase();
            try (Cursor c = sdb.query("science_history", new String[]{"question", "created_at"}, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "created_at DESC", "3")) {
                while (c.moveToNext()) {
                    list.add(new RecentActivity(RecentActivity.Subject.SCIENCE, RecentActivity.Type.CHAT, getString(R.string.activity_ask_science), c.getString(0), c.getLong(1)));
                }
            }
            try (Cursor c = sdb.query("science_quiz_attempts", new String[]{"topic", "created_at"}, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "created_at DESC", "2")) {
                while (c.moveToNext()) {
                    list.add(new RecentActivity(RecentActivity.Subject.SCIENCE, RecentActivity.Type.QUIZ, getString(R.string.activity_quiz_science), c.getString(0), c.getLong(1)));
                }
            }
        } catch (Exception ignored) {}
    }

    /*
     * Responsibility: Reads or loads data for `fetch history activities` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void fetchHistoryActivities(List<RecentActivity> list) {
        try (com.example.historymodule.databases.SqliteDbHelper db = new com.example.historymodule.databases.SqliteDbHelper(requireContext())) {
            List<com.example.historymodule.databases.HistoryHistoryItem> quizes = db.loadQuizAttempts(userId, 3);
            for (com.example.historymodule.databases.HistoryHistoryItem q : quizes) {
                list.add(new RecentActivity(RecentActivity.Subject.HISTORY, RecentActivity.Type.QUIZ, getString(R.string.activity_quiz_history), q.getTitle(), Long.parseLong(q.getTimestamp())));
            }
            List<com.example.historymodule.databases.HistoryHistoryItem> chats = db.loadConversationSummaries(userId, 0, 0, 3);
            for (com.example.historymodule.databases.HistoryHistoryItem c : chats) {
                list.add(new RecentActivity(RecentActivity.Subject.HISTORY, RecentActivity.Type.CHAT, getString(R.string.activity_chat_history), c.getTitle(), Long.parseLong(c.getTimestamp())));
            }
        } catch (Exception ignored) {}
    }

    /*
     * Responsibility: Reads or loads data for `fetch programming activities` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void fetchProgrammingActivities(List<RecentActivity> list) {
        try (com.example.modulelaptrinh.database.ProgrammingDbHelper db = new com.example.modulelaptrinh.database.ProgrammingDbHelper(requireContext())) {
            List<com.example.modulelaptrinh.database.SessionItem> sessions = db.getAllSessions(userId);
            for (int i = 0; i < Math.min(3, sessions.size()); i++) {
                com.example.modulelaptrinh.database.SessionItem s = sessions.get(i);
                list.add(new RecentActivity(RecentActivity.Subject.PROGRAMMING, RecentActivity.Type.CHAT, getString(R.string.activity_chat_prog), s.getName(), System.currentTimeMillis() - (i * 1800000L)));
            }
        } catch (Exception ignored) {}
    }
}
