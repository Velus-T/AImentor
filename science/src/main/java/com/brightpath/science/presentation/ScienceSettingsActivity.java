package com.brightpath.science.presentation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.brightpath.science.R;
import com.brightpath.science.data.remote.GeminiApiClient;
import com.brightpath.science.data.remote.GeminiScienceService;
import com.brightpath.science.util.SciencePreferenceStore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Science-only display settings and a read-only Gemini connection test. */
public final class ScienceSettingsActivity extends AppCompatActivity {
    private CheckBox animationCheckBox;
    private TextView providerStatusView;
    private Button testConnectionButton;
    private SciencePreferenceStore preferenceStore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_settings);

        preferenceStore = new SciencePreferenceStore(this);
        findViewById(R.id.btnScienceSettingsBack).setOnClickListener(view -> finish());

        animationCheckBox = findViewById(R.id.cbScienceAnimation);
        animationCheckBox.setChecked(preferenceStore.isAnimationEnabled());

        providerStatusView = findViewById(R.id.tvScienceProviderStatus);
        testConnectionButton = findViewById(R.id.btnTestGeminiConnection);
        showConfiguredStatus();

        testConnectionButton.setOnClickListener(view -> testGeminiConnection());

        Button saveButton = findViewById(R.id.btnSaveScienceDisplaySettings);
        saveButton.setOnClickListener(view -> {
            preferenceStore.setAnimationEnabled(animationCheckBox.isChecked());
            Toast.makeText(this, "Science display settings saved.", Toast.LENGTH_SHORT).show();
        });
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showConfiguredStatus` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showConfiguredStatus() {
        GeminiApiClient client = new GeminiApiClient();
        providerStatusView.setText(client.isConfigured()
                ? "Configured • " + client.getModelName() + "\nTap Test Gemini connection to verify the key."
                : "Gemini key is not configured in root api.properties");
    }

        /*
 * Responsibility: Handles the `testGeminiConnection` UI interaction/state operation for ScienceSettingsActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void testGeminiConnection() {
        testConnectionButton.setEnabled(false);
        providerStatusView.setText("Testing Gemini connection…");

        executor.execute(() -> {
            try {
                GeminiApiClient client = new GeminiApiClient();
                String response = client.testConnection().text;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    providerStatusView.setText("Gemini connection successful • "
                            + new GeminiApiClient().getModelName() + "\nResponse: " + response);
                    testConnectionButton.setEnabled(true);
                });
            } catch (Exception error) {
                String message = GeminiScienceService.userMessage(
                        error, "Gemini connection failed."
                );
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    providerStatusView.setText(message);
                    testConnectionButton.setEnabled(true);
                });
            }
        });
    }

        /*
 * Responsibility: Releases temporary resources, listeners, or component state during destruction.
 * Purpose: Reduces leaks and prevents stale callbacks from affecting a destroyed Science screen.
 */
@Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
