package com.example.aimentor.science.presentation;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimentor.R;
import com.example.aimentor.science.di.ScienceModule;
import com.example.aimentor.science.domain.model.ChemicalElement;
import com.example.aimentor.science.domain.repository.PeriodicTableRepository;
import com.example.aimentor.science.presentation.adapter.PeriodicElementAdapter;

import java.util.List;

public final class PeriodicTableActivity extends AppCompatActivity {
    private PeriodicTableRepository repository;
    private PeriodicElementAdapter adapter;
    private EditText searchInput;
    private TextView countView;

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

    private void loadElements() {
        try {
            List<ChemicalElement> elements = repository.search(searchInput.getText().toString());
            adapter.submitList(elements);
            countView.setText(elements.size() + " element(s) shown");
        } catch (Exception error) {
            adapter.submitList(java.util.Collections.emptyList());
            countView.setText("Periodic-table data could not be loaded.");
            Toast.makeText(this, "Unable to load the periodic table.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showElement(ChemicalElement element) {
        String details = "Atomic number: " + element.getAtomicNumber()
                + "\nSymbol: " + element.getSymbol()
                + "\nAtomic mass: " + element.getAtomicMass()
                + "\nGroup: " + element.getGroup()
                + "\nPeriod: " + element.getPeriod()
                + "\nCategory: " + element.getCategory()
                + "\n\n" + element.getDescription()
                + "\n\nCommon use: " + element.getCommonUse();
        new AlertDialog.Builder(this)
                .setTitle(element.getName() + " (" + element.getSymbol() + ")")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }
}
