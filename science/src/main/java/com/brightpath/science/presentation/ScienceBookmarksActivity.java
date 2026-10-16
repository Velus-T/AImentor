package com.brightpath.science.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.ScienceBookmark;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.ScienceBookmarkRepository;
import com.brightpath.science.presentation.adapter.ScienceBookmarkAdapter;
import com.brightpath.science.util.ChemicalFormulaFormatter;
import com.brightpath.science.util.ScienceUserSession;
import com.brightpath.science.util.ScienceUiFactory;

import java.util.List;

public final class ScienceBookmarksActivity extends AppCompatActivity {
    private ScienceBookmarkRepository repository;
    private ScienceBookmarkAdapter adapter;
    private Spinner subjectSpinner;
    private EditText searchInput;
    private TextView emptyView;
    private int userId;
    private final ChemicalFormulaFormatter formulaFormatter = new ChemicalFormulaFormatter();

        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_bookmarks);
        repository = ScienceModule.get(this).bookmarkRepository();
        userId = new ScienceUserSession(this).getUserId();
        subjectSpinner = findViewById(R.id.spBookmarkSubject);
        searchInput = findViewById(R.id.etBookmarkSearch);
        emptyView = findViewById(R.id.tvBookmarksEmpty);

        subjectSpinner.setAdapter(ScienceUiFactory.spinnerAdapter(this,
                new String[]{getString(R.string.science_tool_all_subjects), getString(R.string.science_tool_physics), getString(R.string.science_tool_chemistry), getString(R.string.science_tool_biology)}));

        RecyclerView recyclerView = findViewById(R.id.rvScienceBookmarks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScienceBookmarkAdapter(this::showBookmarkDetail);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBookmarksBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnBookmarkSearch).setOnClickListener(view -> loadBookmarks());
        loadBookmarks();
    }

        /*
 * Responsibility: Retrieves the data/state required by `loadBookmarks` from the appropriate Android screen and user-interaction controller.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
private void loadBookmarks() {
        try {
            List<ScienceBookmark> items = repository.search(
                    userId,
                    selectedSubject(subjectSpinner.getSelectedItemPosition()),
                    searchInput.getText().toString()
            );
            adapter.submitList(items);
            emptyView.setText(getString(R.string.science_tool_no_bookmarks_found));
            emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception error) {
            adapter.submitList(java.util.Collections.emptyList());
            emptyView.setText(getString(R.string.science_tool_bookmarks_are_temporarily_unavailable_on_this_device));
            emptyView.setVisibility(View.VISIBLE);
            Toast.makeText(this, getString(R.string.science_tool_unable_to_open_encrypted_science_bookmarks),
                    Toast.LENGTH_LONG).show();
        }
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showBookmarkDetail` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showBookmarkDetail(ScienceBookmark item) {
        TextView content = new TextView(this);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding, padding, padding);
        content.setTextIsSelectable(true);
        content.setText(formulaFormatter.format(item.getContent()));
        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setView(content)
                .setNegativeButton(getString(R.string.science_tool_remove), (dialog, which) -> {
                    try {
                        repository.deleteForHistory(userId, item.getHistoryId());
                        loadBookmarks();
                    } catch (Exception error) {
                        Toast.makeText(this, getString(R.string.science_tool_unable_to_remove_this_bookmark),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton(getString(R.string.science_tool_close), null)
                .show();
    }

        /*
 * Responsibility: Changes the active selection/state handled by `selectedSubject`.
 * Purpose: Keeps the UI and subject-specific Science context synchronized.
 */
private ScienceSubject selectedSubject(int position) {
        if (position == 1) return ScienceSubject.PHYSICS;
        if (position == 2) return ScienceSubject.CHEMISTRY;
        if (position == 3) return ScienceSubject.BIOLOGY;
        return null;
    }
}
