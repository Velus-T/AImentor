package com.example.asm_final;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import android.app.*;
import android.content.*;
import android.os.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import com.example.mentor.data.ActiveUser;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LogicAuditDeviceTest {
  private final Instrumentation ins = InstrumentationRegistry.getInstrumentation();
  private final Context context = ins.getTargetContext();
  private int owner, previous;
  private Activity activity;
  private android.database.sqlite.SQLiteDatabase storage;
  private com.example.historymodule.databases.SqliteDbHelper helper;
  private void main(Runnable action) { ins.runOnMainSync(action); ins.waitForIdleSync(); }
  private Object field(Object object,String name) {
    try { Field f=object.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(object); }
    catch (Exception e) { throw new AssertionError(e); }
  }
  private Object invoke(Object object,String name,Object... args) {
    try {
      for (Method m:object.getClass().getDeclaredMethods()) if(m.getName().equals(name) && m.getParameterCount()==args.length) {
        m.setAccessible(true); return m.invoke(object,args);
      }
      throw new NoSuchMethodException(name);
    } catch (InvocationTargetException e) { throw new AssertionError(e.getCause()); }
    catch (Exception e) { throw new AssertionError(e); }
  }
  private void launch(Class<?> type, Intent intent) {
    activity=ins.startActivitySync(intent.setClass(context,type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    ins.waitForIdleSync();
  }
  private void recreate() {
    Activity old=activity; main(old::recreate);
    long end=SystemClock.uptimeMillis()+15000;
    while(SystemClock.uptimeMillis()<end) {
      main(() -> {
        for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))
          if(a!=old && a.getClass()==old.getClass() && !a.isChangingConfigurations()) activity=a;
      });
      if(activity!=old) return;
      SystemClock.sleep(50);
    }
    fail("Activity was not recreated");
  }
  @Before public void prepare() {
    previous=ActiveUser.get(context);
    owner=(int)((AiMentorApplication)context.getApplicationContext()).services().registration()
        .register("logic_qa_"+System.nanoTime(),"Mentor01!2026","qa@gmail.com","0901234567");
    assertTrue(owner>0); ActiveUser.set(context,owner);
    helper=new com.example.historymodule.databases.SqliteDbHelper(context); storage=helper.getWritableDatabase();
    try(com.example.mathmodule.databases.SqliteDbHelper db=new com.example.mathmodule.databases.SqliteDbHelper(context)) { db.getWritableDatabase(); }
    try(com.example.modulelaptrinh.database.ProgrammingDbHelper db=new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) { db.getWritableDatabase(); }
    com.brightpath.science.di.ScienceModule.get(context).noteRepository().getAll(owner);
    try(com.example.mentor.data.QuizBank bank = new com.example.mentor.data.QuizBank(context)) { bank.getWritableDatabase(); }
  }
  @After public void cleanup() {
    main(() -> {
      for(Stage stage:new Stage[]{Stage.RESUMED,Stage.STARTED,Stage.STOPPED,Stage.CREATED})
        for(Activity a:new ArrayList<>(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage))) a.finish();
    });
    if(storage!=null && storage.isOpen()) {
      storage.execSQL("DROP TRIGGER IF EXISTS qa_logic_note_fail");
      if(owner>0) {
        storage.delete("mentor_quiz_bank_questions","set_id IN (SELECT id FROM mentor_quiz_bank_sets WHERE user_id=?)",new String[]{String.valueOf(owner)});
        storage.delete("mentor_quiz_bank_sets","user_id=?",new String[]{String.valueOf(owner)});
        for(String table:new String[]{"history_messages","history_notes","math_notes","math_history","science_notes","programming_sessions"})
          storage.delete(table,"user_id=?",new String[]{String.valueOf(owner)});
        storage.delete("users","id=?",new String[]{String.valueOf(owner)});
      }
    }
    if(helper!=null) helper.close(); ActiveUser.set(context,previous);
    if(owner>0) context.getSharedPreferences("SCIENCE_STUDY_PLANS",0).edit().remove("plan_user_"+owner).commit();
  }
  @Test public void historyAiParserActuallyReturnsFifteenAndMapsNumericAnswer() throws Exception {
    com.example.historymodule.api.HistoryAiService service=new com.example.historymodule.api.HistoryAiService();
    try {
      JSONArray json=new JSONArray();
      for(int i=0;i<15;i++) json.put(new JSONObject().put("question","Question "+i)
          .put("options",new JSONArray(Arrays.asList("One","Two","Three","Four")))
          .put("correctAnswer","1").put("explanation","Reason"));
      List<?> questions=(List<?>)invoke(service,"parseQuiz",json.toString(),15);
      assertEquals(15,questions.size());
      assertEquals(0,invoke(questions.get(0),"getCorrectIndex"));
    } finally { service.shutdown(); }
  }
  @Test public void scienceAiParserRejectsMalformedQuestionAndCanonicalizesBooleanOrder() throws Exception {
    com.brightpath.science.data.remote.GeminiScienceService service=
        new com.brightpath.science.data.remote.GeminiScienceService(null,null);
    JSONObject item=new JSONObject().put("type","TRUE_FALSE").put("prompt","Question")
        .put("options",new JSONArray(Arrays.asList("False","True")))
        .put("correctAnswer","False").put("explanation","Reason");
    List<?> parsed=(List<?>)invoke(service,"parseQuiz",new JSONArray().put(item).toString(),1);
    assertEquals(Arrays.asList("True","False"),invoke(parsed.get(0),"getOptions"));
    @SuppressWarnings("unchecked") List<com.brightpath.science.domain.model.QuizQuestion> scienceQuestions = (List<com.brightpath.science.domain.model.QuizQuestion>)(List<?>)parsed;
    try(com.example.mentor.data.QuizBank bank = new com.example.mentor.data.QuizBank(context)) {
      long id = bank.save(owner,"science","PHYSICS","MIDDLE_SCHOOL","QA boolean","GEMINI",
          com.brightpath.science.util.QuizCards.from(scienceQuestions));
      assertTrue(bank.questions(owner,id).get(0).correct("FALSE"));
    }
    item.put("type","MULTIPLE_CHOICE").put("options",new JSONArray()).put("correctAnswer","A");
    try { invoke(service,"parseQuiz",new JSONArray().put(item).toString(),1); fail("Invalid AI question accepted"); }
    catch(AssertionError e) { assertTrue(e.getCause() instanceof IllegalArgumentException); }
  }
  @Test public void studyPlanEncryptionFailureIsReportedWithoutOverwritingSavedPlan() {
    String key="plan_user_"+owner;
    context.getSharedPreferences("SCIENCE_STUDY_PLANS",0).edit().putString(key,"previous").commit();
    com.brightpath.science.data.repository.SharedPreferencesStudyPlanRepository repo=
        new com.brightpath.science.data.repository.SharedPreferencesStudyPlanRepository(context,new com.brightpath.science.data.local.TextCipher() {
          public String encrypt(String value) { throw new IllegalStateException("keystore"); }
          public String decrypt(String value) { return value; }
        });
    try { repo.save(owner,new com.brightpath.science.domain.model.StudyPlan(1,
        com.brightpath.science.domain.model.ScienceSubject.PHYSICS,
        com.brightpath.science.domain.model.EducationLevel.MIDDLE_SCHOOL,"Goal",false,Collections.emptyList())); fail("Write was reported successful"); }
    catch(IllegalStateException expected) { assertEquals("previous",context.getSharedPreferences("SCIENCE_STUDY_PLANS",0).getString(key,"")); }
  }
  @Test public void programmingRotationKeepsSessionAndDoesNotCreateDuplicateWelcome() {
    launch(com.example.modulelaptrinh.ProgrammingChatActivity.class,new Intent().putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID,owner));
    long id=(long)field(activity,"sessionId");
    assertTrue(id>0); recreate(); assertEquals(id,field(activity,"sessionId"));
    try(com.example.modulelaptrinh.database.ProgrammingDbHelper db=new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      assertEquals(1,db.getAllSessions(owner).size()); assertEquals(1,db.getMessagesBySession(id).size());
    }
  }
  @Test public void historyRotationKeepsNewConversationInsteadOfLaunchConversation() {
    launch(com.example.historymodule.HistoryChatActivity.class,new Intent().putExtra(com.example.historymodule.HistoryChatActivity.EXTRA_CONVERSATION_ID,"original"));
    main(() -> invoke(activity,"startNewConversation"));
    String id=(String)field(activity,"conversationId");
    assertNotEquals("original",id); recreate(); assertEquals(id,field(activity,"conversationId"));
  }
  @Test public void mathRotationKeepsVisibleMessagesAndDraft() {
    launch(com.example.mathmodule.MathChatActivity.class,new Intent());
    main(() -> {
      invoke(activity,"addMessage","QA question",true); invoke(activity,"addMessage","QA answer",false);
      ((android.widget.EditText)activity.findViewById(com.example.mathmodule.R.id.math_edt_question)).setText("QA draft");
    });
    recreate();
    main(() -> {
      assertEquals(3,((List<?>)field(activity,"messageList")).size());
      assertEquals("QA draft",((android.widget.EditText)activity.findViewById(com.example.mathmodule.R.id.math_edt_question)).getText().toString());
    });
  }
  private void failedNoteEdit(int module) {
    Object[] note={null}; String table; int titleId, saveId;
    if(module==0) {
      try(com.example.mathmodule.databases.SqliteDbHelper db=new com.example.mathmodule.databases.SqliteDbHelper(context)) {
        db.insertNote("Original","Formula","algebra"); note[0]=db.getAllNotes().get(0);
      }
      table="math_notes"; titleId=com.example.mathmodule.R.id.edt_note_title; saveId=com.example.mathmodule.R.string.math_formula_btn_save;
      launch(com.example.mathmodule.MathDashboardActivity.class,new Intent());
      main(() -> ((com.google.android.material.bottomnavigation.BottomNavigationView)activity.findViewById(com.example.mathmodule.R.id.bottom_navigation)).setSelectedItemId(com.example.mathmodule.R.id.nav_formula));
    } else if(module==1) {
      helper.insertNote(owner,"Original","Content"); note[0]=helper.loadNotes(owner).get(0);
      table="history_notes"; titleId=com.example.historymodule.R.id.note_title; saveId=com.example.historymodule.R.string.history_notes_save;
      launch(com.example.historymodule.HistoryDashboardActivity.class,new Intent());
      main(() -> ((com.example.historymodule.HistoryDashboardActivity)activity).selectTab(com.example.historymodule.R.id.nav_events));
    } else {
      com.brightpath.science.domain.repository.ScienceNoteRepository repo=com.brightpath.science.di.ScienceModule.get(context).noteRepository();
      repo.insert(owner,"Original","Content","chemistry"); note[0]=repo.getAll(owner).get(0);
      table="science_notes"; titleId=0; saveId=com.brightpath.science.R.string.science_save;
      launch(com.brightpath.science.presentation.ScienceMainActivity.class,new Intent());
      main(() -> ((com.brightpath.science.presentation.ScienceMainActivity)activity).selectTab(com.brightpath.science.R.id.navScienceFormula));
    }
    storage.execSQL("CREATE TRIGGER qa_logic_note_fail BEFORE UPDATE ON "+table+" BEGIN SELECT RAISE(ABORT,'qa'); END");
    main(() -> {
      AppCompatActivity a=(AppCompatActivity)activity; a.getSupportFragmentManager().executePendingTransactions();
      for(Fragment fragment:a.getSupportFragmentManager().getFragments())
        if(fragment.isVisible()) invoke(fragment,"showNoteDialog",note[0]);
    });
    if(titleId!=0) onView(withId(titleId)).inRoot(isDialog()).perform(replaceText("Unsaved draft"),closeSoftKeyboard());
    else onView(withHint(com.brightpath.science.R.string.science_note_title_hint)).inRoot(isDialog()).perform(replaceText("Unsaved draft"),closeSoftKeyboard());
    onView(withText(saveId)).inRoot(isDialog()).perform(click());
    onView(withText("Unsaved draft")).inRoot(isDialog()).check(matches(isDisplayed()));
    recreate();
    onView(withText("Unsaved draft")).inRoot(isDialog()).check(matches(isDisplayed()));
    storage.execSQL("DROP TRIGGER qa_logic_note_fail");
    onView(withText(saveId)).inRoot(isDialog()).perform(click());
    try(android.database.Cursor c=storage.rawQuery("SELECT title FROM "+table+" WHERE user_id=?",new String[]{String.valueOf(owner)})) {
      assertTrue(c.moveToFirst()); assertEquals("Unsaved draft",c.getString(0));
    }
  }
  @Test public void mathFailedNoteUpdateKeepsDraftAndAllowsRetry() { failedNoteEdit(0); }
  @Test public void historyFailedNoteUpdateKeepsDraftAndAllowsRetry() { failedNoteEdit(1); }
  @Test public void scienceFailedNoteUpdateKeepsDraftAndAllowsRetry() { failedNoteEdit(2); }
  @Test public void programmingQuizTabRequiresConfirmationBeforeDiscardingAttempt() {
    launch(com.example.modulelaptrinh.MainActivity.class,new Intent().putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID,owner));
    main(() -> {
      com.google.android.material.bottomnavigation.BottomNavigationView nav=activity.findViewById(com.example.modulelaptrinh.R.id.bottom_nav);
      nav.setSelectedItemId(com.example.modulelaptrinh.R.id.nav_quiz);
      ((AppCompatActivity)activity).getSupportFragmentManager().executePendingTransactions();
      com.example.mentor.ui.QuizFormView form=activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
      form.bind(Collections.singletonList(new com.example.mentor.data.QuizCard("QA question",
          Arrays.asList("One","Two","Three","Four"), com.example.mentor.data.QuizCard.letters(4),"A","Reason","EASY",false)),
          (correct,count,exp,completion) -> completion.complete(false,0));
      nav.setSelectedItemId(com.example.modulelaptrinh.R.id.nav_home);
      assertEquals(com.example.modulelaptrinh.R.id.nav_quiz,nav.getSelectedItemId());
    });
    onView(withText(com.example.mentor.ui.R.string.mentor_quiz_continue)).inRoot(isDialog()).perform(click());
    main(() -> ((AppCompatActivity)activity).getOnBackPressedDispatcher().onBackPressed());
    onView(withText(com.example.mentor.ui.R.string.mentor_quiz_continue)).inRoot(isDialog()).perform(click());
    assertFalse(activity.isFinishing());
    main(() -> ((com.google.android.material.bottomnavigation.BottomNavigationView)activity.findViewById(com.example.modulelaptrinh.R.id.bottom_nav)).setSelectedItemId(com.example.modulelaptrinh.R.id.nav_home));
    onView(withText(com.example.mentor.ui.R.string.mentor_quiz_leave)).inRoot(isDialog()).perform(click());
    main(() -> {
      ((AppCompatActivity)activity).getSupportFragmentManager().executePendingTransactions();
      assertEquals(com.example.modulelaptrinh.R.id.nav_home,((com.google.android.material.bottomnavigation.BottomNavigationView)activity.findViewById(com.example.modulelaptrinh.R.id.bottom_nav)).getSelectedItemId());
    });
  }
}
