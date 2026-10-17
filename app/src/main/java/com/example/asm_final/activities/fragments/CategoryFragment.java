package com.example.asm_final.activities.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.asm_final.R;
import com.example.asm_final.activities.database.DatabaseHelper;

/*
 * Role: Fragment for App Shell, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public class CategoryFragment extends Fragment {

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_category, container, false);

    CardView cardMath = view.findViewById(R.id.cardMath);
    CardView cardScience = view.findViewById(R.id.cardScience);
    CardView cardHistory = view.findViewById(R.id.cardHistory);
    CardView cardIT = view.findViewById(R.id.cardIT);

    cardMath.setOnClickListener(v -> openMathModule());
    cardScience.setOnClickListener(v -> openScienceModule());
    cardHistory.setOnClickListener(v -> openHistoryModule());
    cardIT.setOnClickListener(v -> openProgrammingModule());

    return view;
  }

  /*
   * Responsibility: Displays or binds data from `open math module` to the appropriate UI or navigation destination.
   * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
   */
  private void openMathModule() {
    Context context = requireContext();
    com.example.asm_final.utils.SubjectNavigator.open(
        context,
        com.example.asm_final.utils.RecentActivity.Subject.MATH,
        resolveCurrentUserId(context));
  }

  /*
   * Responsibility: Displays or binds data from `open science module` to the appropriate UI or navigation destination.
   * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
   */
  private void openScienceModule() {
    Context context = requireContext();
    com.example.asm_final.utils.SubjectNavigator.open(
        context,
        com.example.asm_final.utils.RecentActivity.Subject.SCIENCE,
        resolveCurrentUserId(context));
  }

  /*
   * Responsibility: Displays or binds data from `open history module` to the appropriate UI or navigation destination.
   * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
   */
  private void openHistoryModule() {
    Context context = requireContext();
    com.example.asm_final.utils.SubjectNavigator.open(
        context,
        com.example.asm_final.utils.RecentActivity.Subject.HISTORY,
        resolveCurrentUserId(context));
  }

  /*
   * Responsibility: Displays or binds data from `open programming module` to the appropriate UI or navigation destination.
   * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
   */
  private void openProgrammingModule() {
    Context context = requireContext();
    com.example.asm_final.utils.SubjectNavigator.open(
        context,
        com.example.asm_final.utils.RecentActivity.Subject.PROGRAMMING,
        resolveCurrentUserId(context));
  }

  /*
   * Responsibility: Performs the `resolve current user id` responsibility of `CategoryFragment` and returns or updates the result for the caller.
   * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
   */
  private int resolveCurrentUserId(Context context) {
    Bundle arguments = getArguments();
    String username = arguments == null ? null : arguments.getString("USERNAME");
    if (username == null || username.trim().isEmpty()) {
      return 0;
    }

    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    return databaseHelper.getUserId(username);
  }
}
