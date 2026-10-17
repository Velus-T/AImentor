package com.example.asm_final.activities.fragments;

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
import com.example.asm_final.utils.ExpLevelCalculator;
import com.example.asm_final.utils.RecentActivity;
import com.example.asm_final.utils.RecentActivityAdapter;
import com.example.asm_final.utils.SimplePieChartView;
import java.util.ArrayList;
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
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    view.findViewById(R.id.button_start_learning)
        .setOnClickListener(
            v -> {
              if (recentSubject != null && userId > 0) {
                com.example.asm_final.utils.SubjectNavigator.open(
                    requireContext(), recentSubject, userId);
              } else
                ((com.example.asm_final.activities.HomeActivity) requireActivity())
                    .navigateTo(R.id.nav_category, false);
            });
    view.findViewById(R.id.button_stats_toggle)
        .setOnClickListener(
            v -> {
              statsExpanded = !statsExpanded;
              updateStatsVisibility(view);
            });
    updateStatsVisibility(view);
    return view;
  }

  private void updateStatsVisibility(View view) {
    view.findViewById(R.id.layout_detailed_stats)
        .setVisibility(statsExpanded ? View.VISIBLE : View.GONE);
    ((TextView) view.findViewById(R.id.button_stats_toggle))
        .setText(statsExpanded ? R.string.ux_hide_stats : R.string.ux_show_stats);
  }

  @Override
  public void onSaveInstanceState(Bundle out) {
    out.putBoolean("stats_expanded", statsExpanded);
    super.onSaveInstanceState(out);
  }

  /*
   * Responsibility: Configures `setup data` with the required Views, listeners, or dependencies.
   * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
   */
  @Override
  public void onResume() {
    super.onResume();
    setupData();
    timeContext = requireContext().getApplicationContext();
    android.content.IntentFilter filter = new android.content.IntentFilter();
    filter.addAction(android.content.Intent.ACTION_DATE_CHANGED);
    filter.addAction(android.content.Intent.ACTION_TIME_CHANGED);
    filter.addAction(android.content.Intent.ACTION_TIMEZONE_CHANGED);
    androidx.core.content.ContextCompat.registerReceiver(
        timeContext,
        timeChanges,
        filter,
        androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
  }

  private android.content.Context timeContext;
  private final android.content.BroadcastReceiver timeChanges =
      new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, android.content.Intent intent) {
          if (isAdded() && getView() != null) setupData();
        }
      };

  @Override
  public void onPause() {
    if (timeContext != null) {
      timeContext.unregisterReceiver(timeChanges);
      timeContext = null;
    }
    super.onPause();
  }

  private void setupData() {
    if (username == null || username.isEmpty()) {
      txtUsername.setText(R.string.home_default_user);
      return;
    }

    txtUsername.setText(username);
    final int version = ++loadVersion;
    final View owner = getView();
    final android.content.Context context = requireContext().getApplicationContext();
    com.example.asm_final.AppServices services =
        ((com.example.asm_final.AiMentorApplication) context).services();
    services
        .io()
        .execute(
            () -> {
              com.example.asm_final.dashboard.DashboardSnapshot loaded;
              int resolvedId = 0;
              try {
                resolvedId = services.accountLookup().userId(username);
                loaded = services.dashboard().load(resolvedId);
              } catch (RuntimeException error) {
                loaded =
                    new com.example.asm_final.dashboard.DashboardSnapshot(
                        new java.util.EnumMap<>(RecentActivity.Subject.class),
                        java.util.Collections.emptyList(),
                        java.util.EnumSet.allOf(RecentActivity.Subject.class));
              }
              com.example.mentor.data.StreakSnapshot streak = null;
              try {
                streak = services.streaks().load(resolvedId);
              } catch (RuntimeException ignored) {
              }
              final com.example.mentor.data.StreakSnapshot loadedStreak = streak;
              com.example.asm_final.dashboard.WeeklyQuizProgress weekly = null;
              try {
                weekly = services.weeklyQuizzes().load(resolvedId);
              } catch (RuntimeException ignored) {
              }
              final com.example.asm_final.dashboard.WeeklyQuizProgress loadedWeekly = weekly;
              final int loadedUserId = resolvedId;
              final com.example.asm_final.dashboard.DashboardSnapshot snapshot = loaded;
              new android.os.Handler(android.os.Looper.getMainLooper())
                  .post(
                      () -> {
                        if (isAdded() && getView() == owner && loadVersion == version) {
                          userId = loadedUserId;
                          render(snapshot);
                          renderStreak(loadedStreak);
                          renderWeekly(loadedWeekly);
                        }
                      });
            });
  }

  private void renderWeekly(com.example.asm_final.dashboard.WeeklyQuizProgress weekly) {
    TextView xp = getView().findViewById(R.id.weekly_quiz_xp);
    TextView period = getView().findViewById(R.id.weekly_quiz_period);
    if (weekly == null) {
      xp.setText(R.string.weekly_quiz_error);
      period.setText("");
      xp.setOnClickListener(v -> setupData());
      return;
    }
    xp.setOnClickListener(null);
    xp.setText(getString(R.string.weekly_quiz_xp, weekly.experience));
    java.text.DateFormat format =
        java.text.DateFormat.getDateInstance(
            java.text.DateFormat.MEDIUM, getResources().getConfiguration().getLocales().get(0));
    period.setText(
        getString(
            R.string.weekly_quiz_period,
            format.format(new java.util.Date(weekly.startsAt)),
            format.format(new java.util.Date(weekly.endsAt - 1)),
            weekly.completedQuizzes));
  }

  private void renderStreak(com.example.mentor.data.StreakSnapshot streak) {
    View root = getView();
    TextView current = root.findViewById(R.id.streak_current);
    TextView best = root.findViewById(R.id.streak_best);
    TextView status = root.findViewById(R.id.streak_status);
    android.widget.LinearLayout week = root.findViewById(R.id.streak_week_days);
    week.removeAllViews();
    if (streak == null) {
      current.setText(R.string.streak_error);
      best.setText("");
      status.setText("");
      current.setOnClickListener(v -> setupData());
      return;
    }
    current.setOnClickListener(null);
    current.setText(
        getResources().getQuantityString(R.plurals.streak_days, streak.current, streak.current));
    best.setText(getString(R.string.streak_best, streak.best));
    status.setText(
        streak.completedToday
            ? R.string.streak_completed
            : streak.current > 0
                ? R.string.streak_pending
                : streak.best > 0 ? R.string.streak_restart : R.string.streak_start);
    float density = getResources().getDisplayMetrics().density;
    java.util.Locale locale = getResources().getConfiguration().getLocales().get(0);
    java.text.DateFormat dateLabel =
        java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL, locale);
    dateLabel.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    java.util.Calendar calendar =
        java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
    for (int offset = 6; offset >= 0; offset--) {
      long day = streak.today - offset;
      boolean learned = streak.days.contains(day);
      calendar.setTimeInMillis(day * 86400000L);
      TextView cell = new TextView(requireContext());
      cell.setText(
          String.format(
              locale,
              "%d\n%s",
              calendar.get(java.util.Calendar.DAY_OF_MONTH),
              getString(learned ? R.string.streak_done_glyph : R.string.streak_missing_glyph)));
      cell.setTextSize(13);
      cell.setGravity(android.view.Gravity.CENTER);
      cell.setPadding(0, (int) (6 * density), 0, (int) (6 * density));
      cell.setMinHeight((int) (52 * density));
      cell.setTextColor(
          ContextCompat.getColor(
              requireContext(), learned ? R.color.primaryColor : R.color.textColorSecondary));
      if (offset == 0) cell.setTypeface(cell.getTypeface(), android.graphics.Typeface.BOLD);
      android.graphics.drawable.GradientDrawable background =
          new android.graphics.drawable.GradientDrawable();
      background.setCornerRadius(10 * density);
      background.setColor(
          ContextCompat.getColor(
              requireContext(),
              learned ? R.color.primaryContainerColor : R.color.surfaceVariantColor));
      if (offset == 0)
        background.setStroke(
            (int) Math.max(1, density),
            ContextCompat.getColor(requireContext(), R.color.primaryColor));
      cell.setBackground(background);
      cell.setContentDescription(
          getString(
              learned ? R.string.streak_day_done : R.string.streak_day_missing,
              dateLabel.format(calendar.getTime())));
      android.widget.LinearLayout.LayoutParams params =
          new android.widget.LinearLayout.LayoutParams(0, -2, 1);
      if (offset > 0) params.setMarginEnd((int) (4 * density));
      week.addView(cell, params);
    }
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
    tvExpDetail.setText(
        getString(R.string.home_exp_remaining, ExpLevelCalculator.getExpToNextLevel(totalExp)));

    // --- UPDATE MINI CARDS ---
    updateMiniStat(miniMath, getString(R.string.cat_math), R.drawable.ic_cat_math, mathExp);
    updateMiniStat(
        miniScience, getString(R.string.cat_science), R.drawable.ic_cat_science, scienceExp);
    updateMiniStat(
        miniHistory, getString(R.string.cat_history), R.drawable.ic_cat_history, historyExp);
    updateMiniStat(
        miniProg, getString(R.string.cat_it), R.drawable.ic_cat_programming, programmingExp);

    // --- UPDATE PIE CHART ---
    List<SimplePieChartView.Slice> slices = new ArrayList<>();
    slices.add(
        new SimplePieChartView.Slice(
            mathExp, ContextCompat.getColor(requireContext(), R.color.cat_math_text)));
    slices.add(
        new SimplePieChartView.Slice(
            scienceExp, ContextCompat.getColor(requireContext(), R.color.cat_science_text)));
    slices.add(
        new SimplePieChartView.Slice(
            historyExp, ContextCompat.getColor(requireContext(), R.color.cat_history_text)));
    slices.add(
        new SimplePieChartView.Slice(
            programmingExp, ContextCompat.getColor(requireContext(), R.color.cat_it_text)));
    pieChart.setData(slices);

    int maximum =
        Math.max(1, Math.max(mathExp, Math.max(historyExp, Math.max(programmingExp, scienceExp))));
    pbExpMath.setProgress((int) (mathExp * 100L / maximum));
    pbExpScience.setProgress((int) (scienceExp * 100L / maximum));
    pbExpHistory.setProgress((int) (historyExp * 100L / maximum));
    pbExpProg.setProgress((int) (programmingExp * 100L / maximum));
    rvRecent.setAdapter(new RecentActivityAdapter(snapshot.activities));
    View view = getView();
    view.findViewById(R.id.text_recent_empty)
        .setVisibility(
            snapshot.activities.isEmpty() && snapshot.unavailable.isEmpty()
                ? View.VISIBLE
                : View.GONE);
    ((TextView) view.findViewById(R.id.tv_chart_summary))
        .setText(totalExp == 0 ? R.string.ux_no_exp : R.string.home_based_on_exp);
    RecentActivity latest = null;
    for (RecentActivity activity : snapshot.activities) {
      if (activity.getTimestamp() > 0
          && (latest == null || activity.getTimestamp() > latest.getTimestamp())) latest = activity;
    }
    recentSubject = latest == null ? null : latest.getSubject();
    TextView start = view.findViewById(R.id.button_start_learning);
    TextView hint = view.findViewById(R.id.text_learning_hint);
    if (recentSubject != null && userId > 0) {
      int label;
      switch (recentSubject) {
        case MATH:
          label = R.string.cat_math;
          break;
        case SCIENCE:
          label = R.string.cat_science;
          break;
        case HISTORY:
          label = R.string.cat_history;
          break;
        default:
          label = R.string.cat_it;
      }
      start.setText(getString(R.string.ux_reopen_subject, getString(label)));
      hint.setText(
          getString(
              R.string.ux_last_activity,
              com.example.mentor.ui.TimeLabels.dateTime(
                  requireContext(), Long.toString(latest.getTimestamp()))));
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
    ((ImageView) root.findViewById(R.id.iv_mini_icon)).setImageResource(icon);
    ((TextView) root.findViewById(R.id.tv_mini_name)).setText(name);
    ((TextView) root.findViewById(R.id.tv_mini_exp)).setText(getString(R.string.exp_unit, exp));
    ((ProgressBar) root.findViewById(R.id.pb_mini_stat)).setProgress((exp % 200) * 100 / 200);
  }
}
