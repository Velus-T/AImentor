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
import com.example.historymodule.R;
import com.example.historymodule.adapter.HistoryHistoryAdapter;
import com.example.historymodule.databases.HistoryHistoryItem;
import com.example.historymodule.databases.SqliteDbHelper;
import com.example.historymodule.util.HistoryUserSession;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
 * Role: Fragment for History, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public final class HistoryHistoryFragment extends Fragment {
    private final List<HistoryHistoryItem> conversations = new ArrayList<>();
    private HistoryHistoryAdapter adapter;
    private SqliteDbHelper dbHelper;
    private TextView emptyView;
    private int selectedTabIndex;
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
        View view = inflater.inflate(R.layout.history_fragment_conversations, container, false);
        userId = HistoryUserSession.getUserId(requireContext());
        dbHelper = new SqliteDbHelper(requireContext());
        emptyView = view.findViewById(R.id.tvHistoryEmpty);

        RecyclerView recycler = view.findViewById(R.id.recycler_chat_history);
        adapter = new HistoryHistoryAdapter(conversations, new HistoryHistoryAdapter.Listener() {
            /*
             * Responsibility: Handles the Android callback or event `onClick` and updates the related state.
             * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
             */
            @Override
            public void onClick(HistoryHistoryItem item) {
                Intent intent = new Intent(requireContext(), HistoryChatActivity.class);
                intent.putExtra(HistoryChatActivity.EXTRA_CONVERSATION_ID, item.getReferenceId());
                startActivity(intent);
            }

            /*
             * Responsibility: Handles the Android callback or event `onLongClick` and updates the related state.
             * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
             */
            @Override
            public void onLongClick(HistoryHistoryItem item) {
                confirmDelete(item);
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        TabLayout tabs = view.findViewById(R.id.history_tabs);
        tabs.removeAllTabs();
        tabs.addTab(tabs.newTab().setText(R.string.history_filter_all));
        tabs.addTab(tabs.newTab().setText(R.string.history_filter_today));
        tabs.addTab(tabs.newTab().setText(R.string.history_filter_7days));
        tabs.addTab(tabs.newTab().setText(R.string.history_filter_last_month));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                selectedTabIndex = tab.getPosition();
                loadConversations();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { loadConversations(); }
        });

        loadConversations();
        return view;
    }

    /*
     * Responsibility: Handles the Android callback or event `onResume` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (dbHelper != null && adapter != null) loadConversations();
    }

    /*
     * Responsibility: Performs the `confirm delete` responsibility of `HistoryHistoryFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void confirmDelete(HistoryHistoryItem item) {
        if (item.getReferenceId() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.history_delete_conv_title)
                .setMessage(R.string.history_delete_conv_msg)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dbHelper.deleteConversation(userId, item.getReferenceId());
                    loadConversations();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
     * Responsibility: Reads or loads data for `load conversations` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void loadConversations() {
        long[] range = resolveRange(selectedTabIndex);
        conversations.clear();
        conversations.addAll(dbHelper.loadConversationSummaries(userId, range[0], range[1], 100));
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /*
     * Responsibility: Performs the `resolve range` responsibility of `HistoryHistoryFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private long[] resolveRange(int tabIndex) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        if (tabIndex == 1) {
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            return new long[]{start.getTimeInMillis(), 0L};
        }
        if (tabIndex == 2) {
            start.add(Calendar.DAY_OF_YEAR, -7);
            return new long[]{start.getTimeInMillis(), 0L};
        }
        if (tabIndex == 3) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis());
            start.add(Calendar.MONTH, -1);
            return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
        }
        return new long[]{0L, 0L};
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
