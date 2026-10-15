package com.example.aimentor.science.presentation;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.R;
import com.example.aimentor.science.data.local.EncryptedScienceApiSettings;
import com.example.aimentor.science.di.ScienceModule;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceApiConnectionTester;
import com.example.aimentor.science.domain.service.ScienceApiSettings;
import com.example.aimentor.science.util.SciencePreferenceStore;

/** In-app Science API and display settings; no source-code editing is required. */
public final class ScienceSettingsActivity extends com.example.aimentor.ui.InsetActivity {
    private EditText apiKeyInput;
    private EditText modelInput;
    private CheckBox showKeyCheckBox;
    private CheckBox animationCheckBox;
    private TextView statusView;
    private ProgressBar progressBar;
    private Button testButton;
    private Button saveButton;
    private Button clearButton;

    private ScienceApiSettings apiSettings;
    private ScienceApiConnectionTester connectionTester;
    private SciencePreferenceStore preferenceStore;
    private boolean testing;
    private volatile boolean destroyed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_settings);

        ScienceModule module = ScienceModule.get(this);
        apiSettings = module.apiSettings();
        connectionTester = module.apiConnectionTester();
        preferenceStore = new SciencePreferenceStore(this);

        bindViews();
        loadSettings();
        setupActions();
    }

    private void bindViews() {
        apiKeyInput = findViewById(R.id.etScienceApiKey);
        modelInput = findViewById(R.id.etScienceModel);
        showKeyCheckBox = findViewById(R.id.cbShowScienceApiKey);
        animationCheckBox = findViewById(R.id.cbScienceAnimation);
        statusView = findViewById(R.id.tvScienceApiStatus);
        progressBar = findViewById(R.id.progressScienceApiTest);
        testButton = findViewById(R.id.btnTestScienceApi);
        saveButton = findViewById(R.id.btnSaveScienceSettings);
        clearButton = findViewById(R.id.btnClearScienceApi);
    }

    private void loadSettings() {
        modelInput.setText(apiSettings.getModelName());
        statusView.setTextColor(getColor(R.color.science_text_secondary));
        animationCheckBox.setChecked(preferenceStore.isAnimationEnabled());
        apiKeyInput.setHint(apiSettings.isConfigured()
                ? "API key saved — leave blank to keep it" : "Paste Gemini API key");
        statusView.setText(apiSettings.isConfigured()
                ? "An encrypted API key is saved on this device."
                : "No API key is saved. Chat, quiz and study planning use the local fallback until one is added.");
        clearButton.setEnabled(apiSettings.isConfigured());
    }

    private void setupActions() {
        findViewById(R.id.btnScienceSettingsBack).setOnClickListener(view -> finish());
        showKeyCheckBox.setOnCheckedChangeListener((button, checked) -> {
            int selection = apiKeyInput.getSelectionStart();
            apiKeyInput.setInputType(InputType.TYPE_CLASS_TEXT
                    | (checked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_TEXT_VARIATION_PASSWORD));
            apiKeyInput.setSelection(Math.max(0, Math.min(selection, apiKeyInput.length())));
        });
        testButton.setOnClickListener(view -> testConnection());
        saveButton.setOnClickListener(view -> saveSettings(true));
        clearButton.setOnClickListener(view -> confirmClear());
    }

    private String effectiveApiKey() {
        String entered = apiKeyInput.getText().toString().trim();
        if (!entered.isEmpty()) return entered;
        return apiSettings.isConfigured() ? apiSettings.getApiKey() : "";
    }

    private String effectiveModel() {
        String entered = modelInput.getText().toString().trim();
        String model = entered.isEmpty() ? EncryptedScienceApiSettings.DEFAULT_MODEL : entered;
        return model.matches("[A-Za-z0-9._-]{1,100}")
                ? model : EncryptedScienceApiSettings.DEFAULT_MODEL;
    }

    private void testConnection() {
        if (testing) return;
        String key = effectiveApiKey();
        if (key.isEmpty()) {
            apiKeyInput.setError("Enter an API key first.");
            apiKeyInput.requestFocus();
            return;
        }
        setTesting(true);
        statusView.setText("Testing the Science AI connection…");
        connectionTester.test(key, effectiveModel(), new ResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (destroyed) return;
                setTesting(false);
                statusView.setText(result + " You can now save these settings.");
                statusView.setTextColor(getColor(R.color.science_success));
            }

            @Override
            public void onError(String message, Throwable error) {
                if (destroyed) return;
                setTesting(false);
                statusView.setText(message == null ? "Connection failed." : message);
                statusView.setTextColor(getColor(R.color.science_error));
            }
        });
    }

    private void saveSettings(boolean finishAfterSave) {
        String enteredKey = apiKeyInput.getText().toString().trim();
        String model = effectiveModel();
        if (!model.equals(modelInput.getText().toString().trim())
                && !modelInput.getText().toString().trim().isEmpty()) {
            modelInput.setError("Use a valid Gemini model ID, for example gemini-3.5-flash.");
            modelInput.requestFocus();
            return;
        }

        try {
            if (!enteredKey.isEmpty()) {
                apiSettings.save(enteredKey, model);
            } else if (apiSettings.isConfigured()) {
                apiSettings.save(apiSettings.getApiKey(), model);
            }
        } catch (Exception error) {
            statusView.setTextColor(getColor(R.color.science_error));
            statusView.setText("The API key could not be encrypted on this device.");
            Toast.makeText(this, "Unable to save the Science API key securely.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        preferenceStore.setAnimationEnabled(animationCheckBox.isChecked());
        preferenceStore.setHighContrastControlsEnabled(true);
        statusView.setTextColor(getColor(R.color.science_success));
        statusView.setText(apiSettings.isConfigured()
                ? "Science API and display settings saved securely."
                : "Display settings saved. Chat, quiz and planning will use the local fallback until an API key is added.");
        Toast.makeText(this, "Science settings saved.", Toast.LENGTH_SHORT).show();
        if (finishAfterSave) finish();
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Science API key?")
                .setMessage("Science chat, quiz and planning will use the local fallback until another key is saved.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    try {
                        apiSettings.clear();
                        apiKeyInput.setText("");
                        loadSettings();
                        Toast.makeText(this, "Science API key removed.", Toast.LENGTH_SHORT).show();
                    } catch (Exception error) {
                        Toast.makeText(this, "Unable to remove the saved API key.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void setTesting(boolean value) {
        if (destroyed) return;
        testing = value;
        progressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        testButton.setEnabled(!value);
        saveButton.setEnabled(!value);
        clearButton.setEnabled(!value);
        apiKeyInput.setEnabled(!value);
        modelInput.setEnabled(!value);
    }
    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}
