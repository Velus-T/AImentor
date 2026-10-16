package com.example.asm_final;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.is;

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
 private final Instrumentation ins=InstrumentationRegistry.getInstrumentation();
 private final Context context=ins.getTargetContext();
 private String username; private int userId, originalUser; private boolean originalNight;
 private Activity resumed(){return ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).iterator().next();}
 private void main(Runnable action){ins.runOnMainSync(action);ins.waitForIdleSync();}
 private void start(Class<?> type){ins.startActivitySync(new Intent(context,type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("USERNAME",username));}
 private void waitFor(Predicate<Activity> predicate){
  long end=android.os.SystemClock.uptimeMillis()+15000;
  while(android.os.SystemClock.uptimeMillis()<end){boolean[] ready={false};ins.runOnMainSync(()->{Collection<Activity> a=ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);if(!a.isEmpty())ready[0]=predicate.test(a.iterator().next());});if(ready[0])return;android.os.SystemClock.sleep(50);}fail("UI not ready");
 }
 @Before public void prepare(){
  originalUser=com.example.mentor.data.ActiveUser.get(context);originalNight=ThemeHelper.isDarkMode(context);
  username="ux_"+System.nanoTime();AppServices services=((AiMentorApplication)context.getApplicationContext()).services();
  assertTrue(services.registration().register(username,"Mentor01!2026","ux@gmail.com","0901234567")>0);
  userId=services.accountLookup().userId(username);com.example.mentor.data.ActiveUser.set(context,userId);
 }
 @After public void cleanup(){
  main(()->{for(Stage stage:new Stage[]{Stage.RESUMED,Stage.STARTED,Stage.STOPPED,Stage.CREATED})for(Activity a:new ArrayList<>(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage)))a.finish();ThemeHelper.setDarkMode(context,originalNight);});
  try(com.example.asm_final.activities.database.DatabaseHelper helper=new com.example.asm_final.activities.database.DatabaseHelper(context)){
   android.database.sqlite.SQLiteDatabase db=helper.getWritableDatabase();db.delete("math_history","user_id=?",new String[]{Integer.toString(userId)});db.delete("users","id=?",new String[]{Integer.toString(userId)});
  }
  com.example.mentor.data.ActiveUser.set(context,originalUser);
 }
 private TextInputLayout fieldContainer(EditText field){ViewParent parent=field.getParent();while(!(parent instanceof TextInputLayout))parent=parent.getParent();return(TextInputLayout)parent;}
 private void capture(String name){
  try{ins.getUiAutomation().waitForIdle(250,3000);android.graphics.Bitmap bitmap=ins.getUiAutomation().takeScreenshot();java.io.File folder=context.getExternalFilesDir("verified-ui");folder.mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(folder,name+".png"))){bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}finally{bitmap.recycle();}}
  catch(Exception error){throw new AssertionError(error);}
 }
 @Test public void loginErrorsAreInlineAndClearWithoutLosingDraft(){
  start(MainActivity.class);main(()->{EditText name=resumed().findViewById(R.id.edtLoginUsername);assertNull(fieldContainer(name).getError());resumed().findViewById(R.id.btnLogin).performClick();assertNotNull(fieldContainer(name).getError());assertTrue(name.isFocused());name.setText("draft_user");assertNull(fieldContainer(name).getError());assertNotNull(fieldContainer((EditText)resumed().findViewById(R.id.edtLoginPassword)).getError());});capture("ux-login-validation");
 }
 @Test public void registrationShowsGmailConstraintBeforeSubmissionAndTargetsInvalidField(){
  start(RegisterActivity.class);main(()->{EditText email=resumed().findViewById(R.id.edtRegisterEmail);assertNotNull(fieldContainer(email).getHelperText());assertNull(fieldContainer(email).getError());((EditText)resumed().findViewById(R.id.edtRegisterUsername)).setText("draft_user");email.setText("learner@example.org");((EditText)resumed().findViewById(R.id.edtRegisterPhone)).setText("0901234567");((EditText)resumed().findViewById(R.id.edtRegisterPassword)).setText("Mentor01!2026");((EditText)resumed().findViewById(R.id.edtRegisterConfirmPassword)).setText("Mentor01!2026");resumed().findViewById(R.id.btnRegister).performClick();assertNotNull(fieldContainer(email).getError());assertTrue(email.isFocused());assertEquals("draft_user",((EditText)resumed().findViewById(R.id.edtRegisterUsername)).getText().toString());});capture("ux-register-validation");
 }
 @Test public void newLearnerGetsStartActionAndStatsRemainAvailableAfterThemeChange(){
  start(HomeActivity.class);waitFor(a->a.findViewById(R.id.text_recent_empty)!=null&&a.findViewById(R.id.text_recent_empty).getVisibility()==View.VISIBLE);capture("ux-home-new-learner");
  main(()->{assertEquals(View.GONE,resumed().findViewById(R.id.layout_detailed_stats).getVisibility());resumed().findViewById(R.id.button_start_learning).performClick();((AppCompatActivity)resumed()).getSupportFragmentManager().executePendingTransactions();assertNotNull(resumed().findViewById(R.id.cardMath));((HomeActivity)resumed()).navigateTo(R.id.nav_home,false);((AppCompatActivity)resumed()).getSupportFragmentManager().executePendingTransactions();resumed().findViewById(R.id.button_stats_toggle).performClick();ThemeHelper.setDarkMode(context,!originalNight);});
  waitFor(a->a instanceof HomeActivity&&a.findViewById(R.id.layout_detailed_stats)!=null&&(a.getResources().getConfiguration().uiMode&android.content.res.Configuration.UI_MODE_NIGHT_MASK)==(!originalNight?android.content.res.Configuration.UI_MODE_NIGHT_YES:android.content.res.Configuration.UI_MODE_NIGHT_NO));
  main(()->assertEquals(View.VISIBLE,resumed().findViewById(R.id.layout_detailed_stats).getVisibility()));
 }
 @Test public void returningLearnerReopensActualRecentSubjectWithCorrectAccount(){
  try(com.example.mathmodule.databases.SqliteDbHelper helper=new com.example.mathmodule.databases.SqliteDbHelper(context)){assertTrue(helper.insertHistoryForUser(userId,"UX saved question","UX saved answer")>0);}
  start(HomeActivity.class);waitFor(a->a.findViewById(R.id.button_start_learning)!=null&&((TextView)a.findViewById(R.id.button_start_learning)).getText().toString().contains(context.getString(R.string.cat_math)));capture("ux-home-returning-learner");
  main(()->resumed().findViewById(R.id.button_start_learning).performClick());waitFor(a->a instanceof com.example.mathmodule.MathDashboardActivity);assertEquals(userId,com.example.mathmodule.util.MathUserSession.getUserId(context));
 }
 @Test public void calculatorResultStaysVisibleWhenKeypadScrolls(){
  start(com.example.mathmodule.MathDashboardActivity.class);main(()->com.example.mathmodule.calculator.MathCalculator.show(resumed()));
  for(String key:new String[]{"6","×","7","="})onView(withText(key)).perform(scrollTo(),click());
  onView(withTagValue(is((Object)"mentor_calculator_keypad"))).perform(swipeUp());onView(withText("42")).check(matches(isDisplayed()));capture("ux-calculator-pinned-result");
  onView(withText(com.example.mathmodule.R.string.math_calculator_collapse)).perform(click());
 }
}
