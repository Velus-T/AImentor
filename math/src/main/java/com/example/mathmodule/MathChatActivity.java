package com.example.mathmodule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathmodule.adapter.MathChatAdapter;
import com.example.mathmodule.api.MathGeminiApiClient;
import com.example.mathmodule.databases.SqliteDbHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Role: Activity for Math, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public class MathChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtQuestion;
    private ImageButton btnSend;
    private ImageButton btnUploadImage;
    private MathChatAdapter chatAdapter;
    private List<MathMessage> messageList;
    private SqliteDbHelper dbHelper;

    private byte[] currentImageBytes;
    private String currentImageMimeType = "image/jpeg";

    private MathGeminiApiClient aiClient;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                Uri selectedImageUri = result.getData().getData();
                currentImageBytes = convertUriToJpegBytes(selectedImageUri);
                currentImageMimeType = "image/jpeg";

                if (currentImageBytes != null && currentImageBytes.length > 0) {
                    Toast.makeText(
                            this,
                            R.string.math_chat_attached_toast,
                            Toast.LENGTH_LONG
                    ).show();
                    edtQuestion.setHint(R.string.math_chat_hint_attached);
                } else {
                    Toast.makeText(this, R.string.math_chat_error_image, Toast.LENGTH_SHORT).show();
                }
            }
    );

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_chat);

        dbHelper = new SqliteDbHelper(this);
        aiClient = new MathGeminiApiClient();

        recyclerView = findViewById(R.id.math_recycler_view);
        edtQuestion = findViewById(R.id.math_edt_question);
        btnSend = findViewById(R.id.math_btn_send);
        btnUploadImage = findViewById(R.id.math_btn_upload_image);

        messageList = new ArrayList<>();
        chatAdapter = new MathChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        addMessage(
                getString(R.string.math_chat_welcome),
                false
        );

        String actionType = getIntent().getStringExtra("ACTION_TYPE");
        if ("TYPE_TEXT".equals(actionType)) {
            edtQuestion.requestFocus();
        }

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        if (savedInstanceState == null && "OPEN_IMAGE".equals(actionType)) {
            btnUploadImage.performClick();
        }

        com.example.mentor.ui.AiAvailabilityNotice.bind(findViewById(android.R.id.content), aiClient.isConfigured());
        btnSend.setOnClickListener(v -> sendQuestion());
    }

    /*
     * Responsibility: Prepares and executes the request for `send question`, then returns the result or error to the caller.
     * Purpose: Separating AI, API, or I/O work from the UI centralizes asynchronous processing and error handling.
     */
    private void sendQuestion() {
        String typedQuestion = edtQuestion.getText().toString().trim();
        if (typedQuestion.isEmpty() && currentImageBytes == null) {
            return;
        }

        if (!com.example.mentor.ui.AiAvailabilityNotice.canSend(findViewById(android.R.id.content), aiClient.isConfigured())) return;
        String finalQuestion = typedQuestion.isEmpty()
                ? getString(R.string.math_chat_default_image_query)
                : typedQuestion;
        byte[] imageForRequest = currentImageBytes;
        String mimeTypeForRequest = currentImageMimeType;

        String displayMessage = imageForRequest != null
                ? getString(R.string.math_chat_sent_image_label) + finalQuestion
                : finalQuestion;
        addMessage(displayMessage, true);

        edtQuestion.setText("");
        edtQuestion.setHint(R.string.math_chat_hint_default);
        currentImageBytes = null;
        currentImageMimeType = "image/jpeg";

        callGeminiApi(finalQuestion, imageForRequest, mimeTypeForRequest);
    }

    /*
     * Responsibility: Converts or normalizes data in `convert uri to jpeg bytes` into the required format.
     * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
     */
    private byte[] convertUriToJpegBytes(Uri uri) {
        if (uri == null) {
            return null;
        }
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                return null;
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                return compressed ? outputStream.toByteArray() : null;
            }
        } catch (Exception error) {
            return null;
        }
    }

    /*
     * Responsibility: Saves or adds data for `add message` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    private void addMessage(String text, boolean isUser) {
        runOnUiThread(() -> {
            messageList.add(new MathMessage(text, isUser));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.smoothScrollToPosition(messageList.size() - 1);
        });
    }

    /*
     * Responsibility: Prepares and executes the request for `call gemini api`, then returns the result or error to the caller.
     * Purpose: Separating AI, API, or I/O work from the UI centralizes asynchronous processing and error handling.
     */
    private void callGeminiApi(String question, byte[] imageBytes, String imageMimeType) {
        String prompt = getString(R.string.math_chat_ai_prefix) + question;

        btnSend.setEnabled(false);
        aiExecutor.execute(() -> {
            try {
                MathGeminiApiClient.Result result = aiClient.generateText(
                        prompt,
                        imageBytes,
                        imageMimeType,
                        0.2,
                        8192
                );
                String aiAnswer = result.text;
                addMessage(aiAnswer, false);

                if (!isFinishing() && !isDestroyed() && dbHelper != null && !aiAnswer.trim().isEmpty()) {
                    dbHelper.insertHistory(question, aiAnswer);
                }
            } catch (Exception error) {
                String message = MathGeminiApiClient.toUserMessage(
                        error,
                        getString(R.string.math_chat_error_api)
                );
                addMessage(message, false);
            } finally {
                runOnUiThread(() -> btnSend.setEnabled(true));
            }
        });
    }

    /*
     * Responsibility: Handles the Android callback or event `onDestroy` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    protected void onDestroy() {
        aiExecutor.shutdownNow();
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
