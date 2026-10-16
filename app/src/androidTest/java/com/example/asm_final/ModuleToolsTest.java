package com.example.asm_final;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import com.example.asm_final.utils.*;
import java.util.*;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ModuleToolsTest {
  private final Instrumentation ins = InstrumentationRegistry.getInstrumentation();

  private Activity resumed() {
    return ActivityLifecycleMonitorRegistry.getInstance()
        .getActivitiesInStage(Stage.RESUMED)
        .iterator()
        .next();
  }

  private void main(Runnable task) {
    ins.runOnMainSync(task);
    ins.waitForIdleSync();
  }

  private void start(Class<?> type) {
    ins.startActivitySync(
        new Intent(ins.getTargetContext(), type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  }

  private void waitFor(Predicate<Activity> condition) {
    long end = android.os.SystemClock.uptimeMillis() + 15000;
    while (android.os.SystemClock.uptimeMillis() < end) {
      boolean[] ready = {false};
      ins.runOnMainSync(
          () -> {
            Collection<Activity> list =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            if (!list.isEmpty()) ready[0] = condition.test(list.iterator().next());
          });
      if (ready[0]) return;
      android.os.SystemClock.sleep(50);
    }
    fail("UI did not become ready");
  }

  private void login() {
    DebugAccounts.seed(ins.getTargetContext());
    start(com.example.asm_final.activities.MainActivity.class);
    main(
        () -> {
          ((EditText) resumed().findViewById(R.id.edtLoginUsername)).setText("hocvien01");
          ((EditText) resumed().findViewById(R.id.edtLoginPassword)).setText("Mentor01!2026");
          resumed().findViewById(R.id.btnLogin).performClick();
        });
    waitFor(a -> a instanceof com.example.asm_final.activities.HomeActivity);
  }

  private void capture(String name) {
    try {
      ins.getUiAutomation().waitForIdle(250, 3000);
    } catch (java.util.concurrent.TimeoutException error) {
      throw new AssertionError(error);
    }
    android.graphics.Bitmap bitmap = ins.getUiAutomation().takeScreenshot();
    java.io.File folder = ins.getTargetContext().getExternalFilesDir("verified-ui");
    try (java.io.FileOutputStream output =
        new java.io.FileOutputStream(new java.io.File(folder, name + ".png"))) {
      bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output);
    } catch (java.io.IOException error) {
      throw new AssertionError(error);
    } finally {
      bitmap.recycle();
    }
  }

  @Test
  public void specializedScienceToolsRemainAccessible() {
    Class<?>[] tools = {
      com.brightpath.science.presentation.PeriodicTableActivity.class,
      com.brightpath.science.presentation.CircuitDiagramActivity.class,
      com.brightpath.science.presentation.ScienceStudyPlannerActivity.class,
      com.brightpath.science.presentation.ScienceProgressActivity.class,
      com.brightpath.science.presentation.ScienceBookmarksActivity.class,
      com.brightpath.science.presentation.ScienceHistoryActivity.class,
      com.brightpath.science.presentation.ScienceSettingsActivity.class
    };
    for (Class<?> tool : tools) {
      start(tool);
      main(() -> {
        if (tool == com.brightpath.science.presentation.ScienceHistoryActivity.class) {
          assertTrue(resumed() instanceof com.brightpath.science.presentation.ScienceMainActivity);
          assertEquals(com.brightpath.science.R.id.navScienceHistory,
              ((com.google.android.material.bottomnavigation.BottomNavigationView) resumed().findViewById(
                  com.brightpath.science.R.id.scienceBottomNavigation)).getSelectedItemId());
        } else assertEquals(tool, resumed().getClass());
        assertNotNull(resumed().findViewById(android.R.id.content));
        assertNull(resumed().findViewById(android.R.id.content).findViewWithTag(
            com.example.mathmodule.calculator.MathCalculator.TAG));
      });
      main(() -> resumed().finish());
    }
  }

  @Test
  public void calculatorIsMathOnlyAndCanBeHiddenAndReopened() {
    Context c = ins.getTargetContext();
    android.content.SharedPreferences prefs = c.getSharedPreferences("math_calculator", 0);
    String expression = prefs.getString("expression", "");
    boolean hidden = prefs.getBoolean("hidden", false);
    prefs.edit().putBoolean("hidden", false).putString("expression", "").apply();
    try {
      start(com.example.mathmodule.MathDashboardActivity.class);
      main(
          () -> {
            ((AppCompatActivity) resumed())
                .getSupportFragmentManager()
                .executePendingTransactions();
            assertNotNull(
                resumed()
                    .findViewById(android.R.id.content)
                    .findViewWithTag(com.example.mathmodule.calculator.MathCalculator.TAG));
            resumed()
                .findViewById(com.example.mathmodule.R.id.button_math_calculator)
                .performClick();
          });
      onView(withText("7")).perform(click());
      onView(withText("×")).perform(click());
      onView(withText("8")).perform(click());
      onView(withText("=")).perform(click());
      onView(withText("56")).check(matches(isDisplayed()));
      capture("calculator");
      onView(withText(com.example.mathmodule.R.string.math_calculator_hide)).perform(click());
      main(
          () ->
              assertNull(
                  resumed()
                      .findViewById(android.R.id.content)
                      .findViewWithTag(com.example.mathmodule.calculator.MathCalculator.TAG)));
      main(
          () ->
              resumed()
                  .findViewById(com.example.mathmodule.R.id.button_math_calculator)
                  .performClick());
      onView(withText("56")).check(matches(isDisplayed()));
      onView(withText(com.example.mathmodule.R.string.math_calculator_collapse)).perform(click());
      main(() -> resumed().finish());
      start(com.brightpath.science.presentation.ScienceMainActivity.class);
      main(
          () ->
              assertNull(
                  resumed()
                      .findViewById(android.R.id.content)
                      .findViewWithTag(com.example.mathmodule.calculator.MathCalculator.TAG)));
      main(() -> resumed().finish());
    } finally {
      prefs.edit().putBoolean("hidden", hidden).putString("expression", expression).apply();
    }
  }

  @Test
  public void circuitUpdatesValuesAndSwitchState() {
    start(com.brightpath.science.presentation.CircuitDiagramActivity.class);
    main(
        () -> {
          ((EditText) resumed().findViewById(com.brightpath.science.R.id.etCircuitVoltage))
              .setText("6");
          ((EditText) resumed().findViewById(com.brightpath.science.R.id.etCircuitR1))
              .setText("10");
          ((EditText) resumed().findViewById(com.brightpath.science.R.id.etCircuitR2))
              .setText("20");
        });
    main(
        () -> {
          String text =
              ((TextView) resumed().findViewById(com.brightpath.science.R.id.tvCircuitResult))
                  .getText()
                  .toString();
          assertTrue(text.contains("30.000") || text.contains("30,000"));
          ((CheckBox) resumed().findViewById(com.brightpath.science.R.id.cbCircuitSwitch))
              .setChecked(false);
        });
    main(
        () -> {
          String text =
              ((TextView) resumed().findViewById(com.brightpath.science.R.id.tvCircuitResult))
                  .getText()
                  .toString();
          assertTrue(text.contains("0.000") || text.contains("0,000"));
          ((Spinner) resumed().findViewById(com.brightpath.science.R.id.spCircuitType))
              .setSelection(1);
          ((CheckBox) resumed().findViewById(com.brightpath.science.R.id.cbCircuitSwitch))
              .setChecked(true);
        });
    main(
        () -> {
          String text =
              ((TextView) resumed().findViewById(com.brightpath.science.R.id.tvCircuitResult))
                  .getText()
                  .toString();
          assertTrue(text.contains("6.667") || text.contains("6,667"));
        });
    capture("circuit");
    main(() -> resumed().finish());
  }

  @Test
  public void languageThemeAndLoginDraftSurviveAcrossAllSubjectsAndLogout() {
    Context c = ins.getTargetContext();
    String oldLanguage = LocaleHelper.getLanguage(c);
    boolean oldNight = ThemeHelper.isDarkMode(c);
    DebugAccounts.seed(c);
    try {
      for (String language : new String[] {"vi", "en"})
        for (boolean night : new boolean[] {false, true}) {
          start(com.example.asm_final.activities.MainActivity.class);
          main(
              () -> {
                ((EditText) resumed().findViewById(R.id.edtLoginUsername)).setText("draft_login");
                LocaleHelper.setLocale(c, language);
                ThemeHelper.setDarkMode(c, night);
              });
          waitFor(
              a ->
                  a instanceof com.example.asm_final.activities.MainActivity
                      && a.getResources()
                          .getConfiguration()
                          .getLocales()
                          .get(0)
                          .getLanguage()
                          .equals(language)
                      && ((a.getResources().getConfiguration().uiMode
                                  & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                              == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                          == night);
          main(
              () -> {
                assertEquals(
                    "draft_login",
                    ((EditText) resumed().findViewById(R.id.edtLoginUsername))
                        .getText()
                        .toString());
                ((EditText) resumed().findViewById(R.id.edtLoginUsername)).setText("hocvien01");
                ((EditText) resumed().findViewById(R.id.edtLoginPassword)).setText("Mentor01!2026");
                resumed().findViewById(R.id.btnLogin).performClick();
              });
          waitFor(a -> a instanceof com.example.asm_final.activities.HomeActivity);
          main(
              () -> {
                ((com.example.asm_final.activities.HomeActivity) resumed())
                    .navigateTo(R.id.nav_category, false);
                ((AppCompatActivity) resumed())
                    .getSupportFragmentManager()
                    .executePendingTransactions();
              });
          int[] cards = {R.id.cardMath, R.id.cardScience, R.id.cardHistory, R.id.cardIT};
          for (int id : cards) {
            main(() -> resumed().findViewById(id).performClick());
            waitFor(a -> !(a instanceof com.example.asm_final.activities.HomeActivity));
            main(
                () -> {
                  assertEquals(
                      language,
                      resumed()
                          .getResources()
                          .getConfiguration()
                          .getLocales()
                          .get(0)
                          .getLanguage());
                  assertEquals(
                      night,
                      (resumed().getResources().getConfiguration().uiMode
                              & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                          == android.content.res.Configuration.UI_MODE_NIGHT_YES);
                });
            main(() -> resumed().finish());
            waitFor(a -> a instanceof com.example.asm_final.activities.HomeActivity);
          }
          main(() -> ((com.example.asm_final.activities.HomeActivity) resumed()).logout());
          waitFor(a -> a instanceof com.example.asm_final.activities.MainActivity);
          assertEquals(language, LocaleHelper.getLanguage(c));
          assertEquals(night, ThemeHelper.isDarkMode(c));
          main(() -> resumed().finish());
        }
    } finally {
      main(
          () -> {
            LocaleHelper.setLocale(c, oldLanguage);
            ThemeHelper.setDarkMode(c, oldNight);
          });
    }
  }

  @Test
  public void unsavedProfileNameSurvivesNightModeRecreation() {
    Context c = ins.getTargetContext();
    boolean old = ThemeHelper.isDarkMode(c);
    try {
      login();
      main(
          () -> {
            ((com.example.asm_final.activities.HomeActivity) resumed())
                .navigateTo(R.id.nav_edit_profile, false);
            ((AppCompatActivity) resumed())
                .getSupportFragmentManager()
                .executePendingTransactions();
          });
      waitFor(
          a ->
              a.findViewById(R.id.btnUpdateProfile) != null
                  && a.findViewById(R.id.btnUpdateProfile).isEnabled());
      main(
          () -> {
            ((EditText) resumed().findViewById(R.id.edtProfileFullName)).setText("Unsaved draft");
            ThemeHelper.setDarkMode(c, !old);
          });
      waitFor(
          a ->
              a instanceof com.example.asm_final.activities.HomeActivity
                  && a.findViewById(R.id.edtProfileFullName) != null
                  && a.findViewById(R.id.btnUpdateProfile).isEnabled());
      main(
          () ->
              assertEquals(
                  "Unsaved draft",
                  ((EditText) resumed().findViewById(R.id.edtProfileFullName))
                      .getText()
                      .toString()));
      main(() -> ((com.example.asm_final.activities.HomeActivity) resumed()).logout());
      waitFor(a -> a instanceof com.example.asm_final.activities.MainActivity);
      main(() -> resumed().finish());
    } finally {
      main(() -> ThemeHelper.setDarkMode(c, old));
    }
  }
}
