package com.example.historymodule;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.historymodule.fragments.HistoryFormulaFragment;
import com.example.historymodule.fragments.HistoryHistoryFragment;
import com.example.historymodule.fragments.HistoryQuizFragment;
import com.example.historymodule.fragments.HistorySolveFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/*
 * Role: Activity for History, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public final class HistoryDashboardActivity extends AppCompatActivity {
    private BottomNavigationView navigation;
    private TextView titleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity_dashboard);

        titleView = findViewById(R.id.tvHistoryDashboardTitle);
        navigation = findViewById(R.id.bottom_navigation);
        findViewById(R.id.btnHistoryDashboardBack).setOnClickListener(v -> finish());

        navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_solver) {
                showPage(new HistorySolveFragment(), getString(R.string.history_tab_solver));
            } else if (id == R.id.nav_history) {
                showPage(new HistoryHistoryFragment(), getString(R.string.history_tab_history));
            } else if (id == R.id.nav_events) {
                showPage(new HistoryFormulaFragment(), getString(R.string.history_tab_notes));
            } else if (id == R.id.nav_quiz) {
                showPage(new HistoryQuizFragment(), getString(R.string.history_tab_quiz));
            } else {
                return false;
            }
            return true;
        });

        if (savedInstanceState == null) {
            navigation.setSelectedItemId(R.id.nav_solver);
        } else {
            updateTitle(navigation.getSelectedItemId());
        }
    }

    /*
     * Responsibility: Displays or binds data from `show page` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    public void selectTab(int id) { navigation.setSelectedItemId(id); }

    private void showPage(Fragment fragment, String title) {
        titleView.setText(title);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /*
     * Responsibility: Updates or synchronizes `update title` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateTitle(int selectedId) {
        if (selectedId == R.id.nav_history) titleView.setText(R.string.history_tab_history);
        else if (selectedId == R.id.nav_events) titleView.setText(R.string.history_tab_notes);
        else if (selectedId == R.id.nav_quiz) titleView.setText(R.string.history_tab_quiz);
        else titleView.setText(R.string.history_tab_solver);
    }
}
