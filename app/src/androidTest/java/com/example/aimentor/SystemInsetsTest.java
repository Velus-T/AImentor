package com.example.aimentor;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.aimentor.activities.LoginActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

/** Exercises the actual activity listener for gesture, three-button and IME modes. */
@RunWith(AndroidJUnit4.class)
public class SystemInsetsTest {
    @Test public void safeAreaDoesNotAccumulateAndUsesLargerKeyboardInset() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent intent = new Intent(instrumentation.getTargetContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = instrumentation.startActivitySync(intent);
        try {
            instrumentation.runOnMainSync(() -> {
                View content = activity.findViewById(android.R.id.content);
                apply(content, 24, 48, 0, 0);
                assertEquals(24, content.getPaddingTop());
                assertEquals(48, content.getPaddingBottom());
                apply(content, 24, 48, 0, 0);
                assertEquals(48, content.getPaddingBottom());
                apply(content, 24, 16, 0, 0);
                assertEquals(16, content.getPaddingBottom());
                apply(content, 24, 48, 320, 32);
                assertEquals(320, content.getPaddingBottom());
                assertEquals(32, content.getPaddingLeft());
                apply(content, 24, 48, 0, 0);
                assertEquals(48, content.getPaddingBottom());
                assertEquals(0, content.getPaddingLeft());
            });
        } finally {
            instrumentation.runOnMainSync(activity::finish);
        }
    }

    private void apply(View content, int top, int bottom, int keyboard, int cutout) {
        WindowInsetsCompat insets = new WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, top, 0, bottom))
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(cutout, 0, 0, 0))
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, keyboard))
                .build();
        ViewCompat.dispatchApplyWindowInsets(content, insets);
    }
}
