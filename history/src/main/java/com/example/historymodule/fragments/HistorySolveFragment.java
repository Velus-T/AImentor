package com.example.historymodule.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.historymodule.HistoryChatActivity;
import com.example.historymodule.HistoryDashboardActivity;
import com.example.historymodule.model.HistoryLevelCalculator;
import com.example.historymodule.R;
import com.example.historymodule.adapter.HistoryHistoryAdapter;
import com.example.historymodule.databases.HistoryHistoryItem;
import com.example.historymodule.databases.SqliteDbHelper;
import com.example.historymodule.util.HistoryUserSession;

import java.util.ArrayList;
import java.util.List;

/*
 * Role: Fragment for History, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public final class HistorySolveFragment extends Fragment {
    private final List<HistoryHistoryItem> recentList = new ArrayList<>();
    private HistoryHistoryAdapter adapter;
    private SqliteDbHelper dbHelper;
    private TextView emptyView;
    private int userId;

    /*
     * Responsibility: Handles the Android callback or event `onCreateView` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment_solver, container, false);
        userId = HistoryUserSession.getUserId(requireContext());
        dbHelper = new SqliteDbHelper(requireContext());
        emptyView = view.findViewById(R.id.tvRecentEmpty);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_recent_solver);
        adapter = new HistoryHistoryAdapter(recentList, new HistoryHistoryAdapter.Listener() {
            /*
             * Responsibility: Handles the Android callback or event `onClick` and updates the related state.
             * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
             */
            @Override
            public void onClick(HistoryHistoryItem item) {
                openConversation(item.getReferenceId());
            }

            @Override
            public void onLongClick(HistoryHistoryItem item) {
                confirmDelete(item);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        view.findViewById(R.id.card_ai_intro)
                .setOnClickListener(v -> openChat(false, false));
        view.findViewById(R.id.btn_camera_card)
                .setOnClickListener(v -> openChat(true, false));
        view.findViewById(R.id.btn_input_card)
                .setOnClickListener(v -> openChat(false, true));

        view.findViewById(R.id.history_topic_vietnam).setOnClickListener(v -> openTopic(R.string.history_draft_vietnam));
        view.findViewById(R.id.history_topic_world).setOnClickListener(v -> openTopic(R.string.history_draft_world));
        view.findViewById(R.id.history_progress_card).setOnClickListener(v -> selectTab(R.id.nav_quiz));
        view.findViewById(R.id.history_open_notes).setOnClickListener(v -> selectTab(R.id.nav_events));
        view.findViewById(R.id.history_view_all).setOnClickListener(v -> selectTab(R.id.nav_history));
        loadRecentConversations();
        return view;
    }

    /*
     * Responsibility: Handles the Android callback or event `onResume` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (dbHelper != null && adapter != null) loadRecentConversations();
    }

    /*
     * Responsibility: Displays or binds data from `open chat` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void openChat(boolean openCamera, boolean focusInput) {
        Intent intent = new Intent(requireContext(), HistoryChatActivity.class);
        intent.putExtra(HistoryChatActivity.EXTRA_OPEN_CAMERA, openCamera);
        intent.putExtra(HistoryChatActivity.EXTRA_FOCUS_INPUT, focusInput);
        startActivity(intent);
    }

    /*
     * Responsibility: Displays or binds data from `open conversation` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void openConversation(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) return;
        Intent intent = new Intent(requireContext(), HistoryChatActivity.class);
        intent.putExtra(HistoryChatActivity.EXTRA_CONVERSATION_ID, conversationId);
        startActivity(intent);
    }

    /*
     * Responsibility: Performs the `confirm delete` responsibility of `HistorySolveFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void confirmDelete(HistoryHistoryItem item) {
        if (item.getReferenceId() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.history_solver_delete_title)
                .setMessage(R.string.history_solver_delete_msg)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dbHelper.deleteConversation(userId, item.getReferenceId());
                    loadRecentConversations();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
     * Responsibility: Reads or loads data for `load recent conversations` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void selectTab(int id) {
        ((HistoryDashboardActivity) requireActivity()).selectTab(id);
    }

    private void openTopic(int draftRes) {
        startActivity(new Intent(requireContext(), HistoryChatActivity.class)
                .putExtra(HistoryChatActivity.EXTRA_CONVERSATION_ID, java.util.UUID.randomUUID().toString())
                .putExtra(HistoryChatActivity.EXTRA_DRAFT, getString(draftRes))
                .putExtra(HistoryChatActivity.EXTRA_FOCUS_INPUT, true));
    }

    private void loadRecentConversations() {
        int exp = dbHelper.getTotalQuizExp(userId);
        View root = getView();
        if (root != null) {
            ((TextView) root.findViewById(R.id.history_home_progress)).setText(
                    getString(R.string.history_home_progress_format, HistoryLevelCalculator.getLevel(exp), exp));
            ((android.widget.ProgressBar) root.findViewById(R.id.history_home_progress_bar))
                    .setProgress(HistoryLevelCalculator.getProgressPercent(exp));
        }
        recentList.clear();
        recentList.addAll(dbHelper.loadConversationSummaries(userId, 0L, 0L, 5));
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(recentList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /*
     * Responsibility: Handles the Android callback or event `onDestroyView` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onDestroyView() {
        if (dbHelper != null) dbHelper.close();
        dbHelper = null;
        adapter = null;
        emptyView = null;
        super.onDestroyView();
    }
}
