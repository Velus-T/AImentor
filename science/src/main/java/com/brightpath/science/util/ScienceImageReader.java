package com.brightpath.science.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** Safely decodes and compresses homework images before preview/upload. */
public final class ScienceImageReader {
    private static final int MAX_SIDE = 1280;
    private static final int MAX_COMPRESSED_BYTES = 5 * 1024 * 1024;

        /*
 * Responsibility: Performs the `readCompressed` operation for ScienceImageReader within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public ImageData readCompressed(Context context, Uri uri) throws Exception {
        if (context == null || uri == null) {
            throw new IllegalArgumentException("No image was selected.");
        }
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        String mimeType = resolver.getType(uri);
        if (mimeType == null || !mimeType.startsWith("image/")) mimeType = "image/jpeg";

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) throw new IllegalArgumentException("Unable to open the selected image.");
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IllegalArgumentException("The selected file is not a valid image.");
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, MAX_SIDE * 2);
        decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap decoded;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) throw new IllegalArgumentException("Unable to reopen the selected image.");
            decoded = BitmapFactory.decodeStream(input, null, decodeOptions);
        } catch (OutOfMemoryError error) {
            throw new IllegalArgumentException("This image is too large for the device. Choose a smaller image.");
        }
        if (decoded == null) throw new IllegalArgumentException("The selected file is not a valid image.");

        Bitmap scaled = decoded;
        int maxSide = Math.max(decoded.getWidth(), decoded.getHeight());
        if (maxSide > MAX_SIDE) {
            float scale = MAX_SIDE / (float) maxSide;
            try {
                scaled = Bitmap.createScaledBitmap(
                        decoded,
                        Math.max(1, Math.round(decoded.getWidth() * scale)),
                        Math.max(1, Math.round(decoded.getHeight() * scale)),
                        true
                );
            } catch (OutOfMemoryError error) {
                decoded.recycle();
                throw new IllegalArgumentException("This image is too large for the device. Choose a smaller image.");
            }
            if (scaled != decoded) decoded.recycle();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int quality = 84;
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, output);
            while (output.size() > MAX_COMPRESSED_BYTES && quality > 52) {
                output.reset();
                quality -= 8;
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, output);
            }
            if (output.size() > MAX_COMPRESSED_BYTES) {
                throw new IllegalArgumentException("The compressed image is still too large. Crop it and try again.");
            }
            return new ImageData(output.toByteArray(), "image/jpeg");
        } finally {
            scaled.recycle();
        }
    }

        /*
 * Responsibility: Calculates the value represented by `calculateSampleSize` from the supplied Science learning data.
 * Purpose: Keeps the calculation rule deterministic and reusable outside the UI.
 */
private int calculateSampleSize(int width, int height, int targetSide) {
        int sample = 1;
        int max = Math.max(width, height);
        while (max / sample > targetSide && sample < 16) sample *= 2;
        return sample;
    }

    public static final class ImageData {
        private final byte[] bytes;
        private final String mimeType;

                /*
 * Responsibility: Initializes ImageData and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ImageData(byte[] bytes, String mimeType) {
            this.bytes = bytes == null ? new byte[0] : bytes;
            this.mimeType = mimeType == null ? "image/jpeg" : mimeType;
        }

                /*
 * Responsibility: Retrieves the data/state required by `getBytes` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public byte[] getBytes() { return bytes; }
                /*
 * Responsibility: Retrieves the data/state required by `getMimeType` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getMimeType() { return mimeType; }
    }
}
