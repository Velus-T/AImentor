package com.brightpath.science.data.local;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;

public final class ScienceImageStore {
    private final File directory;

        /*
 * Responsibility: Initializes ScienceImageStore and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceImageStore(Context context) {
        directory = new File(context.getFilesDir(), "science_images");
        if (!directory.exists()) {
            // Best effort here; saveJpeg reports a clear error if storage is unavailable.
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
    }

        /*
 * Responsibility: Persists the data supplied to `saveJpeg` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
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
