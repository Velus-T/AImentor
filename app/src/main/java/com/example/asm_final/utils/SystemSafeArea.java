package com.example.asm_final.utils;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.asm_final.R;

/** Applies one inset policy to the app shell and every feature module without cross-module dependencies. */
public final class SystemSafeArea implements Application.ActivityLifecycleCallbacks {
    public static void install(Application app) { app.registerActivityLifecycleCallbacks(new SystemSafeArea()); }

    @Override public void onActivityResumed(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (content == null || Boolean.TRUE.equals(content.getTag(R.id.safe_area_applied))) return;
        content.setTag(R.id.safe_area_applied, true);
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final int left = content.getPaddingLeft(), top = content.getPaddingTop();
        final int right = content.getPaddingRight(), bottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(left + bars.left, top + bars.top, right + bars.right,
                    bottom + Math.max(bars.bottom, keyboard.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
        boolean light = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;
        android.util.TypedValue background = new android.util.TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.windowBackground, background, true)
                && background.type >= android.util.TypedValue.TYPE_FIRST_COLOR_INT
                && background.type <= android.util.TypedValue.TYPE_LAST_COLOR_INT) {
            content.setBackgroundColor(background.data);
        }
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(light);
        controller.setAppearanceLightNavigationBars(light);
        ViewCompat.requestApplyInsets(content);
    }
    @Override public void onActivityCreated(Activity a, Bundle b) { }
    @Override public void onActivityStarted(Activity a) { }
    @Override public void onActivityPaused(Activity a) { }
    @Override public void onActivityStopped(Activity a) { }
    @Override public void onActivitySaveInstanceState(Activity a, Bundle b) { }
    @Override public void onActivityDestroyed(Activity a) { }
}
