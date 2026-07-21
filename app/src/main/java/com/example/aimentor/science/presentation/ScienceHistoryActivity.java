package com.example.aimentor.science.presentation;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.aimentor.science.domain.model.ScienceHistory;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.ScienceHistoryRepository;
import com.example.aimentor.science.presentation.adapter.ScienceHistoryAdapter;
import com.example.aimentor.science.util.ChemicalFormulaFormatter;
import com.example.aimentor.science.util.ScienceUserSession;
import com.example.aimentor.science.util.ScienceUiFactory;

import java.util.List;

public final class ScienceHistoryActivity extends AppCompatActivity {
    private ScienceHistoryRepository repository;
    private ScienceHistoryAdapter adapter;
    private Spinner subjectSpinner;
    private EditText searchInput;
    private TextView emptyView;
    private int userId;
    private final ChemicalFormulaFormatter formulaFormatter = new ChemicalFormulaFormatter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_history);

        repository = ScienceModule.get(this).historyRepository();
        userId = new ScienceUserSession(this).getUserId();
        subjectSpinner = findViewById(R.id.spHistorySubject);
        searchInput = findViewById(R.id.etHistorySearch);
        emptyView = findViewById(R.id.tvHistoryEmpty);

        subjectSpinner.setAdapter(ScienceUiFactory.spinnerAdapter(this,
                new String[]{"All subjects", "Physics", "Chemistry", "Biology"}));

        RecyclerView recyclerView = findViewById(R.id.rvScienceHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScienceHistoryAdapter(this::showHistoryDetail);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnHistoryBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnHistorySearch).setOnClickListener(view -> loadHistory());
        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) loadHistory();
    }

    private void loadHistory() {
        try {
            List<ScienceHistory> items = repository.search(
                    userId,
                    selectedSubject(subjectSpinner.getSelectedItemPosition()),
                    searchInput.getText().toString()
            );
            adapter.submitList(items);
            emptyView.setText("No science history found.");
            emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception error) {
            adapter.submitList(java.util.Collections.emptyList());
            emptyView.setText("History is temporarily unavailable on this device.");
            emptyView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Unable to open encrypted Science history.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showHistoryDetail(ScienceHistory item) {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, padding);
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            ImageView image = new ImageView(this);
            image.setAdjustViewBounds(true);
            image.setMaxHeight((int) (220 * getResources().getDisplayMetrics().density));
            image.setImageURI(Uri.parse(item.getImageUri()));
            container.addView(image, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        }
        TextView content = new TextView(this);
        content.setTextIsSelectable(true);
        content.setText(formulaFormatter.format(
                item.getQuestion() + "\n\n" + item.getAnswer()
        ));
        container.addView(content);
        long openedAt = System.currentTimeMillis();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(item.getSubject().getDisplayName() + " answer")
                .setView(container)
                .setPositiveButton("Close", null)
                .create();
        dialog.setOnDismissListener(ignored -> {
            int seconds = (int) Math.max(1, (System.currentTimeMillis() - openedAt) / 1000);
            try {
                repository.addReviewDuration(item.getId(), seconds);
            } catch (Exception ignoredError) {
                // Review timing is optional and must never close the app.
            }
        });
        dialog.show();
    }

    private ScienceSubject selectedSubject(int position) {
        if (position == 1) return ScienceSubject.PHYSICS;
        if (position == 2) return ScienceSubject.CHEMISTRY;
        if (position == 3) return ScienceSubject.BIOLOGY;
        return null;
    }
}
