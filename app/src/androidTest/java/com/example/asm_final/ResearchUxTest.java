package com.example.asm_final;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import com.example.asm_final.activities.*;
import com.example.asm_final.utils.ThemeHelper;
import com.google.android.material.textfield.TextInputLayout;
import java.util.*;
import java.util.function.Predicate;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ResearchUxTest {
  private final Instrumentation ins = InstrumentationRegistry.getInstrumentation();
  private final Context context = ins.getTargetContext();
  private String username;
  private int userId, originalUser;
  private boolean originalNight;
  private Map<String, ?> calculatorState;
  private Activity foreground;

  private Activity resumed() {
    Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    if (foreground != null && activities.contains(foreground) && !foreground.isFinishing() && !foreground.isChangingConfigurations()) return foreground;
    if (foreground != null) for (Activity activity : activities) {
      if (activity.getClass() == foreground.getClass() && !activity.isFinishing()) { foreground = activity; return activity; }
    }
    for (Activity activity : activities) if (activity.hasWindowFocus()) return activity;
    return activities.iterator().next();
  }

  private void main(Runnable action) {
    ins.runOnMainSync(action);
    ins.waitForIdleSync();
  }

  private void start(Class<?> type) {
    foreground = ins.startActivitySync(
        new Intent(context, type)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("USERNAME", username));
    waitFor(a -> a == foreground && a.hasWindowFocus());
  }

  private void waitFor(Predicate<Activity> predicate) {
    long end = android.os.SystemClock.uptimeMillis() + 15000;
    while (android.os.SystemClock.uptimeMillis() < end) {
      boolean[] ready = {false};
      ins.runOnMainSync(
          () -> {
            Collection<Activity> a =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            ready[0] = a.stream().anyMatch(predicate);
          });
      if (ready[0]) return;
      android.os.SystemClock.sleep(50);
    }
    fail("UI not ready");
  }

  @Before
  public void prepare() {
    originalUser = com.example.mentor.data.ActiveUser.get(context);
    originalNight = ThemeHelper.isDarkMode(context);
    calculatorState = new HashMap<>(context.getSharedPreferences("math_calculator", 0).getAll());
    context.getSharedPreferences("math_calculator", 0).edit().clear().apply();
    try (com.example.mathmodule.databases.SqliteDbHelper helper =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      helper.getWritableDatabase();
    }
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      helper.getWritableDatabase();
    }
    username = "ux_" + System.nanoTime();
    AppServices services = ((AiMentorApplication) context.getApplicationContext()).services();
    assertTrue(
        services.registration().register(username, "Mentor01!2026", "ux@gmail.com", "0901234567")
            > 0);
    userId = services.accountLookup().userId(username);
    com.example.mentor.data.ActiveUser.set(context, userId);
  }

  @After
  public void cleanup() {
    main(
        () -> {
          for (Stage stage :
              new Stage[] {Stage.RESUMED, Stage.STARTED, Stage.STOPPED, Stage.CREATED})
            for (Activity a :
                new ArrayList<>(
                    ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage)))
              a.finish();
          ThemeHelper.setDarkMode(context, originalNight);
        });
    try (com.example.asm_final.activities.database.DatabaseHelper helper =
        new com.example.asm_final.activities.database.DatabaseHelper(context)) {
      android.database.sqlite.SQLiteDatabase db = helper.getWritableDatabase();
      db.execSQL("DROP TRIGGER IF EXISTS ux_fail_note_insert");
      db.execSQL("DROP TRIGGER IF EXISTS ux_fail_note_update");
      db.delete("programming_sessions", "user_id=?", new String[] {Integer.toString(userId)});
      db.delete("programming_notes", "user_id=?", new String[] {Integer.toString(userId)});
      db.delete(
          "programming_notes", "user_id=?", new String[] {Integer.toString(userId + 100000000)});
      db.delete("math_history", "user_id=?", new String[] {Integer.toString(userId)});
      db.delete("users", "id=?", new String[] {Integer.toString(userId)});
    }
    com.example.mentor.data.ActiveUser.set(context, originalUser);
    SharedPreferences.Editor editor =
        context.getSharedPreferences("math_calculator", 0).edit().clear();
    for (Map.Entry<String, ?> item : calculatorState.entrySet()) {
      Object value = item.getValue();
      if (value instanceof String) editor.putString(item.getKey(), (String) value);
      else if (value instanceof Boolean) editor.putBoolean(item.getKey(), (Boolean) value);
      else if (value instanceof Float) editor.putFloat(item.getKey(), (Float) value);
    }
    editor.apply();
  }

  private TextInputLayout fieldContainer(EditText field) {
    ViewParent parent = field.getParent();
    while (!(parent instanceof TextInputLayout)) parent = parent.getParent();
    return (TextInputLayout) parent;
  }

  private void capture(String name) {
    try {
      ins.getUiAutomation().waitForIdle(250, 3000);
      android.graphics.Bitmap bitmap = ins.getUiAutomation().takeScreenshot();
      java.io.File folder = context.getExternalFilesDir("verified-ui");
      folder.mkdirs();
      try (java.io.FileOutputStream out =
          new java.io.FileOutputStream(new java.io.File(folder, name + ".png"))) {
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
      } finally {
        bitmap.recycle();
      }
    } catch (Exception error) {
      throw new AssertionError(error);
    }
  }

  @Test
  public void loginErrorsAreInlineAndClearWithoutLosingDraft() {
    start(MainActivity.class);
    main(
        () -> {
          EditText name = resumed().findViewById(R.id.edtLoginUsername);
          assertNull(fieldContainer(name).getError());
          resumed().findViewById(R.id.btnLogin).performClick();
          assertNotNull(fieldContainer(name).getError());
          assertTrue(name.isFocused());
          name.setText("draft_user");
          assertNull(fieldContainer(name).getError());
          assertNotNull(
              fieldContainer((EditText) resumed().findViewById(R.id.edtLoginPassword)).getError());
        });
    capture("ux-login-validation");
  }

  @Test
  public void registrationShowsGmailConstraintBeforeSubmissionAndTargetsInvalidField() {
    start(RegisterActivity.class);
    main(
        () -> {
          EditText email = resumed().findViewById(R.id.edtRegisterEmail);
          assertNotNull(fieldContainer(email).getHelperText());
          assertNull(fieldContainer(email).getError());
          ((EditText) resumed().findViewById(R.id.edtRegisterUsername)).setText("draft_user");
          email.setText("learner@example.org");
          ((EditText) resumed().findViewById(R.id.edtRegisterPhone)).setText("0901234567");
          ((EditText) resumed().findViewById(R.id.edtRegisterPassword)).setText("Mentor01!2026");
          ((EditText) resumed().findViewById(R.id.edtRegisterConfirmPassword))
              .setText("Mentor01!2026");
          resumed().findViewById(R.id.btnRegister).performClick();
          assertNotNull(fieldContainer(email).getError());
          assertTrue(email.isFocused());
          assertEquals("", ((TextView)resumed().findViewById(R.id.tvRegisterMessage)).getText().toString());
          assertEquals(
              "draft_user",
              ((EditText) resumed().findViewById(R.id.edtRegisterUsername)).getText().toString());
        });
    capture("ux-register-validation");
  }

  @Test
  public void newLearnerGetsStartActionAndStatsRemainAvailableAfterThemeChange() {
    start(HomeActivity.class);
    waitFor(
        a ->
            a.findViewById(R.id.text_recent_empty) != null
                && a.findViewById(R.id.text_recent_empty).getVisibility() == View.VISIBLE);
    capture("ux-home-new-learner");
    main(
        () -> {
          assertEquals(
              View.GONE, resumed().findViewById(R.id.layout_detailed_stats).getVisibility());
          resumed().findViewById(R.id.button_start_learning).performClick();
          ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
          assertNotNull(resumed().findViewById(R.id.cardMath));
          ((HomeActivity) resumed()).navigateTo(R.id.nav_home, false);
          ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
          resumed().findViewById(R.id.button_stats_toggle).performClick();
          ThemeHelper.setDarkMode(context, !originalNight);
        });
    waitFor(
        a ->
            a instanceof HomeActivity
                && a.findViewById(R.id.layout_detailed_stats) != null
                && (a.getResources().getConfiguration().uiMode
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == (!originalNight
                        ? android.content.res.Configuration.UI_MODE_NIGHT_YES
                        : android.content.res.Configuration.UI_MODE_NIGHT_NO));
    main(
        () ->
            assertEquals(
                View.VISIBLE, resumed().findViewById(R.id.layout_detailed_stats).getVisibility()));
  }

  @Test
  public void returningLearnerReopensActualRecentSubjectWithCorrectAccount() {
    try (com.example.mathmodule.databases.SqliteDbHelper helper =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      assertTrue(helper.insertHistoryForUser(userId, "UX saved question", "UX saved answer") > 0);
    }
    start(HomeActivity.class);
    waitFor(
        a ->
            a.findViewById(R.id.button_start_learning) != null
                && ((TextView) a.findViewById(R.id.button_start_learning))
                    .getText()
                    .toString()
                    .contains(context.getString(R.string.cat_math)));
    capture("ux-home-returning-learner");
    main(() -> resumed().findViewById(R.id.button_start_learning).performClick());
    waitFor(a -> a instanceof com.example.mathmodule.MathDashboardActivity);
    assertEquals(userId, com.example.mathmodule.util.MathUserSession.getUserId(context));
  }

  @Test
  public void calculatorResultStaysVisibleWhenKeypadScrolls() {
    start(com.example.mathmodule.MathDashboardActivity.class);
    main(() -> com.example.mathmodule.calculator.MathCalculator.show(resumed()));
    for (String key : new String[] {"6", "×", "7", "="})
      onView(withText(key)).perform(scrollTo(), click());
    onView(withTagValue(is((Object) "mentor_calculator_keypad"))).perform(swipeUp());
    onView(withText("42")).check(matches(isDisplayed()));
    capture("ux-calculator-pinned-result");
    onView(withText(com.example.mathmodule.R.string.math_calculator_collapse)).perform(click());
  }

  private Activity startNote(int noteId, String title, String content) {
    foreground = ins.startActivitySync(
        new Intent(context, com.example.modulelaptrinh.NoteEditorActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID, userId)
            .putExtra("NOTE_ID", noteId)
            .putExtra("NOTE_TITLE", title)
            .putExtra("NOTE_CONTENT", content));
    waitFor(a -> a == foreground && a.hasWindowFocus());
    return foreground;
  }

  private void editNote(String title, String content) {
    main(
        () -> {
          ((EditText) resumed().findViewById(com.example.modulelaptrinh.R.id.et_note_title))
              .setText(title);
          ((EditText) resumed().findViewById(com.example.modulelaptrinh.R.id.et_note_content))
              .setText(content);
        });
  }

  private void trigger(String operation) {
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      helper
          .getWritableDatabase()
          .execSQL(
              "CREATE TRIGGER ux_fail_note_"
                  + operation.toLowerCase(java.util.Locale.ROOT)
                  + " BEFORE "
                  + operation
                  + " ON programming_notes WHEN NEW.user_id="
                  + userId
                  + " BEGIN SELECT RAISE(ABORT,'UX simulated write failure'); END");
    }
  }

  @Test
  public void offlineChatRetainsTypedQuestionInEachOnlineOnlySubject() {
    org.junit.Assume.assumeFalse(
        new com.example.mathmodule.api.MathGeminiApiClient().isConfigured());
    org.junit.Assume.assumeFalse(
        new com.example.historymodule.api.HistoryAiService().isConfigured());
    org.junit.Assume.assumeFalse(
        new com.example.modulelaptrinh.utils.GeminiApiClient(
                com.example.modulelaptrinh.utils.GeminiApiClient.Usage.CHAT)
            .isConfigured());
    Class<?>[] types = {
      com.example.mathmodule.MathChatActivity.class,
      com.example.historymodule.HistoryChatActivity.class,
      com.example.modulelaptrinh.ProgrammingChatActivity.class
    };
    int[] inputs = {
      com.example.mathmodule.R.id.math_edt_question,
      com.example.historymodule.R.id.message_edit_text,
      com.example.modulelaptrinh.R.id.et_chat_input
    };
    int[] sends = {
      com.example.mathmodule.R.id.math_btn_send,
      com.example.historymodule.R.id.send_btn,
      com.example.modulelaptrinh.R.id.iv_send
    };
    int[] lists = {
      com.example.mathmodule.R.id.math_recycler_view,
      com.example.historymodule.R.id.recycler_view,
      com.example.modulelaptrinh.R.id.rv_chat_detail
    };
    for (int index = 0; index < types.length; index++) {
      final int i = index;
      foreground = ins.startActivitySync(
          new Intent(context, types[i])
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              .putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID, userId));
      waitFor(a -> a == foreground && a.hasWindowFocus());
      final int[] messageCount = {0};
      main(() -> messageCount[0] = ((androidx.recyclerview.widget.RecyclerView)resumed().findViewById(lists[i])).getAdapter().getItemCount());
      onView(withId(inputs[i])).perform(click(), replaceText("Keep this question"));
      if (Boolean.parseBoolean(InstrumentationRegistry.getArguments().getString("mentorRequireIme", "false"))) {
        waitFor(a -> {
          androidx.core.view.WindowInsetsCompat insets = androidx.core.view.ViewCompat.getRootWindowInsets(a.getWindow().getDecorView());
          return insets != null && insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime());
        });
      }
      onView(withId(sends[i])).perform(click());
      main(
          () -> {
            EditText input = resumed().findViewById(inputs[i]);
            androidx.recyclerview.widget.RecyclerView list = resumed().findViewById(lists[i]);
            int count = messageCount[0];
            assertEquals("Keep this question", input.getText().toString());
            assertEquals(count, list.getAdapter().getItemCount());
            assertEquals(
                View.VISIBLE,
                resumed()
                    .findViewById(com.example.mentor.ui.R.id.mentor_ai_availability)
                    .getVisibility());
          });
      capture("ux-offline-chat-" + i);
      main(() -> resumed().finish());
    }
  }

  @Test
  public void noteSaveAndBackPreserveDraftAcrossRecreation() {
    Activity first = startNote(-1, "", "");
    editNote("Lesson", "Version one");
    main(() -> first.findViewById(com.example.modulelaptrinh.R.id.btn_save).performClick());
    int id;
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      java.util.List<com.example.modulelaptrinh.database.ProgrammingNote> notes =
          helper.getAllNotes(userId);
      assertEquals(1, notes.size());
      id = notes.get(0).getId();
      assertEquals("Version one", notes.get(0).getContent());
    }
    Activity editing = startNote(id, "Lesson", "Version one");
    editNote("Lesson", "Unsaved draft");
    main(editing::recreate);
    waitFor(a -> a != editing && a instanceof com.example.modulelaptrinh.NoteEditorActivity);
    main(
        () -> {
          assertEquals(
              "Unsaved draft",
              ((EditText) resumed().findViewById(com.example.modulelaptrinh.R.id.et_note_content))
                  .getText()
                  .toString());
          ((AppCompatActivity) resumed()).getOnBackPressedDispatcher().onBackPressed();
        });
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      java.util.List<com.example.modulelaptrinh.database.ProgrammingNote> notes =
          helper.getAllNotes(userId);
      assertEquals(1, notes.size());
      assertEquals(id, notes.get(0).getId());
      assertEquals("Unsaved draft", notes.get(0).getContent());
    }
  }

  @Test
  public void failedNoteInsertStaysOpenAndKeepsDraft() {
    trigger("INSERT");
    Activity editing = startNote(-1, "", "");
    editNote("Important", "Do not lose this");
    main(() -> editing.findViewById(com.example.modulelaptrinh.R.id.btn_save).performClick());
    main(
        () -> {
          assertFalse(editing.isFinishing());
          assertEquals(
              "Do not lose this",
              ((EditText) editing.findViewById(com.example.modulelaptrinh.R.id.et_note_content))
                  .getText()
                  .toString());
          assertFalse(
              ((TextView) editing.findViewById(com.example.modulelaptrinh.R.id.note_save_status))
                  .getText()
                  .toString()
                  .isEmpty());
        });
    capture("ux-note-write-failed");
    main(() -> ((AppCompatActivity) editing).getOnBackPressedDispatcher().onBackPressed());
    capture("ux-note-back-recovery");
    onView(withText(com.example.modulelaptrinh.R.string.note_keep_editing)).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog()).perform(click());
    main(() -> assertFalse(editing.isFinishing()));
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      assertTrue(helper.getAllNotes(userId).isEmpty());
      helper.getWritableDatabase().execSQL("DROP TRIGGER ux_fail_note_insert");
    }
    main(() -> editing.findViewById(com.example.modulelaptrinh.R.id.btn_save).performClick());
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      assertEquals("Do not lose this", helper.getAllNotes(userId).get(0).getContent());
    }
  }

  @Test
  public void failedNoteUpdateKeepsStoredDataAndEditorOpen() {
    int id;
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      id = (int) helper.insertNote(userId, "Lesson", "Stored version");
    }
    trigger("UPDATE");
    Activity editing = startNote(id, "Lesson", "Stored version");
    editNote("Lesson", "Retry later");
    main(() -> editing.findViewById(com.example.modulelaptrinh.R.id.btn_save).performClick());
    main(() -> assertFalse(editing.isFinishing()));
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      assertEquals("Stored version", helper.getAllNotes(userId).get(0).getContent());
    }
  }

  @Test
  public void noteCannotOverwriteAnotherAccountsRecord() {
    int otherId = userId + 100000000, noteId;
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      noteId = (int) helper.insertNote(otherId, "Other user", "Keep private");
    }
    Activity editing = startNote(noteId, "Other user", "Keep private");
    editNote("Wrong owner", "Must not save");
    main(() -> editing.findViewById(com.example.modulelaptrinh.R.id.btn_save).performClick());
    main(() -> assertFalse(editing.isFinishing()));
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      assertEquals("Keep private", helper.getAllNotes(otherId).get(0).getContent());
      assertTrue(helper.getAllNotes(userId).isEmpty());
    }
  }

  @Test
  public void calculatorRemainsUsableInLandscapeWithPinnedResult() {
    start(com.example.mathmodule.MathDashboardActivity.class);
    main(
        () ->
            resumed()
                .setRequestedOrientation(
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
    waitFor(
        a ->
            a instanceof com.example.mathmodule.MathDashboardActivity
                && a.getResources().getConfiguration().orientation
                    == android.content.res.Configuration.ORIENTATION_LANDSCAPE
              && a.hasWindowFocus() && !a.isChangingConfigurations());
    main(() -> com.example.mathmodule.calculator.MathCalculator.show(resumed()));
    for (String key : new String[] {"6", "×", "7", "="})
      onView(withText(key)).perform(scrollTo(), click());
    onView(withText("42")).check(matches(isDisplayed()));
    capture("ux-calculator-landscape");
    onView(withText(com.example.mathmodule.R.string.math_calculator_collapse)).perform(click());
  }
}
