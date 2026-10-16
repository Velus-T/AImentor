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
    private ProgressBar pbExpMath, pbExpScience, pbExpHistory, pbExpProg;
    private RecyclerView rvRecent;

    private String username;
    private int userId;
    private int loadVersion;
    private boolean statsExpanded;
    private RecentActivity.Subject recentSubject;

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) statsExpanded = savedInstanceState.getBoolean("stats_expanded");
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
        pbExpMath = view.findViewById(R.id.pb_time_math);
        pbExpScience = view.findViewById(R.id.pb_time_science);
        pbExpHistory = view.findViewById(R.id.pb_time_history);
        pbExpProg = view.findViewById(R.id.pb_time_prog);

        rvRecent = view.findViewById(R.id.rv_recent_activity);
        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.button_start_learning).setOnClickListener(v -> {
            if (recentSubject != null && userId > 0) {
                com.example.asm_final.utils.SubjectNavigator.open(requireContext(), recentSubject, userId);
            } else ((com.example.asm_final.activities.HomeActivity) requireActivity()).navigateTo(R.id.nav_category, false);
        });
        view.findViewById(R.id.button_stats_toggle).setOnClickListener(v -> {
            statsExpanded = !statsExpanded;
            updateStatsVisibility(view);
        });
        updateStatsVisibility(view);
        return view;
    }

    private void updateStatsVisibility(View view) {
        view.findViewById(R.id.layout_detailed_stats).setVisibility(statsExpanded ? View.VISIBLE : View.GONE);
        ((TextView) view.findViewById(R.id.button_stats_toggle)).setText(statsExpanded ? R.string.ux_hide_stats : R.string.ux_show_stats);
    }

    @Override public void onSaveInstanceState(Bundle out) {
        out.putBoolean("stats_expanded", statsExpanded);
        super.onSaveInstanceState(out);
    }

    /*
     * Responsibility: Configures `setup data` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    @Override public void onResume() { super.onResume(); setupData(); }

    private void setupData() {
        if (username == null || username.isEmpty()) {
            txtUsername.setText(R.string.home_default_user);
            return;
        }

        txtUsername.setText(username);
        final int version = ++loadVersion;
        final View owner = getView();
        final android.content.Context context = requireContext().getApplicationContext();
        com.example.asm_final.AppServices services = ((com.example.asm_final.AiMentorApplication) context).services();
        services.io().execute(() -> {
            com.example.asm_final.dashboard.DashboardSnapshot loaded;
            int resolvedId = 0;
            try {
                resolvedId = services.accountLookup().userId(username);
                loaded = services.dashboard().load(resolvedId);
            } catch (RuntimeException error) {
                loaded = new com.example.asm_final.dashboard.DashboardSnapshot(
                    new java.util.EnumMap<>(RecentActivity.Subject.class), java.util.Collections.emptyList(),
                    java.util.EnumSet.allOf(RecentActivity.Subject.class));
            }
            final int loadedUserId = resolvedId;
            final com.example.asm_final.dashboard.DashboardSnapshot snapshot = loaded;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (isAdded() && getView() == owner && loadVersion == version) {
                    userId = loadedUserId;
                    render(snapshot);
                }
            });
        });
    }

    private void render(com.example.asm_final.dashboard.DashboardSnapshot snapshot) {
        int mathExp = snapshot.progress(RecentActivity.Subject.MATH).experience;
        int scienceExp = snapshot.progress(RecentActivity.Subject.SCIENCE).experience;
        int historyExp = snapshot.progress(RecentActivity.Subject.HISTORY).experience;
        int programmingExp = snapshot.progress(RecentActivity.Subject.PROGRAMMING).experience;
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

        int maximum = Math.max(1, Math.max(mathExp, Math.max(historyExp, Math.max(programmingExp, scienceExp))));
        pbExpMath.setProgress((int)(mathExp * 100L / maximum));
        pbExpScience.setProgress((int)(scienceExp * 100L / maximum));
        pbExpHistory.setProgress((int)(historyExp * 100L / maximum));
        pbExpProg.setProgress((int)(programmingExp * 100L / maximum));
        rvRecent.setAdapter(new RecentActivityAdapter(snapshot.activities));
        View view = getView();
        view.findViewById(R.id.text_recent_empty).setVisibility(snapshot.activities.isEmpty() && snapshot.unavailable.isEmpty() ? View.VISIBLE : View.GONE);
        ((TextView)view.findViewById(R.id.tv_chart_summary)).setText(totalExp == 0 ? R.string.ux_no_exp : R.string.home_based_on_exp);
        RecentActivity latest = null;
        for (RecentActivity activity : snapshot.activities) {
            if (activity.getTimestamp() > 0 && (latest == null || activity.getTimestamp() > latest.getTimestamp())) latest = activity;
        }
        recentSubject = latest == null ? null : latest.getSubject();
        TextView start = view.findViewById(R.id.button_start_learning);
        TextView hint = view.findViewById(R.id.text_learning_hint);
        if (recentSubject != null && userId > 0) {
            int label;
            switch (recentSubject) {
                case MATH: label = R.string.cat_math; break;
                case SCIENCE: label = R.string.cat_science; break;
                case HISTORY: label = R.string.cat_history; break;
                default: label = R.string.cat_it;
            }
            start.setText(getString(R.string.ux_reopen_subject, getString(label)));
            hint.setText(getString(R.string.ux_last_activity, com.example.mentor.ui.TimeLabels.dateTime(requireContext(), Long.toString(latest.getTimestamp()))));
        } else {
            start.setText(R.string.ux_choose_subject);
            hint.setText(R.string.ux_start_hint);
        }
        TextView status = getView().findViewById(R.id.tv_dashboard_status);
        status.setVisibility(snapshot.unavailable.isEmpty() ? View.GONE : View.VISIBLE);
        status.setOnClickListener(v -> setupData());

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

}
