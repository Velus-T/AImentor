package com.example.asm_final;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.click;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.*;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import com.example.mentor.data.*;
import com.example.mentor.ui.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SharedQuizBankTest {
  private final Instrumentation ins = InstrumentationRegistry.getInstrumentation();
  private final Context context = ins.getTargetContext();
  private int owner, previous;
  private String originalLocale;
  private boolean originalNight;
  private Activity activity;

  private List<QuizCard> cards(boolean math) {
    ArrayList<QuizCard> cards = new ArrayList<>();
    for (String difficulty : new String[] {"EASY", "MEDIUM", "HARD"})
      cards.add(
          new QuizCard(
              math
                  ? "QA fixture " + difficulty + " $\\frac{1}{2}+\\frac{1}{2}$"
                  : "QA fixture " + difficulty,
              Arrays.asList("1", "2", "3", "4"),
              QuizCard.letters(4),
              "A",
              "QA explanation",
              difficulty,
              math));
    return cards;
  }

  private void main(Runnable action) {
    ins.runOnMainSync(action);
    ins.waitForIdleSync();
  }

  @Before
  public void prepare() {
    previous = ActiveUser.get(context);
    originalLocale = com.example.asm_final.utils.LocaleHelper.getLanguage(context);
    originalNight = com.example.asm_final.utils.ThemeHelper.isDarkMode(context);
    main(
        () -> {
          com.example.asm_final.utils.LocaleHelper.setLocale(
              context, InstrumentationRegistry.getArguments().getString("mentorLocale", "vi"));
          com.example.asm_final.utils.ThemeHelper.setDarkMode(
              context,
              Boolean.parseBoolean(
                  InstrumentationRegistry.getArguments().getString("mentorNight", "false")));
        });
    AppServices services = ((AiMentorApplication) context.getApplicationContext()).services();
    String name = "quiz_qa_" + System.nanoTime();
    assertTrue(
        services.registration().register(name, "Mentor01!2026", "qa@gmail.com", "0901234567") > 0);
    owner = services.accountLookup().userId(name);
    ActiveUser.set(context, owner);
    try (com.example.mathmodule.databases.SqliteDbHelper db =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      db.getWritableDatabase();
    }
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper db =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      db.getWritableDatabase();
    }
    try (com.example.historymodule.databases.SqliteDbHelper db =
        new com.example.historymodule.databases.SqliteDbHelper(context)) {
      db.getWritableDatabase();
    }
    com.brightpath.science.di.ScienceModule.get(context).quizAttemptRepository().getTotalExp(owner);
    try (QuizBank db = new QuizBank(context)) {
      db.getWritableDatabase();
    }
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
        });
    if (owner > 0) {
    try (QuizBank bank = new QuizBank(context)) {
      android.database.sqlite.SQLiteDatabase db = bank.getWritableDatabase();
      db.execSQL("DROP TRIGGER IF EXISTS qa_bank_fail");
      db.execSQL("DROP TRIGGER IF EXISTS qa_progress_fail");
      db.delete(
          "mentor_quiz_bank_questions",
          "set_id IN (SELECT id FROM mentor_quiz_bank_sets WHERE user_id=?)",
          new String[] {String.valueOf(owner)});
      db.delete("mentor_quiz_bank_sets", "user_id=?", new String[] {String.valueOf(owner)});
      for (String table :
          new String[] {
            "history_quiz_attempts",
            "programming_quiz_attempts",
            "programming_user_progress",
            "science_quiz_attempts",
            "math_quiz_attempts",
            "math_user_profile",
            "history_messages",
            "math_history",
            "science_history",
            "programming_sessions"
          }) db.delete(table, "user_id=?", new String[] {String.valueOf(owner)});
      db.delete(
          "math_quiz_questions",
          "set_id IN (SELECT id FROM math_quiz_sets WHERE owner_id=?)",
          new String[] {String.valueOf(owner)});
      db.delete("math_quiz_sets", "owner_id=?", new String[] {String.valueOf(owner)});
      db.delete("users", "id=?", new String[] {String.valueOf(owner)});
    }
    }
    ActiveUser.set(context, previous);
    main(
        () -> {
          com.example.asm_final.utils.LocaleHelper.setLocale(context, originalLocale);
          com.example.asm_final.utils.ThemeHelper.setDarkMode(context, originalNight);
        });
  }

  @Test
  public void bankPersistsClassifiesDeduplicatesAndIsolatesOwners() {
    long id;
    try (QuizBank bank = new QuizBank(context)) {
      id =
          bank.save(
              owner, "history", "Vietnam", "MIDDLE_SCHOOL", "QA topic", "GEMINI", cards(false));
      assertEquals(
          id,
          bank.save(
              owner, "history", "Vietnam", "MIDDLE_SCHOOL", "QA topic", "GEMINI", cards(false)));
      assertEquals(1, bank.list(owner, "history").size());
      assertEquals("Vietnam", bank.list(owner, "history").get(0).scope);
      assertEquals(3, bank.list(owner, "history").get(0).count);
      assertTrue(bank.list(owner + 1000000, "history").isEmpty());
      assertTrue(bank.questions(owner + 1000000, id).isEmpty());
    }
    try (QuizBank reopened = new QuizBank(context)) {
      assertEquals("HARD", reopened.questions(owner, id).get(2).difficulty);
      assertEquals("QA explanation", reopened.questions(owner, id).get(0).explanation);
      assertEquals("A", reopened.questions(owner, id).get(0).answer);
    }
  }

  @Test
  public void failedQuestionInsertRollsBackEntireBankSet() {
    try (QuizBank bank = new QuizBank(context)) {
      bank.getWritableDatabase()
          .execSQL(
              "CREATE TRIGGER qa_bank_fail BEFORE INSERT ON mentor_quiz_bank_questions BEGIN SELECT"
                  + " RAISE(ABORT,'QA failure'); END");
      try {
        bank.save(
            owner, "history", "Vietnam", "MIDDLE_SCHOOL", "QA rollback", "GEMINI", cards(false));
        fail();
      } catch (android.database.SQLException expected) {
      }
      assertTrue(bank.list(owner, "history").isEmpty());
    }
  }

  private void launch(Class<?> type, Intent intent) {
    activity =
        ins.startActivitySync(
            intent.setClass(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    long until = android.os.SystemClock.uptimeMillis() + 15000;
    while (android.os.SystemClock.uptimeMillis() < until) {
      final boolean[] focused = {false};
      main(() -> focused[0] = activity.hasWindowFocus() && !activity.isChangingConfigurations());
      if (focused[0]) return;
      android.os.SystemClock.sleep(50);
    }
    fail("Activity did not become focused");
  }

  private void launchHistory() {
    launch(com.example.historymodule.HistoryQuizActivity.class, new Intent());
  }

  private QuizFormView form() {
    return activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
  }

  private void answerAll() {
    for (int i = 0; i < form().getChildCount(); i++)
      if (form().getChildAt(i) instanceof QuizQuestionCard)
        ((QuizQuestionCard) form().getChildAt(i)).restore("A");
  }

  @Test
  public void commonFormKeepsAnswersOnSaveFailureAndNeverDoubleAwards() {
    launchHistory();
    final int[] saves = {0};
    final Bundle[] state = {null};
    main(
        () -> {
          form()
              .bind(
                  cards(false),
                  (correct, count, exp, done) -> {
                    saves[0]++;
                    assertEquals(3, correct);
                    assertEquals(35, exp);
                    done.complete(saves[0] > 1, 35);
                  });
          form().submit();
          assertEquals(0, saves[0]);
          answerAll();
          form().submit();
          assertFalse(form().submitted());
          assertEquals("A", form().answers().get(0));
          form().submit();
          form().submit();
          assertTrue(form().submitted());
          assertEquals(2, saves[0]);
          state[0] = form().state();
          assertTrue(
              form().restore(state[0], (c, n, e, d) -> fail("Restoring must not save again")));
          assertTrue(form().submitted());
          form().submit();
        });
  }

  private void savedQuiz(String subject, String scope, String level, boolean math) {
    try (QuizBank bank = new QuizBank(context)) {
      ArrayList<QuizCard> offline = new ArrayList<>();
      for (int i = 0; i < 15; i++) {
        QuizCard fixture = cards(math).get(i % 3);
        offline.add(
            new QuizCard(
                fixture.prompt + " " + i,
                fixture.options,
                fixture.optionValues,
                fixture.answer,
                fixture.explanation,
                fixture.difficulty,
                math));
      }
      bank.save(owner, subject, scope, level, "QA offline " + subject, "GEMINI", offline);
    }
  }

  private void chooseBank(QuizSetupView setup) {
    main(
        () -> {
          assertEquals(3, setup.count.getCount());
          assertEquals("5", setup.count.getItemAtPosition(0));
          assertEquals("10", setup.count.getItemAtPosition(1));
          assertEquals("15", setup.count.getItemAtPosition(2));
          setup.bank.performClick();
        });
    onData(anything())
        .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
        .inAdapterView(androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(ListView.class))
        .atPosition(0)
        .perform(click());
    ins.waitForIdleSync();
  }

  private void submitAndCheck() {
    main(
        () -> {
          assertEquals(android.view.View.VISIBLE, form().getVisibility());
          answerAll();
          form().submit();
          assertTrue(form().submitted());
        });
  }

  @Test
  public void historyCanPracticeSavedQuestionsWithoutCallingAI() {
    savedQuiz("history", "Lịch sử Việt Nam", "Trung học cơ sở", false);
    launchHistory();
    chooseBank(activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup));
    submitAndCheck();
    try (com.example.historymodule.databases.SqliteDbHelper db =
        new com.example.historymodule.databases.SqliteDbHelper(context)) {
      assertEquals(175, db.getTotalQuizExp(owner));
    }
  }

  @Test
  public void scienceCanPracticeSavedQuestionsWithoutCallingAI() {
    savedQuiz("science", "physics", "MIDDLE_SCHOOL", false);
    launch(com.brightpath.science.presentation.ScienceQuizActivity.class, new Intent());
    chooseBank(activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup));
    submitAndCheck();
    assertEquals(
        175,
        com.brightpath.science.di.ScienceModule.get(context)
            .quizAttemptRepository()
            .getTotalExp(owner));
  }

  @Test
  public void programmingAttemptAndExpRollBackTogether() {
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      android.database.sqlite.SQLiteDatabase db = helper.getWritableDatabase();
      db.execSQL(
          "CREATE TRIGGER qa_progress_fail BEFORE INSERT ON programming_user_progress WHEN"
              + " NEW.user_id="
              + owner
              + " BEGIN SELECT RAISE(ABORT,'QA failure'); END");
      assertEquals(
          -1, helper.saveQuizResult(owner, "Lập trình", "Trung học cơ sở", "QA topic", 3, 3, 35));
      try (android.database.Cursor c =
          db.rawQuery(
              "SELECT COUNT(*) FROM programming_quiz_attempts WHERE user_id=?",
              new String[] {String.valueOf(owner)})) {
        c.moveToFirst();
        assertEquals(0, c.getInt(0));
      }
      assertEquals(0, helper.getTotalExpAllSubjects(owner));
    }
  }

  private void selectQuizTab(int navigationId, int tabId) {
    main(
        () -> {
          ((com.google.android.material.bottomnavigation.BottomNavigationView)
                  activity.findViewById(navigationId))
              .setSelectedItemId(tabId);
          ((androidx.appcompat.app.AppCompatActivity) activity)
              .getSupportFragmentManager()
              .executePendingTransactions();
        });
  }

  @Test
  public void programmingCanPracticeFifteenSavedQuestionsWithoutCallingAI() {
    savedQuiz("programming", "Lập trình", "Trung học cơ sở", false);
    launch(
        com.example.modulelaptrinh.MainActivity.class,
        new Intent().putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID, owner));
    selectQuizTab(
        com.example.modulelaptrinh.R.id.bottom_nav, com.example.modulelaptrinh.R.id.nav_quiz);
    chooseBank(activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup));
    submitAndCheck();
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper db =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      assertEquals(175, db.getTotalExpAllSubjects(owner));
    }
  }

  @Test
  public void mathCanPracticeFifteenSavedQuestionsWithoutCallingAI() {
    savedQuiz("math", "math", "MIDDLE_SCHOOL", true);
    launch(com.example.mathmodule.MathDashboardActivity.class, new Intent());
    selectQuizTab(
        com.example.mathmodule.R.id.bottom_navigation, com.example.mathmodule.R.id.nav_quiz);
    chooseBank(activity.findViewById(com.example.mathmodule.R.id.math_quiz_setup));
    long until = android.os.SystemClock.uptimeMillis() + 15000;
    boolean ready = false;
    while (android.os.SystemClock.uptimeMillis() < until) {
      final boolean[] found = {false};
      main(
          () -> {
            for (Activity a :
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))
              if (a instanceof com.example.mathmodule.QuizPlayActivity && a.hasWindowFocus()) {
                activity = a;
                found[0] = true;
              }
          });
      if (found[0]) {
        ready = true;
        break;
      }
      android.os.SystemClock.sleep(50);
    }
    assertTrue("Math player did not open", ready);
    verifyOfflineFormula();
    submitAndCheck();
    try (com.example.mathmodule.databases.SqliteDbHelper db =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      assertEquals(175, db.getUserExp());
    }
  }

  private void awaitRecreated(Activity old) {
    long until = android.os.SystemClock.uptimeMillis() + 15000;
    while (android.os.SystemClock.uptimeMillis() < until) {
      final boolean[] found = {false};
      main(
          () -> {
            for (Activity a :
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))
              if (a != old
                  && a.getClass() == old.getClass()
                  && a.hasWindowFocus()
                  && !a.isChangingConfigurations()) {
                activity = a;
                found[0] = true;
              }
          });
      if (found[0]) return;
      android.os.SystemClock.sleep(50);
    }
    fail("Recreated activity did not become ready");
  }

  private void capture(String name) {
    try {
      ins.getUiAutomation().waitForIdle(250, 3000);
    } catch (java.util.concurrent.TimeoutException ignored) {
    }
    android.graphics.Bitmap bitmap = ins.getUiAutomation().takeScreenshot();
    assertNotNull(bitmap);
    java.io.File directory = context.getExternalFilesDir("verified-ui");
    directory.mkdirs();
    try (java.io.FileOutputStream out =
        new java.io.FileOutputStream(new java.io.File(directory, name + ".png"))) {
      bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
    } catch (java.io.IOException error) {
      throw new AssertionError(error);
    } finally {
      bitmap.recycle();
    }
  }

  @Test
  public void themeAndLanguageRecreationKeepAnswersAndDoNotAwardTwice() {
    savedQuiz("history", "Lịch sử Việt Nam", "Trung học cơ sở", false);
    launchHistory();
    chooseBank(activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup));
    main(this::answerAll);
    Activity old = activity;
    String language =
        "en".equals(com.example.asm_final.utils.LocaleHelper.getLanguage(context)) ? "vi" : "en";
    main(() -> com.example.asm_final.utils.LocaleHelper.setLocale(context, language));
    awaitRecreated(old);
    main(
        () -> {
          assertEquals(15, form().answers().size());
          assertTrue(form().answers().stream().allMatch("A"::equals));
          QuizSetupView setup = activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup);
          assertEquals(
              activity.getString(com.example.mentor.ui.R.string.mentor_quiz_create),
              setup.create.getText().toString());
          assertEquals(
              activity.getString(com.example.mentor.ui.R.string.mentor_quiz_bank_source),
              setup.status.getText().toString());
        });
    old = activity;
    boolean night = !com.example.asm_final.utils.ThemeHelper.isDarkMode(context);
    main(() -> com.example.asm_final.utils.ThemeHelper.setDarkMode(context, night));
    awaitRecreated(old);
    submitAndCheck();
    capture("quiz-shared-result-theme-language");
    old = activity;
    main(() -> activity.recreate());
    awaitRecreated(old);
    main(
        () -> {
          assertTrue(form().submitted());
          form().submit();
        });
    try (com.example.historymodule.databases.SqliteDbHelper db =
        new com.example.historymodule.databases.SqliteDbHelper(context)) {
      assertEquals(175, db.getTotalQuizExp(owner));
    }
  }

  @Test
  public void bankRetainsMatchingQuestionKindAndAcceptsEquivalentPairOrder() {
    QuizCard question =
        new QuizCard(
            "QA matching",
            Collections.emptyList(),
            Collections.emptyList(),
            "A-2, B-1",
            "QA explanation",
            "HARD",
            false,
            "MATCHING");
    try (QuizBank bank = new QuizBank(context)) {
      long id =
          bank.save(
              owner,
              "science",
              "physics",
              "MIDDLE_SCHOOL",
              "QA matching",
              "GEMINI",
              Collections.singletonList(question));
      QuizCard restored = bank.questions(owner, id).get(0);
      assertEquals("MATCHING", restored.answerType());
      assertTrue(restored.correct("B:1; A:2"));
      assertFalse(restored.correct("A-1,B-2"));
    }
  }

  private void seedHistoryFixtures() {
    long now = System.currentTimeMillis(), old = now - 40L * 24 * 3600000;
    try (com.example.mathmodule.databases.SqliteDbHelper math =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      math.insertHistoryForUser(owner, "QA current topic", "Lực đàn hồi");
      long id = math.insertHistoryForUser(owner, "QA old topic", "QA old answer");
      math.getWritableDatabase()
          .execSQL("UPDATE math_history SET timestamp=? WHERE id=?", new Object[] {old, id});
    }
    try (com.example.historymodule.databases.SqliteDbHelper history =
        new com.example.historymodule.databases.SqliteDbHelper(context)) {
      String current = "qa-history-current-" + owner, previous = "qa-history-old-" + owner;
      history.saveChatMessage(
          owner,
          current,
          com.example.historymodule.HistoryMessage.SENT_BY_ME,
          "QA current topic",
          false);
      history.saveChatMessage(
          owner,
          current,
          com.example.historymodule.HistoryMessage.SENT_BY_BOT,
          "Lực đàn hồi",
          false);
      history.saveChatMessage(
          owner,
          current,
          com.example.historymodule.HistoryMessage.SENT_BY_BOT,
          "QA final message",
          false);
      history.saveChatMessage(
          owner,
          previous,
          com.example.historymodule.HistoryMessage.SENT_BY_ME,
          "QA old topic",
          false);
      history
          .getWritableDatabase()
          .execSQL(
              "UPDATE history_messages SET created_at=? WHERE user_id=? AND conversation_id=?",
              new Object[] {old, owner, previous});
    }
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper helper =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      android.database.sqlite.SQLiteDatabase db = helper.getWritableDatabase();
      for (int i = 0; i < 2; i++) {
        ContentValues session = new ContentValues();
        session.put("user_id", owner);
        session.put("name", i == 0 ? "QA current topic" : "QA old topic");
        session.put("timestamp", i == 0 ? now : old);
        long id = db.insertOrThrow("programming_sessions", null, session);
        for (String text :
            new String[] {
              i == 0 ? "QA current topic" : "QA old topic",
              i == 0 ? "Lực đàn hồi" : "QA old answer",
              "QA final message"
            }) {
          ContentValues message = new ContentValues();
          message.put("session_id", id);
          message.put("content", text);
          message.put("is_user", 1);
          message.put("timestamp", i == 0 ? now : old);
          db.insertOrThrow("programming_messages", null, message);
        }
      }
    }
    com.brightpath.science.domain.repository.ScienceHistoryRepository repo =
        com.brightpath.science.di.ScienceModule.get(context).historyRepository();
    for (int i = 0; i < 2; i++) {
      com.brightpath.science.domain.model.ScienceQuestion question =
          new com.brightpath.science.domain.model.ScienceQuestion(
              owner,
              com.brightpath.science.domain.model.ScienceSubject.PHYSICS,
              com.brightpath.science.domain.model.EducationLevel.MIDDLE_SCHOOL,
              com.brightpath.science.domain.model.ExplanationStyle.SHORT,
              i == 0 ? "QA current topic" : "QA old topic",
              null,
              null,
              null);
      long id = repo.save(question, i == 0 ? "Lực đàn hồi" : "QA old answer");
      if (i == 1)
        try (QuizBank bank = new QuizBank(context)) {
          bank.getWritableDatabase()
              .execSQL(
                  "UPDATE science_history SET created_at=? WHERE id=? AND user_id=?",
                  new Object[] {old, id, owner});
        }
    }
  }

  @Test
  public void fourHistoriesSearchActualContentAndFilterCanonicalEpochDates() {
    seedHistoryFixtures();
    Class<?>[] types = {
      com.example.mathmodule.MathDashboardActivity.class,
      com.example.historymodule.HistoryDashboardActivity.class,
      com.example.modulelaptrinh.MainActivity.class,
      com.brightpath.science.presentation.ScienceMainActivity.class
    };
    int[] navigation = {
      com.example.mathmodule.R.id.bottom_navigation,
      com.example.historymodule.R.id.bottom_navigation,
      com.example.modulelaptrinh.R.id.bottom_nav,
      com.brightpath.science.R.id.scienceBottomNavigation
    };
    int[] tabs = {
      com.example.mathmodule.R.id.nav_history,
      com.example.historymodule.R.id.nav_history,
      com.example.modulelaptrinh.R.id.nav_history,
      com.brightpath.science.R.id.navScienceHistory
    };
    for (int i = 0; i < types.length; i++) {
      launch(
          types[i],
          new Intent().putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID, owner));
      selectQuizTab(navigation[i], tabs[i]);
      main(
          () -> {
            androidx.recyclerview.widget.RecyclerView list =
                activity.findViewById(com.example.mentor.ui.R.id.mentor_history_list);
            assertEquals(2, list.getAdapter().getItemCount());
            EditText search =
                activity.findViewById(com.example.mentor.ui.R.id.mentor_history_search);
            search.setText("dan hoi");
            assertEquals(1, list.getAdapter().getItemCount());
            search.setText("QA missing keyword");
            assertEquals(0, list.getAdapter().getItemCount());
            search.setText("");
            assertEquals(2, list.getAdapter().getItemCount());
            ((Spinner) activity.findViewById(com.example.mentor.ui.R.id.mentor_history_period))
                .setSelection(1);
          });
      ins.waitForIdleSync();
      main(
          () ->
              assertEquals(
                  1,
                  ((androidx.recyclerview.widget.RecyclerView)
                          activity.findViewById(com.example.mentor.ui.R.id.mentor_history_list))
                      .getAdapter()
                      .getItemCount()));
      if(Boolean.parseBoolean(InstrumentationRegistry.getArguments().getString("mentorRequireIme","false")))verifyHistoryKeyboard(navigation[i]);
      capture("history-shared-subject-" + i);
      main(() -> activity.finish());
    }
  }

  private android.webkit.WebView firstWebView(View root) {
    if (root instanceof android.webkit.WebView) return (android.webkit.WebView) root;
    if (root instanceof ViewGroup)
      for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
        android.webkit.WebView found = firstWebView(((ViewGroup) root).getChildAt(i));
        if (found != null) return found;
      }
    return null;
  }

  private void verifyOfflineFormula() {
    java.util.concurrent.atomic.AtomicBoolean rendered =
        new java.util.concurrent.atomic.AtomicBoolean();
    java.util.concurrent.atomic.AtomicReference<String> details =
        new java.util.concurrent.atomic.AtomicReference<>();
    long until = android.os.SystemClock.uptimeMillis() + 20000;
    while (android.os.SystemClock.uptimeMillis() < until) {
      main(
          () -> {
            android.webkit.WebView web = firstWebView(form());
            assertNotNull(web);
            assertTrue(web.getSettings().getBlockNetworkLoads());
            web.evaluateJavascript(
                "JSON.stringify({url:location.href,katex:typeof katex,autorender:typeof"
                    + " renderMathInElement,text:document.body?document.body.innerText:null,scripts:Array.from(document.scripts).map(s=>s.src)})",
                details::set);
            web.evaluateJavascript(
                "typeof katex !== 'undefined' && document.querySelectorAll('.katex').length > 0",
                value -> rendered.set("true".equals(value)));
          });
      if (rendered.get()) return;
      android.os.SystemClock.sleep(100);
    }
    capture("quiz-offline-formula-debug");
    fail("Bundled Math formulas did not render offline: " + details.get());
  }

  private void verifyHistoryKeyboard(int navigationId) {
    onView(androidx.test.espresso.matcher.ViewMatchers.withId(com.example.mentor.ui.R.id.mentor_history_search))
        .perform(androidx.test.espresso.action.ViewActions.click(),androidx.test.espresso.action.ViewActions.replaceText("dan hoi"));
    awaitKeyboard(true);
    main(()->{
      assertEquals(View.GONE,activity.findViewById(navigationId).getVisibility());
      androidx.recyclerview.widget.RecyclerView list=activity.findViewById(com.example.mentor.ui.R.id.mentor_history_list);
      assertTrue("History list must have usable space above the keyboard",list.getHeight()>0);
      assertEquals(1,list.getAdapter().getItemCount());
    });
    onView(androidx.test.espresso.matcher.ViewMatchers.withId(com.example.mentor.ui.R.id.mentor_history_search))
        .perform(androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
    awaitKeyboard(false);
    main(()->assertEquals(View.VISIBLE,activity.findViewById(navigationId).getVisibility()));
  }
  private void awaitKeyboard(boolean visible) {
    long until=android.os.SystemClock.uptimeMillis()+15000;
    while(android.os.SystemClock.uptimeMillis()<until) {
      boolean[] ready={false};main(()->{
        androidx.core.view.WindowInsetsCompat insets=androidx.core.view.ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
        ready[0]=insets!=null&&insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime())==visible;
      });
      if(ready[0])return;android.os.SystemClock.sleep(50);
    }
    fail("Keyboard visibility did not update");
  }
  @Test
  public void reviewMistakesOnlyKeepsAnswersAndNeverAwardsAgain() {
    launchHistory();
    main(
        () -> {
          final int[] saves = {0};
          form()
              .bind(
                  cards(false),
                  (c, n, e, done) -> {
                    saves[0]++;
                    done.complete(true, e);
                  });
          answerAll();
          ((QuizQuestionCard) form().getChildAt(1)).restore("B");
          form().submit();
          android.widget.Button filter = null;
          for (int i = 0; i < form().getChildCount(); i++)
            if (form().getChildAt(i) instanceof android.widget.Button) {
              android.widget.Button b = (android.widget.Button) form().getChildAt(i);
              if (b.isEnabled() && b.getVisibility() == android.view.View.VISIBLE) filter = b;
            }
          assertNotNull(filter);
          filter.performClick();
          assertEquals(android.view.View.GONE, form().getChildAt(0).getVisibility());
          assertEquals(android.view.View.VISIBLE, form().getChildAt(1).getVisibility());
          assertEquals("B", form().answers().get(1));
          Bundle state = form().state();
          assertTrue(form().restore(state, (c, n, e, d) -> fail("Review must not save again")));
          assertEquals(android.view.View.GONE, form().getChildAt(0).getVisibility());
          form().submit();
          assertEquals(1, saves[0]);
        });
  }

  @Test public void legacyTimedOutDraftRemainsEditableAndRequiresAllAnswers() {
    launchHistory();
    main(() -> {
      final int[] saves={0};
      QuizFormView.Saver saver=(c,n,e,d)->{saves[0]++;d.complete(true,e);};
      form().bind(cards(false),saver);
      Bundle state=form().state();state.putBoolean("timedOut",true);
      assertTrue(form().restore(state,saver));
      form().submit();assertEquals(0,saves[0]);assertFalse(form().submitted());
      answerAll();form().submit();assertEquals(1,saves[0]);assertTrue(form().submitted());
    });
  }
}
