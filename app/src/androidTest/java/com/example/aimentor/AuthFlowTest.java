package com.example.aimentor;

import android.app.Instrumentation;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.aimentor.activities.LoginActivity;
import com.example.aimentor.repository.UserRepository;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AuthFlowTest {
    private Activity login;
    private SharedPreferences preferences;
    private Map<String, ?> originalSession;

    @Before public void launchLogin() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        preferences = instrumentation.getTargetContext().getSharedPreferences("USER_INFO", 0);
        originalSession = preferences.getAll();
        Intent intent = new Intent(instrumentation.getTargetContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        login = instrumentation.startActivitySync(intent);
    }

    @After public void restoreSession() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            for (Activity activity : ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)) {
                activity.finish();
            }
            if (!login.isFinishing()) login.finish();
        });
        SharedPreferences.Editor editor = preferences.edit().clear();
        for (Map.Entry<String, ?> entry : originalSession.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) editor.putString(entry.getKey(), (String) value);
            else if (value instanceof Integer) editor.putInt(entry.getKey(), (Integer) value);
            else if (value instanceof Boolean) editor.putBoolean(entry.getKey(), (Boolean) value);
            else if (value instanceof Long) editor.putLong(entry.getKey(), (Long) value);
            else if (value instanceof Float) editor.putFloat(entry.getKey(), (Float) value);
        }
        editor.commit();
    }

    @Test public void emptyLoginShowsFieldErrorAndClearsItOnEdit() throws Exception {
        click(R.id.btnSubmit);
        main(() -> assertNotNull(((TextInputLayout) login.findViewById(R.id.inputUsername)).getError()));
        enter(R.id.edtUsername, "a");
        main(() -> assertNull(((TextInputLayout) login.findViewById(R.id.inputUsername)).getError()));
    }

    @Test public void demoAccountCanSignIn() {
        signIn("hocvien02", "Mentor02!2026");
        main(() -> assertNotNull(resumed().findViewById(R.id.btnHomeScience)));
        assertEquals("hocvien02", preferences.getString("USERNAME_USER", ""));
    }

    @Test public void signupReturnsToLoginAndNewAccountCanSignIn() throws Exception {
        String username = "uitest" + System.nanoTime();
        UserRepository repository = new UserRepository(InstrumentationRegistry.getInstrumentation().getTargetContext());
        try {
            click(R.id.tvSignUp);
            capture("signup");
            enter(R.id.edtUsername, username);
            enter(R.id.edtEmail, "ui@example.invalid");
            enter(R.id.edtPassword, "UiTest!2026");
            click(R.id.btnSubmit);
            main(() -> assertNotNull(resumed().findViewById(R.id.tvSignUp)));
            capture("login");
            signIn(username, "UiTest!2026");
            main(() -> assertNotNull(resumed().findViewById(R.id.btnHomeScience)));
        } finally {
            repository.getWritableDatabase().delete("users", "username = ?", new String[]{username});
            repository.close();
        }
    }

    private void signIn(String username, String password) {
        enter(R.id.edtUsername, username);
        enter(R.id.edtPassword, password);
        click(R.id.btnSubmit);
    }

    private Activity resumed() {
        return ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).iterator().next();
    }

    private void main(Runnable action) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(action);
        instrumentation.waitForIdleSync();
    }

    private void enter(int id, String text) {
        main(() -> ((android.widget.EditText) resumed().findViewById(id)).setText(text));
    }

    private void click(int id) { main(() -> resumed().findViewById(id).performClick()); }

    private void capture(String name) throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        // A resumed activity can precede its first rendered frame.
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            CountDownLatch drawn = new CountDownLatch(1);
            main(() -> {
                android.view.View decor = resumed().getWindow().getDecorView();
                decor.getViewTreeObserver().registerFrameCommitCallback(drawn::countDown);
                decor.invalidate();
            });
            assertTrue("Activity frame was not rendered", drawn.await(5, TimeUnit.SECONDS));
        }
        instrumentation.waitForIdleSync();
        instrumentation.getUiAutomation().waitForIdle(250, 3000);
        Bitmap screenshot = instrumentation.getUiAutomation().takeScreenshot();
        if (screenshot == null) return;
        File folder = instrumentation.getTargetContext().getExternalFilesDir("ui-previews");
        try (FileOutputStream output = new FileOutputStream(new File(folder, name + ".png"))) {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, output);
        } finally { screenshot.recycle(); }
    }
}
