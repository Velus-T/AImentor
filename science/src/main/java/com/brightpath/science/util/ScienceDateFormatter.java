package com.brightpath.science.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ScienceDateFormatter {
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        /*
 * Responsibility: Normalizes or formats the value handled by `format` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
public String format(long timestamp) {
        return formatter.format(new Date(timestamp));
    }
}
