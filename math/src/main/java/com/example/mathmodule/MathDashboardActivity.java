package com.example.mathmodule;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import com.example.mathmodule.fragments.MathFormulaFragment;
import com.example.mathmodule.fragments.MathHistoryFragment;
import com.example.mathmodule.fragments.MathQuizFragment;
import com.example.mathmodule.fragments.MathSolveFragment;

/*
 * Role: Activity for Math, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public class MathDashboardActivity extends AppCompatActivity {
    private static final String STATE_TAB = "math_current_tab";

    private int currentTab = 0;
    private LinearLayout[] navItems;
    private ImageView[] navIcons;
    private TextView[] navLabels;

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_dashboard);

        navItems = new LinearLayout[]{
                findViewById(R.id.nav_home),
                findViewById(R.id.nav_history),
                findViewById(R.id.nav_formula),
                findViewById(R.id.nav_quiz)
        };
        navIcons = new ImageView[]{
                findViewById(R.id.icon_nav_home),
                findViewById(R.id.icon_nav_history),
                findViewById(R.id.icon_nav_formula),
                findViewById(R.id.icon_nav_quiz)
        };
        navLabels = new TextView[]{
                findViewById(R.id.text_nav_home),
                findViewById(R.id.text_nav_history),
                findViewById(R.id.text_nav_formula),
                findViewById(R.id.text_nav_quiz)
        };

        currentTab = savedInstanceState == null ? 0 : savedInstanceState.getInt(STATE_TAB, 0);
        for (int i = 0; i < navItems.length; i++) {
            final int tab = i;
            navItems[i].setOnClickListener(v -> selectTab(tab));
        }

        updateNavigationHighlight(currentTab);
        if (savedInstanceState == null
                || getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            loadFragment(createFragment(currentTab));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            /*
             * Responsibility: Handles the `handle on back pressed` workflow or event and coordinates its related steps.
             * Purpose: A dedicated coordinator method makes the processing order clear and simplifies debugging and explanation.
             */
            @Override
            public void handleOnBackPressed() {
                if (currentTab != 0) {
                    selectTab(0);
                } else {
                    finish();
                }
            }
        });
    }

    /*
     * Responsibility: Performs the `select tab` responsibility of `MathDashboardActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void selectTab(int tab) {
        if (tab < 0 || tab >= navItems.length) tab = 0;
        if (currentTab == tab
                && getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) {
            updateNavigationHighlight(tab);
            return;
        }
        currentTab = tab;
        updateNavigationHighlight(tab);
        loadFragment(createFragment(tab));
    }

    /*
     * Responsibility: Creates the object or data structure for `create fragment` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private Fragment createFragment(int tab) {
        switch (tab) {
            case 1: return new MathHistoryFragment();
            case 2: return new MathFormulaFragment();
            case 3: return new MathQuizFragment();
            default: return new MathSolveFragment();
        }
    }

    /*
     * Responsibility: Updates or synchronizes `update navigation highlight` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateNavigationHighlight(int activeTab) {
        int[] activeColorRes = {
                R.color.math_nav_solve,
                R.color.math_nav_history,
                R.color.math_nav_notes,
                R.color.math_nav_quiz
        };
        int inactiveColor = ContextCompat.getColor(this, R.color.math_inactive);

        for (int i = 0; i < navItems.length; i++) {
            int color = i == activeTab ? ContextCompat.getColor(this, activeColorRes[i]) : inactiveColor;
            ImageViewCompat.setImageTintList(navIcons[i],
                    android.content.res.ColorStateList.valueOf(color));
            navLabels[i].setTextColor(color);
            navLabels[i].setTypeface(null, i == activeTab ? Typeface.BOLD : Typeface.NORMAL);
            navItems[i].setSelected(i == activeTab);
        }
    }

    /*
     * Responsibility: Reads or loads data for `load fragment` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /*
     * Responsibility: Handles the Android callback or event `onSaveInstanceState` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_TAB, currentTab);
        super.onSaveInstanceState(outState);
    }
}
