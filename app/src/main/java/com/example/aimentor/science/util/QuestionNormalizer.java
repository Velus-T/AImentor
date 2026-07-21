package com.example.aimentor.science.util;

import java.text.Normalizer;
import java.util.Locale;

public final class QuestionNormalizer {
    public String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return normalized;
    }
}
