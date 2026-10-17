package com.example.mentor.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.*;
import com.example.mentor.data.QuizBank;
import java.util.*;

/** Search and count filtering for stored sets; selecting a set never calls an AI service. */
final class QuizBankDialog {
  private QuizBankDialog() {}

  static void show(
      Context context, List<QuizBank.Set> sets, java.util.function.Consumer<QuizBank.Set> select) {
    LinearLayout root = new LinearLayout(context);
    root.setOrientation(LinearLayout.VERTICAL);
    int pad = Math.round(16 * context.getResources().getDisplayMetrics().density);
    root.setPadding(pad, 0, pad, 0);
    EditText search = new EditText(context);
    search.setHint(R.string.mentor_quiz_bank_search);
    search.setSingleLine(true);
    search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
    search.setMinHeight(pad * 3);
    root.addView(search, new LinearLayout.LayoutParams(-1, -2));
    Spinner count = new Spinner(context);
    count.setContentDescription(context.getString(R.string.mentor_quiz_count));
    count.setMinimumHeight(pad * 3);
    String[] options = {
      context.getString(R.string.mentor_quiz_bank_all_counts),
      context.getString(R.string.mentor_quiz_bank_count, 5),
      context.getString(R.string.mentor_quiz_bank_count, 10),
      context.getString(R.string.mentor_quiz_bank_count, 15)
    };
    ArrayAdapter<String> counts =
        new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
    counts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    count.setAdapter(counts);
    root.addView(count, new LinearLayout.LayoutParams(-1, -2));
    TextView empty = new TextView(context);
    empty.setText(R.string.mentor_quiz_bank_no_match);
    empty.setPadding(0, pad, 0, pad);
    root.addView(empty, new LinearLayout.LayoutParams(-1, -2));
    ListView list = new ListView(context);
    root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
    list.setEmptyView(empty);
    ArrayList<QuizBank.Set> visible = new ArrayList<>();
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new ArrayList<>());
    list.setAdapter(adapter);
    Runnable filter =
        () -> {
          visible.clear();
          adapter.clear();
          int required = count.getSelectedItemPosition() * 5;
          for (QuizBank.Set set : sets) {
            String label =
                set.topic
                    + "\n"
                    + scopeLabel(context,set.scope)
                    + " · "
                    + levelLabel(context, set.level)
                    + " · "
                    + context.getString(R.string.mentor_quiz_bank_count, set.count)
                    + "\n"
                    + TimeLabels.date(context, String.valueOf(set.createdAt));
            if ((required == 0 || set.count == required)
                && SearchText.contains(label+" "+set.scope+" "+set.level, search.getText().toString())) {
              visible.add(set);
              adapter.add(label);
            }
          }
          adapter.notifyDataSetChanged();
        };
    search.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            filter.run();
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
    count.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> p, android.view.View v, int position, long id) {
            filter.run();
          }

          @Override
          public void onNothingSelected(AdapterView<?> p) {}
        });
    AlertDialog dialog =
        new AlertDialog.Builder(context)
            .setTitle(R.string.mentor_quiz_bank)
            .setView(root)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    list.setOnItemClickListener(
        (p, v, position, id) -> {
          QuizBank.Set set = visible.get(position);
          dialog.dismiss();
          select.accept(set);
        });
    search.setOnEditorActionListener(
        (v, action, event) -> {
          ((android.view.inputmethod.InputMethodManager)
                  context.getSystemService(Context.INPUT_METHOD_SERVICE))
              .hideSoftInputFromWindow(search.getWindowToken(), 0);
          search.clearFocus();
          return true;
        });
    filter.run();
    dialog.show();
    if (dialog.getWindow() != null) {
      dialog
          .getWindow()
          .setSoftInputMode(
              WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                  | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      android.util.DisplayMetrics metrics = context.getResources().getDisplayMetrics();
      dialog
          .getWindow()
          .setLayout(
              Math.min(metrics.widthPixels - pad * 2, pad * 35),
              Math.round(metrics.heightPixels * .82f));
    }
  }

  private static String scopeLabel(Context context,String raw) {
    if("math".equalsIgnoreCase(raw))return context.getString(R.string.mentor_quiz_scope_math);
    if("programming".equalsIgnoreCase(raw)||"Lập trình".equals(raw))return context.getString(R.string.mentor_quiz_scope_programming);
    if("physics".equalsIgnoreCase(raw))return context.getString(R.string.mentor_quiz_scope_physics);
    if("chemistry".equalsIgnoreCase(raw))return context.getString(R.string.mentor_quiz_scope_chemistry);
    if("biology".equalsIgnoreCase(raw))return context.getString(R.string.mentor_quiz_scope_biology);
    if("Lịch sử Việt Nam".equals(raw))return context.getString(R.string.mentor_quiz_scope_history_vietnam);
    if("Lịch sử nước ngoài".equals(raw))return context.getString(R.string.mentor_quiz_scope_history_world);
    if("Tổng hợp".equals(raw))return context.getString(R.string.mentor_quiz_scope_history_mixed);
    return raw;
  }

  private static String levelLabel(Context c, String raw) {
    if ("MIDDLE_SCHOOL".equals(raw)
        || "Middle School".equalsIgnoreCase(raw)
        || "Trung học cơ sở".equals(raw)) return c.getString(R.string.mentor_quiz_middle);
    if ("HIGH_SCHOOL".equals(raw)
        || "High School".equalsIgnoreCase(raw)
        || "Trung học phổ thông".equals(raw)) return c.getString(R.string.mentor_quiz_high);
    if ("UNIVERSITY".equalsIgnoreCase(raw) || "Đại học".equals(raw))
      return c.getString(R.string.mentor_quiz_university);
    return raw;
  }
}
