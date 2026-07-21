package com.example.aimentor.science.data.local;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;

public final class ScienceImageStore {
    private final File directory;

    public ScienceImageStore(Context context) {
        directory = new File(context.getFilesDir(), "science_images");
        if (!directory.exists()) {
            // Best effort here; saveJpeg reports a clear error if storage is unavailable.
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
    }

    public String saveJpeg(byte[] bytes) throws Exception {
        if (bytes == null || bytes.length == 0) return null;
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IllegalStateException("Private Science image storage is unavailable.");
        }
        File target = new File(directory, "science_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream output = new FileOutputStream(target)) {
            output.write(bytes);
        }
        return Uri.fromFile(target).toString();
    }
}
