package com.example.asm_final;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.click;
import static org.hamcrest.Matchers.anything;
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
    private final Instrumentation ins=InstrumentationRegistry.getInstrumentation();
    private final Context context=ins.getTargetContext();
    private int owner,previous;
    private Activity activity;
    private List<QuizCard> cards(boolean math) {
        ArrayList<QuizCard> cards=new ArrayList<>();
        for(String difficulty:new String[]{"EASY","MEDIUM","HARD"})cards.add(new QuizCard("QA fixture "+difficulty,
                Arrays.asList("1","2","3","4"),QuizCard.letters(4),"A","QA explanation",difficulty,math));
        return cards;
    }
    private void main(Runnable action) { ins.runOnMainSync(action);ins.waitForIdleSync(); }
    @Before public void prepare() {
        previous=ActiveUser.get(context);
        AppServices services=((AiMentorApplication)context.getApplicationContext()).services();
        String name="quiz_qa_"+System.nanoTime();
        assertTrue(services.registration().register(name,"Mentor01!2026","qa@gmail.com","0901234567")>0);
        owner=services.accountLookup().userId(name);ActiveUser.set(context,owner);
        try(com.example.mathmodule.databases.SqliteDbHelper db=new com.example.mathmodule.databases.SqliteDbHelper(context)) { db.getWritableDatabase(); }
        try(com.example.modulelaptrinh.database.ProgrammingDbHelper db=new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) { db.getWritableDatabase(); }
        try(com.example.historymodule.databases.SqliteDbHelper db=new com.example.historymodule.databases.SqliteDbHelper(context)) { db.getWritableDatabase(); }
        com.brightpath.science.di.ScienceModule.get(context).quizAttemptRepository().getTotalExp(owner);
        try(QuizBank db=new QuizBank(context)) { db.getWritableDatabase(); }
    }
    @After public void cleanup() {
        main(()->{
            for(Stage stage:new Stage[]{Stage.RESUMED,Stage.STARTED,Stage.STOPPED,Stage.CREATED})
                for(Activity a:new ArrayList<>(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage)))a.finish();
        });
        try(QuizBank bank=new QuizBank(context)) {
            android.database.sqlite.SQLiteDatabase db=bank.getWritableDatabase();
            db.execSQL("DROP TRIGGER IF EXISTS qa_bank_fail");
            db.execSQL("DROP TRIGGER IF EXISTS qa_progress_fail");
            db.delete("mentor_quiz_bank_questions","set_id IN (SELECT id FROM mentor_quiz_bank_sets WHERE user_id=?)",new String[]{String.valueOf(owner)});
            db.delete("mentor_quiz_bank_sets","user_id=?",new String[]{String.valueOf(owner)});
            for(String table:new String[]{"history_quiz_attempts","programming_quiz_attempts","programming_user_progress","science_quiz_attempts","math_quiz_attempts","math_user_profile"})
                db.delete(table,"user_id=?",new String[]{String.valueOf(owner)});
            db.delete("math_quiz_questions","set_id IN (SELECT id FROM math_quiz_sets WHERE owner_id=?)",new String[]{String.valueOf(owner)});
            db.delete("math_quiz_sets","owner_id=?",new String[]{String.valueOf(owner)});
            db.delete("users","id=?",new String[]{String.valueOf(owner)});
        }
        ActiveUser.set(context,previous);
    }
    @Test public void bankPersistsClassifiesDeduplicatesAndIsolatesOwners() {
        long id;
        try(QuizBank bank=new QuizBank(context)) {
            id=bank.save(owner,"history","Vietnam","MIDDLE_SCHOOL","QA topic","GEMINI",cards(false));
            assertEquals(id,bank.save(owner,"history","Vietnam","MIDDLE_SCHOOL","QA topic","GEMINI",cards(false)));
            assertEquals(1,bank.list(owner,"history").size());
            assertEquals("Vietnam",bank.list(owner,"history").get(0).scope);
            assertEquals(3,bank.list(owner,"history").get(0).count);
            assertTrue(bank.list(owner+1000000,"history").isEmpty());
            assertTrue(bank.questions(owner+1000000,id).isEmpty());
        }
        try(QuizBank reopened=new QuizBank(context)) {
            assertEquals("HARD",reopened.questions(owner,id).get(2).difficulty);
            assertEquals("QA explanation",reopened.questions(owner,id).get(0).explanation);
            assertEquals("A",reopened.questions(owner,id).get(0).answer);
        }
    }
    @Test public void failedQuestionInsertRollsBackEntireBankSet() {
        try(QuizBank bank=new QuizBank(context)) {
            bank.getWritableDatabase().execSQL("CREATE TRIGGER qa_bank_fail BEFORE INSERT ON mentor_quiz_bank_questions BEGIN SELECT RAISE(ABORT,'QA failure'); END");
            try { bank.save(owner,"history","Vietnam","MIDDLE_SCHOOL","QA rollback","GEMINI",cards(false));fail(); }
            catch(android.database.SQLException expected) {}
            assertTrue(bank.list(owner,"history").isEmpty());
        }
    }
    private void launch(Class<?> type,Intent intent) {
        activity=ins.startActivitySync(intent.setClass(context,type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        ins.waitForIdleSync();
    }
    private void launchHistory() { launch(com.example.historymodule.HistoryQuizActivity.class,new Intent()); }
    private QuizFormView form() { return activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_form); }
    private void answerAll() {
        for(int i=0;i<form().getChildCount();i++)if(form().getChildAt(i) instanceof QuizQuestionCard)
            ((QuizQuestionCard)form().getChildAt(i)).restore("A");
    }
    @Test public void commonFormKeepsAnswersOnSaveFailureAndNeverDoubleAwards() {
        launchHistory();final int[] saves={0};final Bundle[] state={null};
        main(()->{
            form().bind(cards(false),(correct,count,exp,done)->{
                saves[0]++;assertEquals(3,correct);assertEquals(35,exp);done.complete(saves[0]>1,35);
            });
            form().submit(false);assertEquals(0,saves[0]);
            answerAll();form().submit(false);assertFalse(form().submitted());assertEquals("A",form().answers().get(0));
            form().submit(false);form().submit(false);assertTrue(form().submitted());assertEquals(2,saves[0]);
            state[0]=form().state();
            assertTrue(form().restore(state[0],(c,n,e,d)->fail("Restoring must not save again")));
            assertTrue(form().submitted());form().submit(false);
        });
    }
    private void savedQuiz(String subject,String scope,String level,boolean math) {
        try(QuizBank bank=new QuizBank(context)) { bank.save(owner,subject,scope,level,"QA offline "+subject,"GEMINI",cards(math)); }
    }
    private void chooseBank(QuizSetupView setup) {
        main(()->setup.bank.performClick());onData(anything()).atPosition(0).perform(click());ins.waitForIdleSync();
    }
    private void submitAndCheck() { main(()->{ assertEquals(android.view.View.VISIBLE,form().getVisibility());answerAll();form().submit(false);assertTrue(form().submitted()); }); }
    @Test public void historyCanPracticeSavedQuestionsWithoutCallingAI() {
        savedQuiz("history","Lịch sử Việt Nam","Trung học cơ sở",false);launchHistory();
        chooseBank(activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup));submitAndCheck();
        try(com.example.historymodule.databases.SqliteDbHelper db=new com.example.historymodule.databases.SqliteDbHelper(context)) { assertEquals(35,db.getTotalQuizExp(owner)); }
    }
    @Test public void scienceCanPracticeSavedQuestionsWithoutCallingAI() {
        savedQuiz("science","physics","MIDDLE_SCHOOL",false);launch(com.brightpath.science.presentation.ScienceQuizActivity.class,new Intent());
        chooseBank(activity.findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup));submitAndCheck();
        assertEquals(35,com.brightpath.science.di.ScienceModule.get(context).quizAttemptRepository().getTotalExp(owner));
    }
    @Test public void programmingAttemptAndExpRollBackTogether() {
        try(com.example.modulelaptrinh.database.ProgrammingDbHelper helper=new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
            android.database.sqlite.SQLiteDatabase db=helper.getWritableDatabase();
            db.execSQL("CREATE TRIGGER qa_progress_fail BEFORE INSERT ON programming_user_progress WHEN NEW.user_id="+owner+" BEGIN SELECT RAISE(ABORT,'QA failure'); END");
            assertEquals(-1,helper.saveQuizResult(owner,"Lập trình","Trung học cơ sở","QA topic",3,3,35));
            try(android.database.Cursor c=db.rawQuery("SELECT COUNT(*) FROM programming_quiz_attempts WHERE user_id=?",new String[]{String.valueOf(owner)})) { c.moveToFirst();assertEquals(0,c.getInt(0)); }
            assertEquals(0,helper.getTotalExpAllSubjects(owner));
        }
    }
}
