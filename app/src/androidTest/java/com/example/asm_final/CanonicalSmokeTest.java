package com.example.asm_final;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.EditText;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.asm_final.activities.MainActivity;
import com.example.asm_final.activities.HomeActivity;
import com.example.asm_final.utils.DebugAccounts;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CanonicalSmokeTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private Activity resumed() {
        return ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).iterator().next();
    }
    private void main(Runnable action) {
        instrumentation.runOnMainSync(action);
        instrumentation.waitForIdleSync();
    }
    private void start(Class<?> type) {
        instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(), type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    private void awaitActivity(String expected) {
        long deadline = android.os.SystemClock.uptimeMillis() + 10000;
        while (android.os.SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            instrumentation.runOnMainSync(() -> {
                for (Activity activity : ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)) {
                    if (expected == null || expected.equals(activity.getClass().getName())) ready[0] = true;
                }
            });
            if (ready[0]) return;
            android.os.SystemClock.sleep(50);
        }
        fail("Activity did not resume: " + expected);
    }
    private void capture(String name) throws Exception {
        awaitActivity(null);
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            CountDownLatch drawn = new CountDownLatch(1);
            main(() -> {
                View decor = resumed().getWindow().getDecorView();
                decor.getViewTreeObserver().registerFrameCommitCallback(drawn::countDown);
                decor.invalidate();
            });
            assertTrue(drawn.await(5, TimeUnit.SECONDS));
        }
        instrumentation.getUiAutomation().waitForIdle(250, 3000);
        Bitmap screen = instrumentation.getUiAutomation().takeScreenshot();
        if (screen == null) return;
        File folder = instrumentation.getTargetContext().getExternalFilesDir("verified-ui");
        try (FileOutputStream output = new FileOutputStream(new File(folder, name + ".png"))) {
            screen.compress(Bitmap.CompressFormat.PNG, 100, output);
        } finally { screen.recycle(); }
    }

    @org.junit.Before public void selectTheme() {
        boolean night = Boolean.parseBoolean(InstrumentationRegistry.getArguments().getString("mentorNight", "false"));
        main(() -> com.example.asm_final.utils.ThemeHelper.setDarkMode(instrumentation.getTargetContext(), night));
    }
    private BottomNavigationView navigation(View view) {
        if (view instanceof BottomNavigationView) return (BottomNavigationView) view;
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                BottomNavigationView found = navigation(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
    private void checkSafeBounds(View navigation) {
        View content = resumed().findViewById(android.R.id.content);
        int[] origin = new int[2], position = new int[2];
        content.getLocationOnScreen(origin);
        navigation.getLocationOnScreen(position);
        assertTrue("Navigation must remain above the system controls",
                position[1] + navigation.getHeight() <= origin[1] + content.getHeight() - content.getPaddingBottom());
        assertTrue("Navigation must be visible", navigation.getHeight() > 0);
    }

    @Test public void loginRegistrationAndAllFourOriginalModulesOpen() throws Exception {
        DebugAccounts.seed(instrumentation.getTargetContext());
        start(MainActivity.class);
        capture("login");
        main(() -> resumed().findViewById(R.id.tvGoToRegister).performClick());
        awaitActivity("com.example.asm_final.activities.RegisterActivity");
        capture("register");
        main(() -> {
            assertNotNull(resumed().findViewById(R.id.edtRegisterConfirmPassword));
            resumed().findViewById(R.id.tvGoToLogin).performClick();
        });
        awaitActivity(MainActivity.class.getName());
        main(() -> {
            ((EditText) resumed().findViewById(R.id.edtLoginUsername)).setText("hocvien01");
            ((EditText) resumed().findViewById(R.id.edtLoginPassword)).setText("Mentor01!2026");
            resumed().findViewById(R.id.btnLogin).performClick();
        });
        awaitActivity(HomeActivity.class.getName());
        main(() -> {
            assertTrue(resumed() instanceof HomeActivity);
        });
        capture("home");
        main(() -> {
            ((BottomNavigationView) resumed().findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.nav_settings);
            ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
        });
        capture("settings");
        main(() -> {
            resumed().findViewById(R.id.setting_edit_profile).performClick();
            ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
            assertEquals("hocvien01", ((AppCompatActivity) resumed()).getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container).getArguments().getString("USERNAME"));
        });
        capture("profile");
        main(() -> { ((AppCompatActivity) resumed()).getOnBackPressedDispatcher().onBackPressed();
            ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
            assertNotNull(resumed().findViewById(R.id.setting_edit_profile)); });
        main(() -> {
            ((BottomNavigationView) resumed().findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.nav_category);
            ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
        });
        capture("courses");
        int[] ids = {R.id.cardMath, R.id.cardScience, R.id.cardHistory, R.id.cardIT};
        String[] targets = {"com.example.mathmodule.MathDashboardActivity", "com.brightpath.science.presentation.ScienceMainActivity", "com.example.historymodule.HistoryDashboardActivity", "com.example.modulelaptrinh.MainActivity"};
        for (int i = 0; i < ids.length; i++) {
            final int id = ids[i];
            final String expected = targets[i];
            awaitActivity(HomeActivity.class.getName());
            main(() -> resumed().findViewById(id).performClick());
            awaitActivity(expected);
            main(() -> assertEquals(expected, resumed().getClass().getName()));
            capture("module-" + i);
            if (i == 0) {
                String[] tabs = {"nav_home", "nav_history", "nav_formula", "nav_quiz"};
                for (String tab : tabs) {
                    main(() -> {
                        int tabId = resumed().getResources().getIdentifier(tab, "id", resumed().getPackageName());
                        View view = resumed().findViewById(tabId);
                        assertNotNull(view);
                        view.performClick();
                        ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
                    });
                    capture("math-" + tab);
                }
            } else {
                final BottomNavigationView[] bar = new BottomNavigationView[1];
                main(() -> bar[0] = navigation(resumed().getWindow().getDecorView()));
                assertNotNull(bar[0]);
                for (int tab = 0; tab < bar[0].getMenu().size(); tab++) {
                    final int tabId = bar[0].getMenu().getItem(tab).getItemId();
                    main(() -> {
                        bar[0].setSelectedItemId(tabId);
                        ((AppCompatActivity) resumed()).getSupportFragmentManager().executePendingTransactions();
                        checkSafeBounds(bar[0]);
                    });
                    capture("module-" + i + "-tab-" + tab);
                }
            }
            main(() -> resumed().finish());
        }
        awaitActivity(HomeActivity.class.getName());
        main(() -> ((HomeActivity) resumed()).logout());
        awaitActivity(MainActivity.class.getName());
        main(() -> { assertNotNull(resumed().findViewById(R.id.edtLoginUsername)); resumed().finish(); });
    }

    @Test public void systemInsetsDoNotAccumulateOrDoubleCountKeyboard() {
        start(MainActivity.class);
        try {
            main(() -> {
                View content = resumed().findViewById(android.R.id.content);
                WindowInsetsCompat bars = new WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0,24,0,48)).build();
                ViewCompat.dispatchApplyWindowInsets(content,bars);
                ViewCompat.dispatchApplyWindowInsets(content,bars);
                assertEquals(24,content.getPaddingTop());
                assertEquals(48,content.getPaddingBottom());
                WindowInsetsCompat keyboard = new WindowInsetsCompat.Builder(bars).setInsets(WindowInsetsCompat.Type.ime(),Insets.of(0,0,0,320)).build();
                ViewCompat.dispatchApplyWindowInsets(content,keyboard);
                assertEquals(320,content.getPaddingBottom());
            });
        } finally { main(() -> resumed().finish()); }
    }
    @Test public void mathReadsAreScopedToExplicitAccount() {
        try (com.example.mathmodule.databases.SqliteDbHelper db = new com.example.mathmodule.databases.SqliteDbHelper(instrumentation.getTargetContext())) {
            int first=180000001,second=180000002;
            long legacy=db.insertHistoryForUser(0,"unowned-legacy","answer");
            try {
                assertTrue(db.insertHistoryForUser(first,"first-account-only","answer")>0);
                assertTrue(db.insertHistoryForUser(second,"second-account-only","answer")>0);
                assertEquals(1,db.getAllHistory(first).size());
                assertEquals("first-account-only",db.getAllHistory(first).get(0).getQuestion());
                assertEquals("second-account-only",db.getAllHistory(second).get(0).getQuestion());
                assertTrue(db.getAllHistory(0).stream().anyMatch(row -> row.getId()==legacy));
            } finally { db.deleteUserHistory(first);db.deleteUserHistory(second);
                db.getWritableDatabase().delete("math_history","id=?",new String[]{String.valueOf(legacy)}); }

        }
    }
    @Test public void unsupportedScienceDowngradePreservesData() {
        try (android.database.sqlite.SQLiteDatabase memory=android.database.sqlite.SQLiteDatabase.create(null);
             com.brightpath.science.data.local.ScienceDbHelper helper=new com.brightpath.science.data.local.ScienceDbHelper(instrumentation.getTargetContext())) {
            memory.execSQL("CREATE TABLE sentinel(value TEXT)");memory.execSQL("INSERT INTO sentinel VALUES('keep')");
            try { helper.onDowngrade(memory,999,1);fail("Downgrade must be rejected"); }
            catch(android.database.sqlite.SQLiteException expected) { }
            try(android.database.Cursor cursor=memory.rawQuery("SELECT value FROM sentinel",null)) { assertTrue(cursor.moveToFirst());assertEquals("keep",cursor.getString(0)); }
        }
    }
    private void awaitEnabled(int id) {
        long limit=android.os.SystemClock.uptimeMillis()+10000;
        while(android.os.SystemClock.uptimeMillis()<limit) {
            final boolean[] ready={false};
            main(() -> { View view=resumed().findViewById(id);ready[0]=view!=null&&view.isEnabled(); });
            if(ready[0]) return;
            android.os.SystemClock.sleep(50);
        }
        fail("Control not ready: "+id);
    }
    @Test public void registerLoginUpdatePasswordAndLogout() {
        String username="solid_"+System.nanoTime();
        try {
            start(MainActivity.class);
            main(() -> resumed().findViewById(R.id.tvGoToRegister).performClick());
            awaitActivity("com.example.asm_final.activities.RegisterActivity");
            main(() -> {
                ((EditText)resumed().findViewById(R.id.edtRegisterUsername)).setText(username);
                ((EditText)resumed().findViewById(R.id.edtRegisterEmail)).setText(username+"@gmail.com");
                ((EditText)resumed().findViewById(R.id.edtRegisterPhone)).setText("0901234567");
                ((EditText)resumed().findViewById(R.id.edtRegisterPassword)).setText("Solid01!2026");
                ((EditText)resumed().findViewById(R.id.edtRegisterConfirmPassword)).setText("Solid01!2026");
                resumed().findViewById(R.id.btnRegister).performClick();
            });
            awaitActivity(MainActivity.class.getName());
            main(() -> {
                ((EditText)resumed().findViewById(R.id.edtLoginUsername)).setText(username);
                ((EditText)resumed().findViewById(R.id.edtLoginPassword)).setText("Solid01!2026");
                resumed().findViewById(R.id.btnLogin).performClick();
            });
            awaitActivity(HomeActivity.class.getName());
            main(() -> { ((HomeActivity)resumed()).navigateTo(R.id.nav_profile,false);
                ((AppCompatActivity)resumed()).getSupportFragmentManager().executePendingTransactions(); });
            awaitEnabled(R.id.btnUpdateProfile);
            main(() -> {
                ((EditText)resumed().findViewById(R.id.edtProfileFullName)).setText("SOLID regression");
                ((EditText)resumed().findViewById(R.id.edtProfilePassword)).setText("Solid02!2026");
                resumed().findViewById(R.id.btnUpdateProfile).performClick();
            });
            awaitEnabled(R.id.btnUpdateProfile);
            com.example.asm_final.accounts.LocalAccountRepository accounts=new com.example.asm_final.accounts.LocalAccountRepository(instrumentation.getTargetContext());
            assertTrue(accounts.authenticate(username,"Solid02!2026"));
            assertFalse(accounts.authenticate(username,"Solid01!2026"));
            assertEquals("SOLID regression",new com.example.asm_final.accounts.LocalUserProfileRepository(instrumentation.getTargetContext()).load(username).fullName);
            main(() -> ((HomeActivity)resumed()).logout());
            awaitActivity(MainActivity.class.getName());
            main(() -> resumed().finish());
        } finally {
            try(com.example.asm_final.activities.database.DatabaseHelper db=new com.example.asm_final.activities.database.DatabaseHelper(instrumentation.getTargetContext())) {
                db.getWritableDatabase().delete("users","username=?",new String[]{username});
            }
        }
    }
}
