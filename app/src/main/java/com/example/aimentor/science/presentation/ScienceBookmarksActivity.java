package com.example.aimentor.science.presentation;

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

import com.example.aimentor.R;
import com.example.aimentor.science.di.ScienceModule;
import com.example.aimentor.science.domain.model.ScienceBookmark;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.ScienceBookmarkRepository;
import com.example.aimentor.science.presentation.adapter.ScienceBookmarkAdapter;
import com.example.aimentor.science.util.ChemicalFormulaFormatter;
import com.example.aimentor.science.util.ScienceUserSession;
import com.example.aimentor.science.util.ScienceUiFactory;

import java.util.List;

public final class ScienceBookmarksActivity extends com.example.aimentor.ui.InsetActivity {
    private ScienceBookmarkRepository repository;
    private ScienceBookmarkAdapter adapter;
    private Spinner subjectSpinner;
    private EditText searchInput;
    private TextView emptyView;
    private int userId;
    private final ChemicalFormulaFormatter formulaFormatter = new ChemicalFormulaFormatter();

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
                new String[]{"All subjects", "Physics", "Chemistry", "Biology"}));

        RecyclerView recyclerView = findViewById(R.id.rvScienceBookmarks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScienceBookmarkAdapter(this::showBookmarkDetail);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBookmarksBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnBookmarkSearch).setOnClickListener(view -> loadBookmarks());
        loadBookmarks();
    }

    private void loadBookmarks() {
        try {
            List<ScienceBookmark> items = repository.search(
                    userId,
                    selectedSubject(subjectSpinner.getSelectedItemPosition()),
                    searchInput.getText().toString()
            );
            adapter.submitList(items);
            emptyView.setText("No bookmarks found.");
            emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception error) {
            adapter.submitList(java.util.Collections.emptyList());
            emptyView.setText("Bookmarks are temporarily unavailable on this device.");
            emptyView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Unable to open encrypted Science bookmarks.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showBookmarkDetail(ScienceBookmark item) {
        TextView content = new TextView(this);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding, padding, padding);
        content.setTextIsSelectable(true);
        content.setText(formulaFormatter.format(item.getContent()));
        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setView(content)
                .setNegativeButton("Remove", (dialog, which) -> {
                    try {
                        repository.deleteForHistory(userId, item.getHistoryId());
                        loadBookmarks();
                    } catch (Exception error) {
                        Toast.makeText(this, "Unable to remove this bookmark.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private ScienceSubject selectedSubject(int position) {
        if (position == 1) return ScienceSubject.PHYSICS;
        if (position == 2) return ScienceSubject.CHEMISTRY;
        if (position == 3) return ScienceSubject.BIOLOGY;
        return null;
    }
}
