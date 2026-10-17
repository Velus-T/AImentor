package com.example.asm_final;

import static org.junit.Assert.*;
import android.app.*;
import android.content.*;
import android.os.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import com.example.mentor.data.*;
import com.example.mentor.ui.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;

/** Explicit opt-in: real network requests may consume Gemini quota. Never includes credentials. */
@RunWith(AndroidJUnit4.class)
public class LiveGeminiSmokeTest {
  private final Instrumentation ins=InstrumentationRegistry.getInstrumentation();
  private final Context context=ins.getTargetContext();
  private int owner,previous;
  private Activity activity;
  private void main(Runnable action) { ins.runOnMainSync(action); ins.waitForIdleSync(); }
  @Before public void prepare() {
    Assume.assumeTrue("true".equals(InstrumentationRegistry.getArguments().getString("mentorLiveAi")));
    previous=ActiveUser.get(context);
    owner=(int)((AiMentorApplication)context.getApplicationContext()).services().registration()
        .register("live_qa_"+System.nanoTime(),"Mentor01!2026","qa@gmail.com","0901234567");
    assertTrue(owner>0); ActiveUser.set(context,owner);
    try(com.example.historymodule.databases.SqliteDbHelper db=new com.example.historymodule.databases.SqliteDbHelper(context)) { db.getWritableDatabase(); }
    try(com.example.mathmodule.databases.SqliteDbHelper db=new com.example.mathmodule.databases.SqliteDbHelper(context)) { db.getWritableDatabase(); }
    try(com.example.modulelaptrinh.database.ProgrammingDbHelper db=new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) { db.getWritableDatabase(); }
    com.brightpath.science.di.ScienceModule.get(context).quizAttemptRepository().getTotalExp(owner);
  }
  @After public void cleanup() {
    if(owner<=0) return;
    main(() -> {
      for(Stage stage:new Stage[]{Stage.RESUMED,Stage.STARTED,Stage.STOPPED,Stage.CREATED})
        for(Activity a:new ArrayList<>(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage))) a.finish();
    });
    try(QuizBank bank=new QuizBank(context)) {
      android.database.sqlite.SQLiteDatabase db=bank.getWritableDatabase();
      String[] args={String.valueOf(owner)};
      db.delete("mentor_quiz_bank_questions","set_id IN (SELECT id FROM mentor_quiz_bank_sets WHERE user_id=?)",args);
      db.delete("mentor_quiz_bank_sets","user_id=?",args);
      db.delete("math_quiz_questions","set_id IN (SELECT id FROM math_quiz_sets WHERE owner_id=?)",args);
      db.delete("math_quiz_sets","owner_id=?",args);
      for(String table:new String[]{"math_quiz_attempts","math_user_profile","history_quiz_attempts","science_quiz_attempts","programming_quiz_attempts","programming_user_progress"}) db.delete(table,"user_id=?",args);
      db.delete("users","id=?",args);
    }
    ActiveUser.set(context,previous);
  }
  private void report(String name,JSONObject data) throws Exception {
    java.io.File folder=context.getExternalFilesDir("live-ai"); folder.mkdirs();
    try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(folder,name+".json"))) {
      out.write(data.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
  }
  @Test public void historyChat() throws Exception {
    com.example.historymodule.api.HistoryGeminiApiClient client=new com.example.historymodule.api.HistoryGeminiApiClient();
    assertTrue(client.isConfigured());
    com.example.historymodule.api.HistoryGeminiApiClient.Result r=client.generateText("Trả lời ngắn bằng tiếng Việt: Chiến thắng Điện Biên Phủ diễn ra năm nào?",null,null,0.2,2048);
    assertTrue(r.getText().contains("1954"));
    report("history-chat",new JSONObject().put("text",r.getText()).put("tokens",r.getTotalTokens()));
  }
  @Test public void mathChat() throws Exception {
    com.example.mathmodule.api.MathGeminiApiClient client=new com.example.mathmodule.api.MathGeminiApiClient();
    assertTrue(client.isConfigured());
    com.example.mathmodule.api.MathGeminiApiClient.Result r=client.generateText("Giải ngắn bằng tiếng Việt: 2x + 3 = 7.",null,null,0.2,2048);
    assertFalse(r.text.trim().isEmpty());
    report("math-chat",new JSONObject().put("text",r.text));
  }
  @Test public void programmingChat() throws Exception {
    com.example.modulelaptrinh.utils.GeminiApiClient client=new com.example.modulelaptrinh.utils.GeminiApiClient(com.example.modulelaptrinh.utils.GeminiApiClient.Usage.CHAT);
    com.example.modulelaptrinh.utils.GeminiApiClient.Result r=client.generateText("Trả lời ngắn bằng tiếng Việt: vòng lặp for trong Python dùng để làm gì?",Collections.emptyList(),Collections.emptyList(),0.2,2048);
    assertFalse(r.text.trim().isEmpty()); report("programming-chat",new JSONObject().put("text",r.text));
  }
  @Test public void scienceChat() throws Exception {
    com.brightpath.science.data.remote.GeminiApiClient client=new com.brightpath.science.data.remote.GeminiApiClient();
    assertTrue(client.isConfigured());
    com.brightpath.science.data.remote.GeminiApiClient.Result r=client.generateText("Trả lời ngắn bằng tiếng Việt: định luật Ohm là gì?",null,null,0.2,2048);
    assertFalse(r.text.trim().isEmpty()); report("science-chat",new JSONObject().put("text",r.text).put("tokens",r.totalTokens));
  }
  private void invoke(Object target,String name,Object... args) {
    try {
      for(Method m:target.getClass().getDeclaredMethods()) if(m.getName().equals(name)) {
        m.setAccessible(true); m.invoke(target,args); return;
      }
      throw new NoSuchMethodException(name);
    } catch(Exception e) { throw new AssertionError("Unable to start quiz flow",e); }
  }
  private void quiz(String subject,int count) throws Exception {
    Class<?> type=subject.equals("history") ? com.example.historymodule.HistoryQuizActivity.class
        : subject.equals("science") ? com.brightpath.science.presentation.ScienceQuizActivity.class
        : subject.equals("math") ? com.example.mathmodule.MathDashboardActivity.class : com.example.modulelaptrinh.MainActivity.class;
    activity=ins.startActivitySync(new Intent(context,type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(com.example.modulelaptrinh.MainActivity.EXTRA_USER_ID,owner));
    main(() -> {
      if(subject.equals("math")) {
        ((com.google.android.material.bottomnavigation.BottomNavigationView)activity.findViewById(com.example.mathmodule.R.id.bottom_navigation)).setSelectedItemId(com.example.mathmodule.R.id.nav_quiz);
        ((AppCompatActivity)activity).getSupportFragmentManager().executePendingTransactions();
        Object fragment=((AppCompatActivity)activity).getSupportFragmentManager().findFragmentById(com.example.mathmodule.R.id.fragment_container);
        invoke(fragment,"generateQuizWithAI","Phương trình bậc nhất",count,com.example.mathmodule.util.MathEducationLevel.MIDDLE_SCHOOL);
      } else {
        if(subject.equals("programming")) {
          ((com.google.android.material.bottomnavigation.BottomNavigationView)activity.findViewById(com.example.modulelaptrinh.R.id.bottom_nav)).setSelectedItemId(com.example.modulelaptrinh.R.id.nav_quiz);
          ((AppCompatActivity)activity).getSupportFragmentManager().executePendingTransactions();
        }
        QuizSetupView setup=activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup);
        setup.topic.setText(subject.equals("history") ? "Chiến thắng Điện Biên Phủ" : subject.equals("science") ? "Điện học cơ bản" : "Python cơ bản");
        setup.count.setSelection(count/5-1); setup.create.performClick();
      }
    });
    List<QuizCard> cards=null;
    long end=SystemClock.uptimeMillis()+240000;
    while(SystemClock.uptimeMillis()<end) {
      try(QuizBank bank=new QuizBank(context)) {
        List<QuizBank.Set> sets=bank.list(owner,subject);
        if(!sets.isEmpty()) { cards=bank.questions(owner,sets.get(0).id); break; }
      }
      main(() -> { QuizSetupView setup=activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup); if(setup!=null && setup.create.isEnabled()) fail(subject+" generation ended without a bank entry: "+setup.status.getText()); });
      SystemClock.sleep(250);
    }
    assertNotNull(subject+" did not persist a generated quiz",cards); assertEquals(count,cards.size());
    JSONArray rows=new JSONArray();
    for(QuizCard q:cards) {
      assertTrue(q.correct(q.answer));
      rows.put(new JSONObject().put("question",q.prompt).put("options",new JSONArray(q.options))
          .put("answer",q.answer).put("explanation",q.explanation).put("difficulty",q.difficulty));
    }
    report(subject+"-quiz",new JSONObject().put("count",cards.size()).put("questions",rows));
  }
  @Test public void historyFifteenQuestionQuiz() throws Exception { quiz("history",15); }
  @Test public void mathFiveQuestionQuiz() throws Exception { quiz("math",5); }
  @Test public void programmingFiveQuestionQuiz() throws Exception { quiz("programming",5); }
  @Test public void scienceFiveQuestionQuiz() throws Exception { quiz("science",5); }
}
