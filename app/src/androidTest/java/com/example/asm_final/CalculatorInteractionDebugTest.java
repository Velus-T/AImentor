package com.example.asm_final;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.*;
import android.os.SystemClock;
import android.view.*;
import android.widget.FrameLayout;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CalculatorInteractionDebugTest {
  private final Instrumentation ins = InstrumentationRegistry.getInstrumentation();
  private SharedPreferences prefs;
  private java.util.Map<String, ?> original;
  private Activity activity;

  private void main(Runnable action) {
    ins.runOnMainSync(action);
    ins.waitForIdleSync();
  }

  @Before
  public void open() {
    prefs = ins.getTargetContext().getSharedPreferences("math_calculator", 0);
    original = new java.util.HashMap<>(prefs.getAll());
    prefs.edit().clear().apply();
    activity =
        ins.startActivitySync(
            new Intent(ins.getTargetContext(), com.example.mathmodule.MathDashboardActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    ins.waitForIdleSync();
  }

  @After
  public void close() {
    main(() -> activity.finish());
    SharedPreferences.Editor editor = prefs.edit().clear();
    for (java.util.Map.Entry<String, ?> e : original.entrySet()) {
      Object value = e.getValue();
      if (value instanceof String) editor.putString(e.getKey(), (String) value);
      else if (value instanceof Boolean) editor.putBoolean(e.getKey(), (Boolean) value);
      else if (value instanceof Float) editor.putFloat(e.getKey(), (Float) value);
    }
    editor.apply();
  }

  @Test
  public void repeatedSquareAndRootOperateOnCurrentValue() {
    main(() -> com.example.mathmodule.calculator.MathCalculator.show(activity));
    for (String key : new String[] {"2", "x²", "x²", "x²", "="})
      onView(withText(key)).perform(scrollTo(), click());
    onView(withText("256")).check(matches(isDisplayed()));
    for (String key : new String[] {"C", "9", "√", "="})
      onView(withText(key)).perform(scrollTo(), click());
    assertEquals("3", prefs.getString("expression", ""));
    onView(withText("√")).perform(scrollTo(), click());
    onView(withText("=")).perform(scrollTo(), click());
    onView(withText("1.73205080757")).check(matches(isDisplayed()));
  }

  private void touch(View view, int action, float x, float y, long down) {
    main(
        () -> {
          MotionEvent event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x, y, 0);
          view.dispatchTouchEvent(event);
          event.recycle();
        });
  }

  @Test
  public void draggingStaysInsideSafeContentAndDoesNotOpenHideDialog() {
    final FrameLayout[] layer = {null};
    final View[] bubble = {null};
    main(
        () -> {
          layer[0] =
              activity
                  .findViewById(android.R.id.content)
                  .findViewWithTag(com.example.mathmodule.calculator.MathCalculator.TAG);
          bubble[0] = layer[0].getChildAt(0);
        });
    long down = SystemClock.uptimeMillis();
    touch(bubble[0], MotionEvent.ACTION_DOWN, 20, 20, down);
    touch(bubble[0], MotionEvent.ACTION_MOVE, -4000, 4000, down);
    SystemClock.sleep(ViewConfiguration.getLongPressTimeout() + 100);
    touch(bubble[0], MotionEvent.ACTION_UP, -4000, 4000, down);
    SystemClock.sleep(180);
    ins.waitForIdleSync();
    onView(withText(com.example.mathmodule.R.string.math_calculator_hide_hint))
        .check(doesNotExist());
    main(
        () -> {
          assertEquals(0f, bubble[0].getX(), 1f);
          assertTrue(bubble[0].getY() >= 0);
          assertTrue(bubble[0].getY() + bubble[0].getHeight() <= layer[0].getHeight());
        });
    assertEquals(0f, prefs.getFloat("x", -1), 0f);
  }
}
