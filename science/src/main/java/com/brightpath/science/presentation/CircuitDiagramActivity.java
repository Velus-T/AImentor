package com.brightpath.science.presentation;

import android.os.Bundle;
import android.text.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.brightpath.science.R;
import com.brightpath.science.domain.model.CircuitModel;
import com.brightpath.science.presentation.view.CircuitDiagramView;
import com.brightpath.science.util.ScienceUiFactory;

public final class CircuitDiagramActivity extends AppCompatActivity {
  private CircuitDiagramView diagram;
  private CheckBox closed;
  private EditText voltage, r1, r2;
  private TextView explanation, result;
  private boolean parallel;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_science_circuit_diagram);
    diagram = findViewById(R.id.circuitDiagramView);
    closed = findViewById(R.id.cbCircuitSwitch);
    explanation = findViewById(R.id.tvCircuitExplanation);
    result = findViewById(R.id.tvCircuitResult);
    voltage = findViewById(R.id.etCircuitVoltage);
    r1 = findViewById(R.id.etCircuitR1);
    r2 = findViewById(R.id.etCircuitR2);
    Spinner type = findViewById(R.id.spCircuitType);
    type.setAdapter(
        ScienceUiFactory.spinnerAdapter(
            this,
            new String[] {
              getString(R.string.science_circuit_series),
              getString(R.string.science_circuit_parallel)
            }));
    if (state != null) {
      parallel = state.getBoolean("parallel");
      type.setSelection(parallel ? 1 : 0);
      closed.setChecked(state.getBoolean("closed", true));
    }
    type.setOnItemSelectedListener(
        new android.widget.AdapterView.OnItemSelectedListener() {
          public void onItemSelected(
              android.widget.AdapterView<?> p, android.view.View v, int position, long id) {
            parallel = position == 1;
            update();
          }

          public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
    TextWatcher watcher =
        new TextWatcher() {
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          public void onTextChanged(CharSequence s, int start, int before, int count) {
            update();
          }

          public void afterTextChanged(Editable e) {}
        };
    voltage.addTextChangedListener(watcher);
    r1.addTextChangedListener(watcher);
    r2.addTextChangedListener(watcher);
    closed.setOnCheckedChangeListener((v, checked) -> update());
    diagram.setOnClickListener(v -> closed.toggle());
    findViewById(R.id.btnCircuitBack).setOnClickListener(v -> finish());
    update();
  }

  private double value(EditText field) {
    return Double.parseDouble(field.getText().toString().trim().replace(',', '.'));
  }

  private void update() {
    diagram.setParallel(parallel);
    diagram.setSwitchClosed(closed.isChecked());
    explanation.setText(
        getString(
                parallel
                    ? R.string.science_circuit_parallel_desc
                    : R.string.science_circuit_series_desc)
            + "\n"
            + getString(
                closed.isChecked()
                    ? R.string.science_circuit_switch_closed_desc
                    : R.string.science_circuit_switch_open_desc)
            + "\n"
            + getString(R.string.science_circuit_model_hint));
    try {
      double u = value(voltage);
      CircuitModel model =
          CircuitModel.calculate(u, value(r1), value(r2), parallel, closed.isChecked());
      diagram.setEnergized(u > 0);
      result.setText(
          getString(
              R.string.science_circuit_result,
              model.resistance,
              model.current,
              model.current1,
              model.current2,
              model.voltage1,
              model.voltage2,
              model.power));
    } catch (IllegalArgumentException error) {
      diagram.setEnergized(false);
      result.setText(R.string.science_circuit_invalid);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle out) {
    out.putBoolean("parallel", parallel);
    out.putBoolean("closed", closed.isChecked());
    super.onSaveInstanceState(out);
  }
}
