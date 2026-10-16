package com.brightpath.science.presentation;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.brightpath.science.R;

/**
 * Compatibility entry point for code that still opens the old History Activity.
 * The actual History UI now lives inside ScienceMainActivity as a bottom-navigation tab.
 */
public final class ScienceHistoryActivity extends AppCompatActivity {

        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, ScienceMainActivity.class);
        intent.putExtra(
                ScienceMainActivity.EXTRA_SELECTED_TAB,
                R.id.navScienceHistory
        );
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }
}
