package com.example.aimentor.science.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class QuestionFingerprint {
    public String create(String normalizedQuestion) {
        if (normalizedQuestion == null || normalizedQuestion.isEmpty()) return "";
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalizedQuestion.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value & 0xff));
            return result.toString();
        } catch (Exception error) {
            throw new IllegalStateException("Unable to create question fingerprint.", error);
        }
    }
}
