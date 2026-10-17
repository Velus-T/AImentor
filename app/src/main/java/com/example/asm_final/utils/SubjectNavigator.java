package com.example.asm_final.utils;

import android.content.Context;
import com.brightpath.science.api.ScienceFeature;
import com.example.historymodule.api.HistoryFeature;
import com.example.mathmodule.api.MathFeature;
import com.example.modulelaptrinh.api.ProgrammingFeature;

/** Single account-aware entry point shared by Home and Subjects. */
public final class SubjectNavigator {
  private SubjectNavigator() {}

  public static void open(Context context, RecentActivity.Subject subject, int userId) {
    switch (subject) {
      case MATH:
        MathFeature.open(context, userId);
        break;
      case SCIENCE:
        ScienceFeature.open(context, userId);
        break;
      case HISTORY:
        HistoryFeature.open(context, userId);
        break;
      case PROGRAMMING:
        ProgrammingFeature.open(context, userId);
        break;
    }
  }
}
