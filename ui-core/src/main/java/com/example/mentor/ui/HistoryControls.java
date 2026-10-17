package com.example.mentor.ui;

import android.os.Bundle;
import android.text.*;
import android.view.View;
import android.widget.*;
import com.example.mentor.data.HistoryPeriod;
import java.util.*;
import java.util.function.Function;

/** Shared local search, date controls and empty state for every history tab. */
public final class HistoryControls {
  private final EditText search;
  private final Spinner period;
  private final TextView empty;

  public HistoryControls(View root, Bundle state, Runnable changed) {
    search = root.findViewById(R.id.mentor_history_search);
    period = root.findViewById(R.id.mentor_history_period);
    empty = root.findViewById(R.id.mentor_history_empty);
    UiPalette.secondary(root.findViewById(R.id.mentor_history_new));
    UiPalette.text(root.findViewById(R.id.mentor_history_clear));
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(
            root.getContext(),
            android.R.layout.simple_spinner_item,
            HistoryLabels.periods(root.getContext()));
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    period.setAdapter(adapter);
    if (state != null) {
      search.setText(state.getString("history_query", ""));
      period.setSelection(Math.max(0, Math.min(4, state.getInt("history_period", 0))));
    }
    View clear = root.findViewById(R.id.mentor_history_clear);
    clear.setVisibility(search.length() == 0 ? View.INVISIBLE : View.VISIBLE);
    clear.setOnClickListener(v -> search.setText(""));
    View heading = (View) root.findViewById(R.id.mentor_history_heading).getParent(),
        newRow =
            root.findViewById(R.id.mentor_history_new).getParent() instanceof View
                ? (View) root.findViewById(R.id.mentor_history_new).getParent()
                : root.findViewById(R.id.mentor_history_new);
    int[] previous = {heading.getVisibility(), newRow.getVisibility()};
    search.setOnFocusChangeListener(
        (v, focused) -> {
          if (focused) {
            previous[0] = heading.getVisibility();
            previous[1] = newRow.getVisibility();
            heading.setVisibility(View.GONE);
            newRow.setVisibility(View.GONE);
          } else {
            heading.setVisibility(previous[0]);
            newRow.setVisibility(previous[1]);
          }
        });
    period.setOnTouchListener(
        (v, event) -> {
          if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
            search.clearFocus();
            ((android.view.inputmethod.InputMethodManager)
                    root.getContext()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(search.getWindowToken(), 0);
          }
          return false;
        });
    search.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            clear.setVisibility(s.length() == 0 ? View.INVISIBLE : View.VISIBLE);
            changed.run();
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
    period.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
            changed.run();
          }

          @Override
          public void onNothingSelected(AdapterView<?> p) {}
        });
    search.setOnEditorActionListener(
        (v, action, event) -> {
          ((android.view.inputmethod.InputMethodManager)
                  root.getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE))
              .hideSoftInputFromWindow(search.getWindowToken(), 0);
          search.clearFocus();
          return true;
        });
  }

  public <T> List<T> filter(List<T> all, Function<T, String> text, Function<T, Long> timestamp) {
    ArrayList<T> result = new ArrayList<>();
    long now = System.currentTimeMillis();
    for (T item : all)
      if (SearchText.contains(text.apply(item), search.getText().toString())
          && HistoryPeriod.contains(
              period.getSelectedItemPosition(), timestamp.apply(item), now, TimeZone.getDefault()))
        result.add(item);
    empty.setText(
        search.length() > 0 || period.getSelectedItemPosition() != HistoryPeriod.ALL
            ? R.string.mentor_history_no_match
            : R.string.mentor_history_empty);
    empty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
    return result;
  }

  public void save(Bundle out) {
    out.putString("history_query", search.getText().toString());
    out.putInt("history_period", period.getSelectedItemPosition());
  }
}
