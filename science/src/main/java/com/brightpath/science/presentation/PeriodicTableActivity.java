package com.brightpath.science.presentation;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.ChemicalElement;
import com.brightpath.science.domain.repository.PeriodicTableRepository;
import com.brightpath.science.presentation.adapter.PeriodicElementAdapter;

import java.util.List;

public final class PeriodicTableActivity extends AppCompatActivity {
    private PeriodicTableRepository repository;
    private PeriodicElementAdapter adapter;
    private EditText searchInput;
    private TextView countView;

        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_periodic_table);
        repository = ScienceModule.get(this).periodicTableRepository();
        searchInput = findViewById(R.id.etElementSearch);
        countView = findViewById(R.id.tvElementCount);

        RecyclerView recyclerView = findViewById(R.id.rvPeriodicTable);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new PeriodicElementAdapter(this::showElement);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnPeriodicBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnElementSearch).setOnClickListener(view -> loadElements());
        loadElements();
    }

        /*
 * Responsibility: Retrieves the data/state required by `loadElements` from the appropriate Android screen and user-interaction controller.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
private void loadElements() {
        try {
            List<ChemicalElement> elements = repository.search(searchInput.getText().toString());
            adapter.submitList(elements);
            countView.setText(getString(R.string.science_elements_count, elements.size()));
        } catch (Exception error) {
            adapter.submitList(java.util.Collections.emptyList());
            countView.setText(getString(R.string.science_tool_periodic_table_data_could_not_be_loaded));
            Toast.makeText(this, getString(R.string.science_tool_unable_to_load_the_periodic_table),
                    Toast.LENGTH_LONG).show();
        }
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showElement` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showElement(ChemicalElement element) {
        String details = getString(R.string.science_element_details,
                element.getAtomicNumber(), element.getSymbol(), element.getAtomicMass(),
                element.getGroup(), element.getPeriod(), element.getCategory(),
                element.getDescription(), element.getCommonUse());
        new AlertDialog.Builder(this)
                .setTitle(element.getName() + " (" + element.getSymbol() + ")")
                .setMessage(details)
                .setPositiveButton(getString(R.string.science_tool_close), null)
                .show();
    }
}
