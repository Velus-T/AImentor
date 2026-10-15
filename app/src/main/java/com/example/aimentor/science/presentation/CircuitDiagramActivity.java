package com.example.aimentor.science.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.R;
import com.example.aimentor.science.presentation.view.CircuitDiagramView;
import com.example.aimentor.science.util.ScienceUiFactory;

public final class CircuitDiagramActivity extends com.example.aimentor.ui.InsetActivity {
    private CircuitDiagramView diagramView;
    private CheckBox switchCheckBox;
    private TextView explanationView;
    private boolean parallel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_circuit_diagram);
        diagramView = findViewById(R.id.circuitDiagramView);
        switchCheckBox = findViewById(R.id.cbCircuitSwitch);
        explanationView = findViewById(R.id.tvCircuitExplanation);
        Spinner typeSpinner = findViewById(R.id.spCircuitType);
        typeSpinner.setAdapter(ScienceUiFactory.spinnerAdapter(this,
                new String[]{"Series circuit", "Parallel circuit"}));
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parallel = position == 1;
                updateDiagram();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        switchCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateDiagram());
        findViewById(R.id.btnCircuitBack).setOnClickListener(view -> finish());
        updateDiagram();
    }

    private void updateDiagram() {
        boolean closed = switchCheckBox.isChecked();
        diagramView.setParallel(parallel);
        diagramView.setSwitchClosed(closed);
        String circuitText = parallel
                ? "In a parallel circuit, each branch has the same potential difference. Current can split between branches, so one lamp may continue working if another branch is opened."
                : "In a series circuit, all components share one current path. The same current passes through the resistor and lamp, while the supply voltage is shared.";
        String switchText = closed
                ? " The closed switch completes the circuit, so conventional current can flow."
                : " The open switch breaks the circuit, so current cannot flow.";
        explanationView.setText(circuitText + switchText
                + " Use this diagram as a learning aid; real circuits must use suitable voltage and adult supervision.");
    }
}
