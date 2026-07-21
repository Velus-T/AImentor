package com.example.aimentor.science.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ScienceDateFormatter {
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public String format(long timestamp) {
        return formatter.format(new Date(timestamp));
    }
}
