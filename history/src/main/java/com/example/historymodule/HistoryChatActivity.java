package com.example.historymodule;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.historymodule.adapter.HistoryChatAdapter;
import com.example.historymodule.api.HistoryAiService;
import com.example.historymodule.databases.SqliteDbHelper;
import com.example.historymodule.util.HistoryImageReader;
import com.example.historymodule.util.HistoryUserSession;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Role: Activity for History, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public final class HistoryChatActivity extends AppCompatActivity {
    public static final String EXTRA_DRAFT = "history_draft";
    public static final String EXTRA_OPEN_CAMERA = "history_open_camera";
    public static final String EXTRA_FOCUS_INPUT = "history_focus_input";
    public static final String EXTRA_CONVERSATION_ID = "history_conversation_id";

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_CAMERA_CAPTURE = 101;
    private static final int REQUEST_PICK_IMAGE = 102;

    private static final String PREFS_NAME = "history_chat_preferences";
    private static final String PREF_CONVERSATION_ID = "active_conversation_id";

    private static final int MAX_CONTEXT_MESSAGES = 12;
    private static final int MAX_CONTEXT_CHARACTERS = 12_000;
    private static final int MAX_USER_CONTEXT_CHARACTERS = 1_600;
    private static final int MAX_ASSISTANT_CONTEXT_CHARACTERS = 3_000;

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton cameraButton;
    private ImageButton attachButton;
    private ImageView imagePreview;

    private final List<HistoryMessage> messages = new ArrayList<>();
    private HistoryChatAdapter adapter;
    private SqliteDbHelper dbHelper;
    private HistoryAiService aiService;
    private HistoryImageReader imageReader;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    private Uri selectedImageUri;
    private HistoryImageReader.ImageData cameraImageData;
    private String conversationId;
    private int userId;
    private boolean loading;
    private boolean destroyed;

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity_chat);

        userId = HistoryUserSession.getUserId(this);
        dbHelper = new SqliteDbHelper(this);
        aiService = new HistoryAiService();
        imageReader = new HistoryImageReader();
        conversationId = resolveConversationId();

        bindViews();
        com.example.mentor.ui.AiAvailabilityNotice.bind(findViewById(android.R.id.content), aiService.isConfigured());
        setupRecyclerView();
        setupActions();
        restoreConversation();
        if (savedInstanceState == null) {
            String draft = getIntent().getStringExtra(EXTRA_DRAFT);
            if (draft != null) messageEditText.setText(draft);
            handleLaunchMode();
        }
    }

    /*
     * Responsibility: Displays or binds data from `bind views` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void bindViews() {
        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        cameraButton = findViewById(R.id.btn_camera);
        attachButton = findViewById(R.id.btn_attach);
        imagePreview = findViewById(R.id.image_preview);
    }

    /*
     * Responsibility: Configures `setup recycler view` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupRecyclerView() {
        adapter = new HistoryChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /*
     * Responsibility: Configures `setup actions` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupActions() {
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
        findViewById(R.id.btn_new_chat).setOnClickListener(view -> confirmNewChat());
        sendButton.setOnClickListener(view -> sendCurrentMessage());
        cameraButton.setOnClickListener(view -> requestCamera());
        attachButton.setOnClickListener(view -> openGallery());
        imagePreview.setOnClickListener(view -> clearSelectedImage());
    }

    /*
     * Responsibility: Performs the `restore conversation` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void restoreConversation() {
        messages.clear();
        messages.addAll(dbHelper.loadChatMessages(userId, conversationId));
        if (messages.isEmpty()) {
            messages.add(new HistoryMessage(
                    getString(R.string.history_chat_welcome_msg),
                    HistoryMessage.SENT_BY_BOT
            ));
        }
        adapter.notifyDataSetChanged();
        scrollToBottom(false);
    }

    /*
     * Responsibility: Handles the `handle launch mode` workflow or event and coordinates its related steps.
     * Purpose: A dedicated coordinator method makes the processing order clear and simplifies debugging and explanation.
     */
    private void handleLaunchMode() {
        Intent intent = getIntent();
        if (intent == null) return;
        if (intent.getBooleanExtra(EXTRA_OPEN_CAMERA, false)) {
            recyclerView.postDelayed(this::requestCamera, 250L);
        } else if (intent.getBooleanExtra(EXTRA_FOCUS_INPUT, false)) {
            recyclerView.postDelayed(this::focusInput, 250L);
        }
    }

    /*
     * Responsibility: Performs the `resolve conversation id` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String resolveConversationId() {
        String requested = getIntent() == null ? null
                : getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String key = PREF_CONVERSATION_ID + "_" + userId;
        String id = requested == null ? preferences.getString(key, "") : requested.trim();
        if (id == null || id.trim().isEmpty()) id = UUID.randomUUID().toString();
        preferences.edit().putString(key, id).apply();
        return id;
    }

    /*
     * Responsibility: Performs the `confirm new chat` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void confirmNewChat() {
        if (loading) {
            Toast.makeText(this, R.string.history_chat_wait_ai, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.history_chat_confirm_new_title)
                .setMessage(R.string.history_chat_confirm_new_msg)
                .setPositiveButton(R.string.history_chat_btn_new_chat, (dialog, which) -> startNewConversation())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
     * Responsibility: Navigates to or starts the `start new conversation` flow and passes the required data to the next step.
     * Purpose: Centralizing navigation makes Intents, Fragments, and parameters between screens easier to control.
     */
    private void startNewConversation() {
        conversationId = UUID.randomUUID().toString();
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(PREF_CONVERSATION_ID + "_" + userId, conversationId)
                .apply();
        messages.clear();
        messages.add(new HistoryMessage(
                getString(R.string.history_chat_new_ready),
                HistoryMessage.SENT_BY_BOT
        ));
        adapter.notifyDataSetChanged();
        clearSelectedImage();
        focusInput();
    }

    /*
     * Responsibility: Prepares and executes the request for `send current message`, then returns the result or error to the caller.
     * Purpose: Separating AI, API, or I/O work from the UI centralizes asynchronous processing and error handling.
     */
    private void sendCurrentMessage() {
        if (loading) return;

        String typedText = messageEditText.getText().toString().trim();
        boolean hasImage = selectedImageUri != null || cameraImageData != null;
        if (typedText.isEmpty() && !hasImage) {
            Toast.makeText(this, R.string.history_chat_input_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!com.example.mentor.ui.AiAvailabilityNotice.canSend(findViewById(android.R.id.content), aiService.isConfigured())) return;
        String question = typedText.isEmpty()
                ? getString(R.string.history_chat_ai_prompt_photo)
                : typedText;
        String visibleText = typedText.isEmpty() ? getString(R.string.history_chat_visible_text_photo) : typedText;
        if (hasImage) visibleText += getString(R.string.history_chat_attached_label);
        String context = buildConversationContext();

        HistoryMessage userMessage = new HistoryMessage(
                visibleText,
                HistoryMessage.SENT_BY_ME,
                false,
                hasImage
        );
        HistoryMessage pendingMessage = HistoryMessage.pending();
        messages.add(userMessage);
        messages.add(pendingMessage);
        int insertedAt = messages.size() - 2;
        adapter.notifyItemRangeInserted(insertedAt, 2);
        scrollToBottom(true);

        dbHelper.saveChatMessage(
                userId,
                conversationId,
                HistoryMessage.SENT_BY_ME,
                visibleText,
                hasImage
        );

        messageEditText.setText("");
        setLoading(true);

        Uri imageUri = selectedImageUri;
        HistoryImageReader.ImageData capturedData = cameraImageData;
        clearSelectedImage();

        imageExecutor.execute(() -> {
            try {
                HistoryImageReader.ImageData imageData = capturedData != null
                        ? capturedData
                        : imageUri == null ? null : imageReader.readCompressed(this, imageUri);
                runOnUiThread(() -> executeAi(
                        question,
                        context,
                        imageData,
                        pendingMessage
                ));
            } catch (Exception error) {
                runOnUiThread(() -> replacePending(
                        pendingMessage,
                        safeMessage(error, getString(R.string.history_chat_error_read_image)),
                        true
                ));
            }
        });
    }

    /*
     * Responsibility: Performs the `execute ai` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void executeAi(String question,
                               String context,
                               @Nullable HistoryImageReader.ImageData imageData,
                               HistoryMessage pendingMessage) {
        aiService.answer(
                question,
                context,
                imageData == null ? null : imageData.getBytes(),
                imageData == null ? null : imageData.getMimeType(),
                new HistoryAiService.Callback<String>() {
                    /*
                     * Responsibility: Handles the Android callback or event `onSuccess` and updates the related state.
                     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
                     */
                    @Override
                    public void onSuccess(String answer) {
                        if (destroyed) return;
                        replacePending(pendingMessage, answer, false);
                        dbHelper.saveChatMessage(
                                userId,
                                conversationId,
                                HistoryMessage.SENT_BY_BOT,
                                answer,
                                false
                        );
                    }

                    /*
                     * Responsibility: Handles the Android callback or event `onError` and updates the related state.
                     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
                     */
                    @Override
                    public void onError(String message, Throwable error) {
                        if (destroyed) return;
                        replacePending(
                                pendingMessage,
                                message == null || message.trim().isEmpty()
                                        ? getString(R.string.history_chat_ai_error)
                                        : message,
                                true
                        );
                    }
                }
        );
    }

    /*
     * Responsibility: Performs the `replace pending` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void replacePending(HistoryMessage pendingMessage,
                                String text,
                                boolean error) {
        int index = messages.indexOf(pendingMessage);
        if (index >= 0) {
            pendingMessage.setMessage(error ? "⚠ " + text : text);
            pendingMessage.setPending(false);
            adapter.notifyItemChanged(index);
        }
        setLoading(false);
        scrollToBottom(true);
    }

    /*
     * Responsibility: Creates the object or data structure for `build conversation context` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private String buildConversationContext() {
        if (messages.isEmpty()) return "";

        LinkedList<String> blocks = new LinkedList<>();
        int usedCharacters = 0;
        int includedMessages = 0;

        for (int i = messages.size() - 1;
             i >= 0 && includedMessages < MAX_CONTEXT_MESSAGES;
             i--) {
            HistoryMessage message = messages.get(i);
            if (message == null || message.isPending()) continue;

            String text = message.getMessage() == null ? "" : message.getMessage().trim();
            if (text.isEmpty()) continue;
            boolean user = HistoryMessage.SENT_BY_ME.equals(message.getSentBy());
            int perMessageLimit = user
                    ? MAX_USER_CONTEXT_CHARACTERS
                    : MAX_ASSISTANT_CONTEXT_CHARACTERS;
            text = compactContextText(text, perMessageLimit);

            String label = user ? "NGƯỜI HỌC" : "GIA SƯ LỊCH SỬ";
            String block = label + ":\n" + text + "\n";
            int remaining = MAX_CONTEXT_CHARACTERS - usedCharacters;
            if (remaining < 120) break;
            if (block.length() > remaining) {
                text = compactContextText(text, Math.max(80, remaining - label.length() - 4));
                block = label + ":\n" + text + "\n";
            }
            blocks.addFirst(block);
            usedCharacters += block.length();
            includedMessages++;
        }

        if (blocks.isEmpty()) return "";
        StringBuilder context = new StringBuilder(
                "Bản ghi theo thứ tự thời gian; phần cuối là lượt hoàn chỉnh gần nhất.\n");
        for (String block : blocks) {
            context.append("---\n").append(block);
        }
        return context.toString().trim();
    }

    /*
     * Responsibility: Performs the `compact context text` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String compactContextText(String text, int maximumCharacters) {
        if (text == null) return "";
        String normalized = text.trim();
        if (normalized.length() <= maximumCharacters) return normalized;
        if (maximumCharacters < 120) {
            return normalized.substring(0, Math.max(1, maximumCharacters));
        }
        int head = (int) (maximumCharacters * 0.62);
        int tail = maximumCharacters - head - 24;
        return normalized.substring(0, head).trim()
                + "\n… [đã rút gọn phần giữa] …\n"
                + normalized.substring(normalized.length() - tail).trim();
    }

    /*
     * Responsibility: Applies or updates `set loading` in the current state or UI.
     * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
     */
    private void setLoading(boolean value) {
        loading = value;
        sendButton.setEnabled(!value);
        cameraButton.setEnabled(!value);
        attachButton.setEnabled(!value);
        messageEditText.setEnabled(!value);
        sendButton.setAlpha(value ? 0.45f : 1f);
    }

    /*
     * Responsibility: Prepares and executes the request for `request camera`, then returns the result or error to the caller.
     * Purpose: Separating AI, API, or I/O work from the UI centralizes asynchronous processing and error handling.
     */
    private void requestCamera() {
        if (loading) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
        } else {
            openCamera();
        }
    }

    /*
     * Responsibility: Displays or binds data from `open camera` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA_CAPTURE);
        } else {
            Toast.makeText(this, R.string.history_chat_device_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Responsibility: Displays or binds data from `open gallery` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void openGallery() {
        if (loading) return;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    /*
     * Responsibility: Handles the Android callback or event `onActivityResult` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQUEST_CAMERA_CAPTURE) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = extras == null ? null : (Bitmap) extras.get("data");
            if (bitmap == null) {
                Toast.makeText(this, R.string.history_chat_no_camera_image, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                cameraImageData = imageReader.fromBitmap(bitmap);
                selectedImageUri = null;
                imagePreview.setImageBitmap(bitmap);
                imagePreview.setVisibility(View.VISIBLE);
            } catch (Exception error) {
                clearSelectedImage();
                Toast.makeText(this, safeMessage(error, getString(R.string.history_chat_error_camera_process)),
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_PICK_IMAGE) {
            Uri uri = data.getData();
            if (uri == null) return;
            selectedImageUri = uri;
            cameraImageData = null;
            imagePreview.setImageURI(uri);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }

    /*
     * Responsibility: Handles the Android callback or event `onRequestPermissionsResult` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, R.string.history_chat_permission_camera, Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
     * Responsibility: Clears or resets the data and state related to `clear selected image`.
     * Purpose: Keeping deletion logic separate helps ensure related UI and data updates are not missed.
     */
    private void clearSelectedImage() {
        selectedImageUri = null;
        cameraImageData = null;
        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);
    }

    /*
     * Responsibility: Performs the `focus input` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void focusInput() {
        messageEditText.requestFocus();
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /*
     * Responsibility: Performs the `scroll to bottom` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void scrollToBottom(boolean smooth) {
        if (messages.isEmpty()) return;
        int position = messages.size() - 1;
        if (smooth) recyclerView.smoothScrollToPosition(position);
        else recyclerView.scrollToPosition(position);
    }

    /*
     * Responsibility: Performs the `safe message` responsibility of `HistoryChatActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String safeMessage(Throwable error, String fallback) {
        String detail = error == null ? "" : error.getMessage();
        return detail == null || detail.trim().isEmpty() ? fallback : detail.trim();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        imageExecutor.shutdownNow();
        if (aiService != null) aiService.shutdown();
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }
}
