package com.example.mathmodule.calculator;

import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import com.example.mathmodule.R;

/** In-app Mathematics bubble; never requests system overlay access. */
public final class MathCalculator implements Application.ActivityLifecycleCallbacks {
  private static final java.util.Map<Activity, android.app.Dialog> PANELS =
      new java.util.WeakHashMap<>();
  public static final String TAG = "mentor_math_calculator";

  public static void install(Application app) {
    app.registerActivityLifecycleCallbacks(new MathCalculator());
  }

  private static int dp(Context c, int n) {
    return Math.round(n * c.getResources().getDisplayMetrics().density);
  }

  private static SharedPreferences prefs(Context c) {
    return c.getApplicationContext().getSharedPreferences("math_calculator", 0);
  }

  public static void show(Activity a) {
    prefs(a).edit().putBoolean("hidden", false).apply();
    attach(a);
    open(a);
  }

  private static void attach(Activity a) {
    FrameLayout root = a.findViewById(android.R.id.content);
    if (root == null || root.findViewWithTag(TAG) != null || prefs(a).getBoolean("hidden", false))
      return;
    FrameLayout layer = new FrameLayout(a);
    layer.setTag(TAG);
    root.addView(layer, new FrameLayout.LayoutParams(-1, -1));
    ImageButton bubble = new ImageButton(a);
    bubble.setImageResource(R.drawable.ic_math_calculator_bubble);
    bubble.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
    bubble.setContentDescription(a.getString(R.string.math_calculator_open));
    bubble.setPadding(dp(a, 14), dp(a, 14), dp(a, 14), dp(a, 14));
    GradientDrawable bg = new GradientDrawable();
    bg.setShape(GradientDrawable.OVAL);
    bg.setColor(ContextCompat.getColor(a, R.color.mentor_math_button));
    bubble.setBackground(bg);
    ViewCompat.setElevation(bubble, dp(a, 6));
    layer.addView(bubble, new FrameLayout.LayoutParams(dp(a, 56), dp(a, 56)));
    layer.post(
        () -> {
          bubble.setX(
              Math.max(0, layer.getWidth() - bubble.getWidth()) * prefs(a).getFloat("x", 1));
          bubble.setY(
              Math.max(0, layer.getHeight() - bubble.getHeight() - dp(a, 88))
                  * prefs(a).getFloat("y", 0.72f));
        });
    layer.addOnLayoutChangeListener(
        (v, l, t, r, b, ol, ot, or, ob) -> {
          bubble.setX(Math.max(0, Math.min(bubble.getX(), layer.getWidth() - bubble.getWidth())));
          bubble.setY(
              Math.max(
                  0, Math.min(bubble.getY(), layer.getHeight() - bubble.getHeight() - dp(a, 88))));
        });
    bubble.setOnClickListener(v -> open(a));
    bubble.setOnLongClickListener(
        v -> {
          new androidx.appcompat.app.AlertDialog.Builder(a)
              .setMessage(R.string.math_calculator_hide_hint)
              .setPositiveButton(R.string.math_calculator_hide, (d, w) -> hide(a))
              .setNegativeButton(android.R.string.cancel, null)
              .show();
          return true;
        });
    bubble.setOnTouchListener(
        new View.OnTouchListener() {
          float x, y, startX, startY;
          boolean dragged;

          public boolean onTouch(View v, MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
              x = e.getRawX();
              y = e.getRawY();
              startX = v.getX();
              startY = v.getY();
              dragged = false;
              return false;
            }
            if (e.getActionMasked() == MotionEvent.ACTION_MOVE) {
              if (Math.hypot(e.getRawX() - x, e.getRawY() - y) > dp(a, 8)) dragged = true;
              if (dragged) {
                v.cancelLongPress();
                v.setPressed(false);
                v.setX(
                    Math.max(
                        0, Math.min(startX + e.getRawX() - x, layer.getWidth() - v.getWidth())));
                v.setY(
                    Math.max(
                        0,
                        Math.min(
                            startY + e.getRawY() - y,
                            layer.getHeight() - v.getHeight() - dp(a, 88))));
                return true;
              }
            }
            if (e.getActionMasked() == MotionEvent.ACTION_UP && dragged) {
              float edge =
                  v.getX() < layer.getWidth() / 2f
                      ? 0
                      : Math.max(0, layer.getWidth() - v.getWidth());
              v.animate().x(edge).setDuration(140).start();
              prefs(a)
                  .edit()
                  .putFloat("x", edge == 0 ? 0 : 1)
                  .putFloat(
                      "y", v.getY() / Math.max(1, layer.getHeight() - v.getHeight() - dp(a, 88)))
                  .apply();
              return true;
            }
            return false;
          }
        });
  }

  private static void hide(Activity a) {
    prefs(a).edit().putBoolean("hidden", true).apply();
    ViewGroup root = a.findViewById(android.R.id.content);
    View layer = root.findViewWithTag(TAG);
    if (layer != null) root.removeView(layer);
  }

  public static void open(Activity a) {
    if (a.isFinishing() || a.isDestroyed() || (PANELS.get(a) != null && PANELS.get(a).isShowing()))
      return;
    LinearLayout body = new LinearLayout(a);
    body.setOrientation(LinearLayout.VERTICAL);
    body.setPadding(dp(a, 12), dp(a, 8), dp(a, 12), dp(a, 8));
    TextView display = new TextView(a);
    display.setTextSize(24);
    display.setTextColor(ContextCompat.getColor(a, R.color.math_text_primary));
    display.setMinHeight(dp(a, 64));
    display.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
    display.setTextIsSelectable(true);
    display.setMaxLines(2);
    display.setEllipsize(android.text.TextUtils.TruncateAt.START);
    StringBuilder expression = new StringBuilder(prefs(a).getString("expression", ""));
    display.setText(expression.length() == 0 ? "0" : expression.toString());
    display.setTag("mentor_calculator_display");
    display.setPadding(dp(a, 16), dp(a, 4), dp(a, 16), dp(a, 4));
    final boolean[] justCalculated = {prefs(a).getBoolean("result", false)};
    String[] keys = {
      "C", "(", ")", "⌫", "7", "8", "9", "÷", "4", "5", "6", "×", "1", "2", "3", "−", "±", "0", ".",
      "+", "%", "√", "x²", "="
    };
    for (int row = 0; row < 6; row++) {
      LinearLayout line = new LinearLayout(a);
      for (int col = 0; col < 4; col++) {
        String key = keys[row * 4 + col];
        com.google.android.material.button.MaterialButton button =
            new com.google.android.material.button.MaterialButton(a);
        button.setText(key);
        button.setTextSize(18);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(0, 0, 0, 0);
        button.setCornerRadius(dp(a, 12));
        button.setMinHeight(dp(a, 48));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(a, 2), 0, dp(a, 2), 0);
        line.addView(button, p);
        if (key.equals("⌫"))
          button.setContentDescription(a.getString(R.string.math_calculator_backspace));
        button.setOnClickListener(
            v -> {
              try {
                if (justCalculated[0]
                    && (Character.isDigit(key.charAt(0)) || key.equals(".") || key.equals("(")))
                  expression.setLength(0);
                justCalculated[0] = false;
                switch (key) {
                  case "C":
                    expression.setLength(0);
                    break;
                  case "⌫":
                    if (expression.length() > 0) expression.deleteCharAt(expression.length() - 1);
                    break;
                  case "±":
                    if (expression.length() > 0) {
                      String previous = expression.toString();
                      expression.setLength(0);
                      expression.append("-(").append(previous).append(")");
                    }
                    break;
                  case "√":
                    boolean complete = expression.length() > 0;
                    if (complete) {
                      try {
                        CalculatorEngine.calculate(expression.toString());
                      } catch (IllegalArgumentException incomplete) {
                        complete = false;
                      }
                    }
                    if (complete) {
                      String root = CalculatorEngine.calculate("sqrt(" + expression + ")");
                      expression.setLength(0);
                      expression.append(root);
                      justCalculated[0] = true;
                    } else expression.append("sqrt(");
                    break;
                  case "x²":
                    String square =
                        CalculatorEngine.calculate(
                            "(" + (expression.length() == 0 ? "0" : expression) + ")^2");
                    expression.setLength(0);
                    expression.append(square);
                    justCalculated[0] = true;
                    break;
                  case "=":
                    String result = CalculatorEngine.calculate(expression.toString());
                    expression.setLength(0);
                    expression.append(result);
                    justCalculated[0] = true;
                    break;
                  default:
                    if (expression.length() < 160) expression.append(key);
                }
                display.setText(expression.length() == 0 ? "0" : expression.toString());
                prefs(a)
                    .edit()
                    .putString("expression", expression.toString())
                    .putBoolean("result", justCalculated[0])
                    .apply();
              } catch (IllegalArgumentException error) {
                display.setText(R.string.math_calculator_error);
              }
            });
      }
      body.addView(line);
    }
    TextView hint = new TextView(a);
    hint.setText(R.string.math_calculator_hint);
    hint.setTextSize(12);
    hint.setTextColor(ContextCompat.getColor(a, R.color.math_text_secondary));
    body.addView(hint);
    ScrollView scroll = new ScrollView(a);
    scroll.addView(body);
    scroll.setTag("mentor_calculator_keypad");
    LinearLayout panel = new LinearLayout(a);
    panel.setOrientation(LinearLayout.VERTICAL);
    panel.addView(display, new LinearLayout.LayoutParams(-1, -2));
    panel.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
    androidx.appcompat.app.AlertDialog dialog =
        new androidx.appcompat.app.AlertDialog.Builder(a)
            .setTitle(R.string.math_calculator_title)
            .setView(panel)
            .setNegativeButton(R.string.math_calculator_collapse, null)
            .setNeutralButton(R.string.math_calculator_hide, (d, w) -> hide(a))
            .create();
    PANELS.put(a, dialog);
    dialog.setOnDismissListener(d -> PANELS.remove(a));
    dialog.show();
    Window window = dialog.getWindow();
    if (window != null) {
      int width =
          Math.min(dp(a, 360), a.getResources().getDisplayMetrics().widthPixels - dp(a, 24));
      window.setLayout(width, -2);
      window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      int available = a.getResources().getDisplayMetrics().heightPixels - dp(a, 310);
      scroll.setLayoutParams(
          new LinearLayout.LayoutParams(-1, Math.min(dp(a, 376), Math.max(dp(a, 72), available))));
      // Large text can stack dialog actions. Keep the keypad scrollable above both actions.
      View decor = window.getDecorView();
      decor.getViewTreeObserver().addOnGlobalLayoutListener(
          new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
              if (scroll.getHeight() <= 0) return;
              decor.getViewTreeObserver().removeOnGlobalLayoutListener(this);
              int[] origin = new int[2];
              decor.getLocationOnScreen(origin);
              int bottom = origin[1] + decor.getHeight() - dp(a, 12);
              int overflow = 0;
              for (int action : new int[] {android.content.DialogInterface.BUTTON_NEGATIVE,
                  android.content.DialogInterface.BUTTON_NEUTRAL}) {
                Button button = dialog.getButton(action);
                if (button != null) {
                  int[] position = new int[2];
                  button.getLocationOnScreen(position);
                  overflow = Math.max(overflow, position[1] + button.getHeight() - bottom);
                }
              }
              if (overflow > 0) scroll.setLayoutParams(new LinearLayout.LayoutParams(
                  -1, Math.max(dp(a, 48), scroll.getHeight() - overflow)));
            }
          });
    }
  }

  public void onActivityResumed(Activity a) {
    if (a.getClass().getName().startsWith("com.example.mathmodule.")) {
      ViewGroup root = a.findViewById(android.R.id.content);
      View old = root == null ? null : root.findViewWithTag(TAG);
      if (old != null) root.removeView(old);
      attach(a);
    }
  }

  public void onActivityCreated(Activity a, Bundle b) {}

  public void onActivityStarted(Activity a) {}

  public void onActivityPaused(Activity a) {}

  public void onActivityStopped(Activity a) {}

  public void onActivitySaveInstanceState(Activity a, Bundle b) {}

  public void onActivityDestroyed(Activity a) {
    android.app.Dialog dialog = PANELS.remove(a);
    if (dialog != null) dialog.dismiss();
  }
}
