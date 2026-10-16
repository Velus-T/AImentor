package com.example.asm_final;

import android.app.Application;

import com.example.asm_final.utils.LocaleHelper;
import com.example.asm_final.utils.ThemeHelper;

/**
 * Applies the saved visual and language preferences before any Activity is created.
 */
/*
 * Role: The `AiMentorApplication` class in App Shell encapsulates a focused responsibility based on its role in the project.
 * Purpose: The class can be explained through three parts: input data, main processing, and the resulting output.
 */
public class AiMentorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applySavedTheme(this);
        LocaleHelper.applySavedLocale(this);
        com.example.asm_final.utils.SystemSafeArea.install(this);
        com.example.asm_final.utils.DebugAccounts.initialize(this);
    }
}
