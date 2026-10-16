package com.brightpath.science.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.brightpath.science.presentation.ScienceMainActivity;
import com.brightpath.science.util.ScienceUserSession;

/** Public entry point for the Science feature. */
public final class ScienceFeature {
        /*
 * Responsibility: Initializes ScienceFeature and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private ScienceFeature() { }

        /*
 * Responsibility: Creates the required navigation/action and opens the Science destination handled by `open`.
 * Purpose: Keeps navigation code separate from the business logic executed by the destination screen.
 */
public static void open(@NonNull Context context) {
        open(context, 0);
    }

        /*
 * Responsibility: Creates the required navigation/action and opens the Science destination handled by `open`.
 * Purpose: Keeps navigation code separate from the business logic executed by the destination screen.
 */
public static void open(@NonNull Context context, int userId) {
        ScienceUserSession.setUserId(context, Math.max(0, userId));
        Intent intent = new Intent(context, ScienceMainActivity.class);
        if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
