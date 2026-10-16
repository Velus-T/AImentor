package com.brightpath.science.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.brightpath.science.R;

/** Creates consistent, high-contrast controls used only inside the Science module. */
public final class ScienceUiFactory {
        /*
 * Responsibility: Initializes ScienceUiFactory and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private ScienceUiFactory() { }

        /*
 * Responsibility: Performs the `spinnerAdapter` operation for ScienceUiFactory within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public static ArrayAdapter<String> spinnerAdapter(Context context, String[] values) {
        String[] safeValues = values == null ? new String[0] : values;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, R.layout.item_science_spinner_selected, safeValues);
        adapter.setDropDownViewResource(R.layout.item_science_spinner_dropdown);
        return adapter;
    }
}
