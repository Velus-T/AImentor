package com.example.historymodule.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.historymodule.R;
import com.example.historymodule.adapter.HistoryNoteAdapter;
import com.example.historymodule.databases.HistoryNoteItem;
import com.example.historymodule.databases.SqliteDbHelper;
import com.example.historymodule.util.HistoryUserSession;

import java.util.ArrayList;
import java.util.List;

/** Notes page. The old event-category UI has been removed. */
/*
 * Role: Fragment for History, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public final class HistoryFormulaFragment extends Fragment {
    private final List<HistoryNoteItem> notes = new ArrayList<>();
    private HistoryNoteAdapter adapter;
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
        View view = inflater.inflate(R.layout.history_fragment_notes, container, false);
        userId = HistoryUserSession.getUserId(requireContext());
        dbHelper = new SqliteDbHelper(requireContext());
        emptyView = view.findViewById(R.id.tvNotesEmpty);

        RecyclerView recycler = view.findViewById(R.id.recycler_events);
        adapter = new HistoryNoteAdapter(notes, new HistoryNoteAdapter.Listener() {
            @Override public void onEdit(HistoryNoteItem item) { showNoteDialog(item); }
            @Override public void onDelete(HistoryNoteItem item) { confirmDelete(item); }
            @Override public void onTogglePin(HistoryNoteItem item) {
                dbHelper.setNotePinned(userId, item.getId(), !item.isPinned());
                loadNotes();
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        view.findViewById(R.id.fab_add_event).setOnClickListener(v -> showNoteDialog(null));
        loadNotes();
        return view;
    }

    /*
     * Responsibility: Handles the Android callback or event `onResume` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (dbHelper != null && adapter != null) loadNotes();
    }

    /*
     * Responsibility: Displays or binds data from `show note dialog` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showNoteDialog(@Nullable HistoryNoteItem existing) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.history_dialog_note, null, false);
        EditText titleInput = dialogView.findViewById(R.id.note_title);
        EditText contentInput = dialogView.findViewById(R.id.note_content);
        if (existing != null) {
            titleInput.setText(existing.getTitle());
            contentInput.setText(existing.getContent());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? R.string.history_notes_add_title : R.string.history_notes_edit_title)
                .setView(dialogView)
                .setPositiveButton(R.string.history_notes_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String title = titleInput.getText().toString().trim();
                    String content = contentInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        titleInput.setError(getString(R.string.history_notes_error_title));
                        return;
                    }
                    if (content.isEmpty()) {
                        contentInput.setError(getString(R.string.history_notes_error_content));
                        return;
                    }
                    long changed = existing == null
                            ? dbHelper.insertNote(userId, title, content)
                            : dbHelper.updateNote(userId, existing.getId(), title, content);
                    if (changed <= 0) {
                        Toast.makeText(requireContext(), R.string.history_notes_error_save, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dialog.dismiss();
                    loadNotes();
                }));
        dialog.show();
    }

    /*
     * Responsibility: Performs the `confirm delete` responsibility of `HistoryFormulaFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void confirmDelete(HistoryNoteItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.history_notes_delete_title)
                .setMessage(getString(R.string.history_notes_delete_msg, item.getTitle()))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dbHelper.deleteNote(userId, item.getId());
                    loadNotes();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
     * Responsibility: Reads or loads data for `load notes` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void loadNotes() {
        notes.clear();
        notes.addAll(dbHelper.loadNotes(userId));
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
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
