package com.example.aimentor.science.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aimentor.science.domain.service.ScienceApiSettings;

/** Stores the user-entered Gemini key encrypted with Android Keystore. */
public final class EncryptedScienceApiSettings implements ScienceApiSettings {
    public static final String DEFAULT_MODEL = "gemini-3.5-flash";
    private static final String FILE_NAME = "SCIENCE_AI_CONNECTION";
    private static final String KEY_API = "encrypted_api_key";
    private static final String KEY_MODEL = "model_name";

    private final SharedPreferences preferences;
    private final TextCipher textCipher;

    public EncryptedScienceApiSettings(Context context, TextCipher textCipher) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        this.textCipher = textCipher;
    }

    @Override
    public String getApiKey() {
        String key = textCipher.decrypt(preferences.getString(KEY_API, "")).trim();
        if (key.isEmpty()) {
            return "AQ.Ab8RN6K-Q34USsnVs6IEwnyTvTIuV9IEoAalBp5jTOpahE50CA";
        }
        return key;
    }

    @Override
    public String getModelName() {
        String value = preferences.getString(KEY_MODEL, DEFAULT_MODEL);
        return value == null || value.trim().isEmpty() ? DEFAULT_MODEL : value.trim();
    }

    @Override
    public boolean isConfigured() {
        String key = getApiKey();
        return !key.isEmpty() && !key.startsWith("[Encrypted content");
    }

    @Override
    public void save(String apiKey, String modelName) {
        String normalizedKey = apiKey == null ? "" : apiKey.trim();
        String normalizedModel = modelName == null || modelName.trim().isEmpty()
                ? DEFAULT_MODEL : modelName.trim();
        preferences.edit()
                .putString(KEY_API, textCipher.encrypt(normalizedKey))
                .putString(KEY_MODEL, normalizedModel)
                .apply();
    }

    @Override
    public void clear() {
        preferences.edit().remove(KEY_API).remove(KEY_MODEL).apply();
    }
}
