package com.example.aimentor.science.presentation;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimentor.R;
import com.example.aimentor.science.data.local.ScienceImageStore;
import com.example.aimentor.science.di.ScienceModule;
import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ExplanationStyle;
import com.example.aimentor.science.domain.model.ScienceAnswer;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceApiSettings;
import com.example.aimentor.science.domain.usecase.AskScienceQuestionUseCase;
import com.example.aimentor.science.domain.usecase.SaveBookmarkUseCase;
import com.example.aimentor.science.presentation.chat.ScienceChatAdapter;
import com.example.aimentor.science.presentation.chat.ScienceChatMessage;
import com.example.aimentor.science.presentation.view.ScienceAnimatedBackgroundView;
import com.example.aimentor.science.util.ScienceImageReader;
import com.example.aimentor.science.util.SciencePreferenceStore;
import com.example.aimentor.science.util.ScienceUserSession;
import com.example.aimentor.science.util.ScienceUiFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** ChatGPT-style Science chat with separate Physics, Chemistry and Biology sessions. */
public final class ScienceActivity extends AppCompatActivity {
    private static final int MAX_CONTEXT_MESSAGES = 8;
    private static final int MAX_CONTEXT_CHARACTERS = 7000;
    private static final String STATE_SUBJECT = "science_chat_subject";
    private static final String STATE_CONVERSATIONS = "science_chat_conversations";
    private static final String STATE_SELECTED_IMAGE = "science_chat_selected_image";
    private static final String STATE_INPUT = "science_chat_input";
    private static final String STATE_TOOLS_VISIBLE = "science_chat_tools_visible";

    private RadioGroup subjectGroup;
    private Spinner educationSpinner;
    private Spinner styleSpinner;
    private EditText questionInput;
    private ImageView imagePreview;
    private View selectedImageLayout;
    private View emptyState;
    private View toolsPanel;
    private TextView subjectStatus;
    private Button askButton;
    private Button clearImageButton;
    private Button pickImageButton;
    private Button takePhotoButton;
    private Button newChatButton;
    private Button apiSettingsButton;
    private RecyclerView chatRecyclerView;
    private ScienceAnimatedBackgroundView animatedBackground;

    private final Map<ScienceSubject, List<ScienceChatMessage>> conversations =
            new EnumMap<>(ScienceSubject.class);
    private List<ScienceChatMessage> messages;
    private ScienceSubject activeSubject = ScienceSubject.PHYSICS;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private final ScienceImageReader imageReader = new ScienceImageReader();

    private ScienceChatAdapter chatAdapter;
    private AskScienceQuestionUseCase askUseCase;
    private SaveBookmarkUseCase bookmarkUseCase;
    private ScienceUserSession userSession;
    private SciencePreferenceStore preferenceStore;
    private ScienceImageStore privateImageStore;
    private ScienceApiSettings apiSettings;

    private Uri selectedImageUri;
    private Uri pendingCameraUri;
    private File pendingCameraFile;
    private boolean loading;
    private boolean suppressSubjectCallback;
    private volatile boolean destroyed;

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // Some providers grant temporary access only; the image is copied on send.
                }
                showSelectedImage(uri);
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingCameraUri != null) {
                    showSelectedImage(pendingCameraUri);
                } else if (pendingCameraUri != null) {
                    deletePendingCameraFile();
                    pendingCameraUri = null;
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
                else Toast.makeText(this,
                        "Camera permission was not granted. You can still choose an image.",
                        Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science);

        ScienceModule module = ScienceModule.get(this);
        askUseCase = module.askScienceQuestionUseCase();
        bookmarkUseCase = module.saveBookmarkUseCase();
        userSession = new ScienceUserSession(this);
        preferenceStore = new SciencePreferenceStore(this);
        privateImageStore = new ScienceImageStore(this);
        apiSettings = module.apiSettings();

        initializeConversations();
        restoreConversationState(savedInstanceState);
        bindViews();
        setupConversation();
        setupSpinners();
        setupNavigation();
        setupQuestionActions();
        setupSuggestions();
        restoreViewState(savedInstanceState);
        switchConversation(activeSubject, false);
    }

    private void initializeConversations() {
        for (ScienceSubject subject : ScienceSubject.values()) {
            conversations.put(subject, new ArrayList<>());
        }
        activeSubject = preferenceStore == null
                ? ScienceSubject.PHYSICS : preferenceStore.getSubject();
        messages = conversations.get(activeSubject);
    }

    private void bindViews() {
        subjectGroup = findViewById(R.id.rgScienceSubject);
        educationSpinner = findViewById(R.id.spEducationLevel);
        styleSpinner = findViewById(R.id.spExplanationStyle);
        questionInput = findViewById(R.id.etScienceQuestion);
        imagePreview = findViewById(R.id.ivSciencePreview);
        selectedImageLayout = findViewById(R.id.layoutSelectedImage);
        emptyState = findViewById(R.id.layoutChatEmptyState);
        toolsPanel = findViewById(R.id.panelChatTools);
        subjectStatus = findViewById(R.id.tvChatSubjectStatus);
        askButton = findViewById(R.id.btnAskScience);
        clearImageButton = findViewById(R.id.btnClearImage);
        pickImageButton = findViewById(R.id.btnPickImage);
        takePhotoButton = findViewById(R.id.btnTakePhoto);
        newChatButton = findViewById(R.id.btnNewScienceChat);
        chatRecyclerView = findViewById(R.id.rvScienceChat);
        apiSettingsButton = findViewById(R.id.btnScienceApiSettings);
        animatedBackground = findViewById(R.id.scienceAnimatedBackground);
    }

    private void setupConversation() {
        chatAdapter = new ScienceChatAdapter(messages, new ScienceChatAdapter.MessageActionListener() {
            @Override
            public void onBookmarkClicked(ScienceChatMessage message, int position) {
                toggleBookmark(message, position);
            }

            @Override
            public void onCopyClicked(ScienceChatMessage message) {
                copyAnswer(message.getText());
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupSpinners() {
        String[] levels = new String[EducationLevel.values().length];
        for (int i = 0; i < levels.length; i++) levels[i] = EducationLevel.values()[i].getDisplayName();
        educationSpinner.setAdapter(scienceSpinnerAdapter(levels));

        String[] styles = new String[ExplanationStyle.values().length];
        for (int i = 0; i < styles.length; i++) styles[i] = ExplanationStyle.values()[i].getDisplayName();
        styleSpinner.setAdapter(scienceSpinnerAdapter(styles));

        educationSpinner.setSelection(preferenceStore.getLevelPosition(), false);
        styleSpinner.setSelection(preferenceStore.getStylePosition(), false);

        suppressSubjectCallback = true;
        subjectGroup.check(subjectButtonId(activeSubject));
        suppressSubjectCallback = false;

        subjectGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (suppressSubjectCallback) return;
            switchConversation(subjectFromButtonId(checkedId), true);
        });

        AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                persistCurrentPreferences();
                updatePreferenceStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updatePreferenceStatus();
            }
        };
        educationSpinner.setOnItemSelectedListener(selectionListener);
        styleSpinner.setOnItemSelectedListener(selectionListener);
    }

    private android.widget.ArrayAdapter<String> scienceSpinnerAdapter(String[] values) {
        return ScienceUiFactory.spinnerAdapter(this, values);
    }

    private void setupNavigation() {
        findViewById(R.id.btnScienceBack).setOnClickListener(view -> finish());
        newChatButton.setOnClickListener(view -> startNewChat());
        findViewById(R.id.btnChatTools).setOnClickListener(view ->
                toolsPanel.setVisibility(toolsPanel.getVisibility() == View.VISIBLE
                        ? View.GONE : View.VISIBLE));

        findViewById(R.id.btnScienceHistory).setOnClickListener(view ->
                safeStartActivity(ScienceHistoryActivity.class));
        findViewById(R.id.btnScienceBookmarks).setOnClickListener(view ->
                safeStartActivity(ScienceBookmarksActivity.class));
        findViewById(R.id.btnScienceQuiz).setOnClickListener(view ->
                safeStartActivity(ScienceQuizActivity.class));
        findViewById(R.id.btnSciencePlanner).setOnClickListener(view ->
                safeStartActivity(ScienceStudyPlannerActivity.class));
        findViewById(R.id.btnPeriodicTable).setOnClickListener(view ->
                safeStartActivity(PeriodicTableActivity.class));
        findViewById(R.id.btnCircuitDiagram).setOnClickListener(view ->
                safeStartActivity(CircuitDiagramActivity.class));
        findViewById(R.id.btnScienceProgress).setOnClickListener(view ->
                safeStartActivity(ScienceProgressActivity.class));
        apiSettingsButton.setOnClickListener(view -> safeStartActivity(ScienceSettingsActivity.class));
    }

    private void safeStartActivity(Class<?> activityClass) {
        try {
            startActivity(new Intent(this, activityClass));
        } catch (Exception error) {
            Toast.makeText(this, "Unable to open this Science tool.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupQuestionActions() {
        pickImageButton.setOnClickListener(view -> {
            if (!loading) openDocumentLauncher.launch(new String[]{"image/*"});
        });
        takePhotoButton.setOnClickListener(view -> requestOrLaunchCamera());
        clearImageButton.setOnClickListener(view -> clearImage());
        askButton.setOnClickListener(view -> askQuestion());
        questionInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                askQuestion();
                return true;
            }
            return false;
        });
    }

    private void setupSuggestions() {
        findViewById(R.id.btnSuggestionPhenomenon).setOnClickListener(view ->
                useSuggestion("Why does the sky look blue?"));
        findViewById(R.id.btnSuggestionChemistry).setOnClickListener(view -> {
            selectSubject(ScienceSubject.CHEMISTRY);
            useSuggestion("Explain and balance: H2 + O2 → H2O");
        });
        findViewById(R.id.btnSuggestionBiology).setOnClickListener(view -> {
            selectSubject(ScienceSubject.BIOLOGY);
            useSuggestion("How does photosynthesis work?");
        });
    }

    private void useSuggestion(String suggestion) {
        questionInput.setText(suggestion);
        questionInput.setSelection(suggestion.length());
        questionInput.requestFocus();
    }

    private void selectSubject(ScienceSubject subject) {
        if (loading || subject == null || subject == activeSubject) return;
        suppressSubjectCallback = true;
        subjectGroup.check(subjectButtonId(subject));
        suppressSubjectCallback = false;
        switchConversation(subject, true);
    }

    private void switchConversation(ScienceSubject subject, boolean announce) {
        activeSubject = subject == null ? ScienceSubject.PHYSICS : subject;
        List<ScienceChatMessage> selectedMessages = conversations.get(activeSubject);
        if (selectedMessages == null) {
            selectedMessages = new ArrayList<>();
            conversations.put(activeSubject, selectedMessages);
        }
        messages = selectedMessages;
        if (chatAdapter != null) chatAdapter.submitMessages(messages);
        if (animatedBackground != null) {
            animatedBackground.setSubject(activeSubject);
            animatedBackground.setAnimationEnabled(preferenceStore.isAnimationEnabled());
        }
        persistCurrentPreferences();
        updatePreferenceStatus();
        updateEmptyState();
        scrollToBottom(false);
        if (announce) {
            Toast.makeText(this,
                    activeSubject.getDisplayName() + " tutor selected. Its chat is kept separate.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void persistCurrentPreferences() {
        if (preferenceStore == null || educationSpinner == null || styleSpinner == null) return;
        preferenceStore.save(activeSubject,
                educationSpinner.getSelectedItemPosition(),
                styleSpinner.getSelectedItemPosition());
    }

    private void requestOrLaunchCamera() {
        if (loading) return;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "This device does not report an available camera.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File directory = new File(getCacheDir(), "science_camera");
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IllegalStateException("Camera cache directory could not be created.");
            }
            pendingCameraFile = File.createTempFile(
                    "science_question_" + System.currentTimeMillis() + "_", ".jpg", directory);
            pendingCameraUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".science.fileprovider", pendingCameraFile);
            takePhotoLauncher.launch(pendingCameraUri);
        } catch (Exception error) {
            deletePendingCameraFile();
            pendingCameraUri = null;
            Toast.makeText(this, "Unable to open the camera on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private void deletePendingCameraFile() {
        if (pendingCameraFile != null && pendingCameraFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            pendingCameraFile.delete();
        }
        pendingCameraFile = null;
    }

    private void showSelectedImage(Uri uri) {
        if (uri == null) return;
        selectedImageUri = uri;
        selectedImageLayout.setVisibility(View.VISIBLE);
        imagePreview.setImageDrawable(null);
        imageExecutor.execute(() -> {
            try {
                ScienceImageReader.ImageData previewData = imageReader.readCompressed(this, uri);
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        previewData.getBytes(), 0, previewData.getBytes().length);
                if (bitmap == null) throw new IllegalArgumentException("Invalid image");
                runOnUiThread(() -> {
                    if (!destroyed && uri.equals(selectedImageUri)) imagePreview.setImageBitmap(bitmap);
                    else bitmap.recycle();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (destroyed) return;
                    clearImage();
                    Toast.makeText(this, "The selected image could not be opened.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void clearImage() {
        clearImage(true);
    }

    private void clearImage(boolean deleteUnusedCameraFile) {
        if (deleteUnusedCameraFile && pendingCameraUri != null
                && pendingCameraUri.equals(selectedImageUri)) {
            deletePendingCameraFile();
        } else if (!deleteUnusedCameraFile) {
            // The chat bubble may still reference this cache URI; let Android clear it later.
            pendingCameraFile = null;
        }
        selectedImageUri = null;
        pendingCameraUri = null;
        imagePreview.setImageDrawable(null);
        selectedImageLayout.setVisibility(View.GONE);
    }

    private void askQuestion() {
        if (loading) return;

        String typedText = questionInput.getText().toString().trim();
        Uri imageUri = selectedImageUri;
        if (typedText.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Enter a message or attach an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        ScienceSubject selectedSubject = activeSubject;
        EducationLevel level = EducationLevel.fromPosition(educationSpinner.getSelectedItemPosition());
        ExplanationStyle style = ExplanationStyle.fromPosition(styleSpinner.getSelectedItemPosition());
        String currentQuestion = typedText.isEmpty()
                ? "Please analyse and solve the attached science image."
                : typedText;
        String conversationContext = buildConversationContext();
        String visibleImageUri = imageUri == null ? null : imageUri.toString();

        persistCurrentPreferences();
        messages.add(ScienceChatMessage.user(typedText, visibleImageUri, selectedSubject));
        int loadingIndex = messages.size();
        messages.add(ScienceChatMessage.loading(selectedSubject, currentQuestion));
        chatAdapter.notifyItemRangeInserted(messages.size() - 2, 2);
        questionInput.setText("");
        clearImage(false);
        setLoading(true);
        updateEmptyState();
        scrollToBottom(true);

        imageExecutor.execute(() -> {
            try {
                ScienceImageReader.ImageData imageData = imageUri == null
                        ? null : imageReader.readCompressed(this, imageUri);
                String privateImageUri = imageData == null
                        ? null : privateImageStore.saveJpeg(imageData.getBytes());
                ScienceQuestion question = new ScienceQuestion(
                        userSession.getUserId(),
                        selectedSubject,
                        level,
                        style,
                        currentQuestion,
                        conversationContext,
                        privateImageUri,
                        imageData == null ? null : imageData.getBytes(),
                        imageData == null ? null : imageData.getMimeType()
                );
                runOnUiThread(() -> {
                    if (!destroyed) executeQuestion(question, loadingIndex, messages);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (!destroyed) showRequestError(
                            loadingIndex,
                            safeMessage(error, "Unable to read the selected image."),
                            selectedSubject,
                            currentQuestion,
                            messages);
                });
            }
        });
    }

    private void executeQuestion(ScienceQuestion question,
                                 int loadingIndex,
                                 List<ScienceChatMessage> targetConversation) {
        askUseCase.execute(question, new ResultCallback<ScienceAnswer>() {
            @Override
            public void onSuccess(ScienceAnswer result) {
                if (destroyed) return;
                boolean bookmarked = false;
                try {
                    bookmarked = bookmarkUseCase.isBookmarked(
                            userSession.getUserId(), result.getHistoryId());
                } catch (Exception ignored) {
                    // The answer should still render when bookmark storage is unavailable.
                }
                ScienceChatMessage answerMessage = ScienceChatMessage.assistant(
                        result.getContent(),
                        result.getHistoryId(),
                        question.getSubject(),
                        question.getQuestionText(),
                        result.isFromCache(),
                        bookmarked
                );
                replaceMessage(targetConversation, loadingIndex, answerMessage);
                setLoading(false);
                scrollToBottom(true);
            }

            @Override
            public void onError(String message, Throwable error) {
                if (destroyed) return;
                showRequestError(
                        loadingIndex,
                        message == null || message.trim().isEmpty()
                                ? "Science AI could not answer this message. Please try again."
                                : message,
                        question.getSubject(),
                        question.getQuestionText(),
                        targetConversation);
            }
        });
    }

    private void showRequestError(int index,
                                  String message,
                                  ScienceSubject subject,
                                  String sourceQuestion,
                                  List<ScienceChatMessage> targetConversation) {
        replaceMessage(targetConversation, index,
                ScienceChatMessage.error(message, subject, sourceQuestion));
        setLoading(false);
        scrollToBottom(true);
    }

    private void replaceMessage(List<ScienceChatMessage> targetConversation,
                                int index,
                                ScienceChatMessage replacement) {
        if (targetConversation == null || index < 0 || index >= targetConversation.size()) return;
        targetConversation.set(index, replacement);
        if (targetConversation == messages && chatAdapter != null) chatAdapter.notifyItemChanged(index);
    }

    private String buildConversationContext() {
        if (messages == null || messages.isEmpty()) return "";
        int start = Math.max(0, messages.size() - MAX_CONTEXT_MESSAGES);
        StringBuilder context = new StringBuilder();
        for (int i = start; i < messages.size(); i++) {
            ScienceChatMessage message = messages.get(i);
            if (message.isLoading() || message.isError()) continue;
            String label = message.getRole() == ScienceChatMessage.Role.USER
                    ? "Student" : activeSubject.getDisplayName() + " tutor";
            String text = message.getText();
            if (text == null || text.trim().isEmpty()) text = "[Image question]";
            if (text.length() > 1800) text = text.substring(0, 1800) + "…";
            String line = label + ": " + text.trim() + "\n";
            if (context.length() + line.length() > MAX_CONTEXT_CHARACTERS) break;
            context.append(line);
        }
        return context.toString().trim();
    }

    private void toggleBookmark(ScienceChatMessage message, int position) {
        if (message == null || message.getHistoryId() <= 0) return;
        try {
            boolean bookmarked = bookmarkUseCase.toggle(
                    userSession.getUserId(),
                    message.getHistoryId(),
                    message.getSubject(),
                    message.getSourceQuestion(),
                    message.getText()
            );
            message.setBookmarked(bookmarked);
            chatAdapter.notifyItemChanged(position);
            Toast.makeText(this,
                    bookmarked ? "Answer bookmarked." : "Bookmark removed.",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Toast.makeText(this, "Bookmark storage is unavailable on this device.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void copyAnswer(String answer) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(this, "Clipboard is unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("Science AI answer", answer == null ? "" : answer));
        Toast.makeText(this, "Answer copied.", Toast.LENGTH_SHORT).show();
    }

    private void startNewChat() {
        if (loading) {
            Toast.makeText(this, "Wait for the current answer before starting a new chat.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int oldSize = messages.size();
        messages.clear();
        if (oldSize > 0) chatAdapter.notifyItemRangeRemoved(0, oldSize);
        questionInput.setText("");
        clearImage();
        toolsPanel.setVisibility(View.GONE);
        updateEmptyState();
        Toast.makeText(this,
                "New " + activeSubject.getDisplayName() + " chat started.",
                Toast.LENGTH_SHORT).show();
    }

    private void updatePreferenceStatus() {
        if (subjectStatus == null || educationSpinner.getAdapter() == null
                || styleSpinner.getAdapter() == null) return;
        EducationLevel level = EducationLevel.fromPosition(educationSpinner.getSelectedItemPosition());
        ExplanationStyle style = ExplanationStyle.fromPosition(styleSpinner.getSelectedItemPosition());
        subjectStatus.setText(activeSubject.getDisplayName() + " tutor · "
                + level.getDisplayName() + " · " + style.getDisplayName());
    }

    private void updateApiSettingsButton() {
        apiSettingsButton.setText(apiSettings != null && apiSettings.isConfigured()
                ? "Settings · API ✓" : "Settings · API needed");
    }

    private void setLoading(boolean isLoading) {
        loading = isLoading;
        askButton.setEnabled(!isLoading);
        pickImageButton.setEnabled(!isLoading);
        takePhotoButton.setEnabled(!isLoading);
        educationSpinner.setEnabled(!isLoading);
        styleSpinner.setEnabled(!isLoading);
        for (int i = 0; i < subjectGroup.getChildCount(); i++) {
            subjectGroup.getChildAt(i).setEnabled(!isLoading);
        }
        newChatButton.setEnabled(!isLoading);
        askButton.setAlpha(isLoading ? 0.55f : 1f);
    }

    private void updateEmptyState() {
        emptyState.setVisibility(messages == null || messages.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void scrollToBottom(boolean smooth) {
        if (messages == null || messages.isEmpty()) return;
        int last = messages.size() - 1;
        chatRecyclerView.post(() -> {
            if (destroyed) return;
            if (smooth) chatRecyclerView.smoothScrollToPosition(last);
            else chatRecyclerView.scrollToPosition(last);
        });
    }

    private int subjectButtonId(ScienceSubject subject) {
        if (subject == ScienceSubject.CHEMISTRY) return R.id.rbChemistry;
        if (subject == ScienceSubject.BIOLOGY) return R.id.rbBiology;
        return R.id.rbPhysics;
    }

    private ScienceSubject subjectFromButtonId(int checkedId) {
        if (checkedId == R.id.rbChemistry) return ScienceSubject.CHEMISTRY;
        if (checkedId == R.id.rbBiology) return ScienceSubject.BIOLOGY;
        return ScienceSubject.PHYSICS;
    }

    private String safeMessage(Exception error, String fallback) {
        String message = error == null ? null : error.getMessage();
        return message == null || message.trim().isEmpty() ? fallback : message;
    }

    private void restoreConversationState(@Nullable Bundle state) {
        if (state == null) return;
        try {
            activeSubject = ScienceSubject.valueOf(
                    state.getString(STATE_SUBJECT, preferenceStore.getSubject().name()));
        } catch (Exception ignored) {
            activeSubject = preferenceStore.getSubject();
        }

        String serialized = state.getString(STATE_CONVERSATIONS, "");
        if (!serialized.isEmpty()) {
            try {
                JSONObject root = new JSONObject(serialized);
                for (ScienceSubject subject : ScienceSubject.values()) {
                    JSONArray array = root.optJSONArray(subject.name());
                    List<ScienceChatMessage> list = conversations.get(subject);
                    if (array == null || list == null) continue;
                    list.clear();
                    for (int i = 0; i < array.length(); i++) {
                        ScienceChatMessage message = ScienceChatMessage.fromJson(array.optJSONObject(i));
                        if (message != null) list.add(message);
                    }
                }
            } catch (Exception ignored) {
                // Corrupt UI state should never prevent the Science module from opening.
            }
        }
        messages = conversations.get(activeSubject);
    }

    private void restoreViewState(@Nullable Bundle state) {
        if (state == null) return;
        questionInput.setText(state.getString(STATE_INPUT, ""));
        toolsPanel.setVisibility(state.getBoolean(STATE_TOOLS_VISIBLE, false)
                ? View.VISIBLE : View.GONE);
        String image = state.getString(STATE_SELECTED_IMAGE, "");
        if (!image.isEmpty()) showSelectedImage(Uri.parse(image));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SUBJECT, activeSubject.name());
        outState.putString(STATE_INPUT, questionInput.getText().toString());
        outState.putBoolean(STATE_TOOLS_VISIBLE, toolsPanel.getVisibility() == View.VISIBLE);
        outState.putString(STATE_SELECTED_IMAGE,
                selectedImageUri == null ? "" : selectedImageUri.toString());

        JSONObject root = new JSONObject();
        try {
            for (ScienceSubject subject : ScienceSubject.values()) {
                JSONArray array = new JSONArray();
                List<ScienceChatMessage> list = conversations.get(subject);
                if (list != null) {
                    for (ScienceChatMessage message : list) {
                        if (!message.isLoading()) array.put(message.toJson());
                    }
                }
                root.put(subject.name(), array);
            }
        } catch (Exception ignored) {
            // Preserve the rest of the activity state even if serialization fails.
        }
        outState.putString(STATE_CONVERSATIONS, root.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateApiSettingsButton();
        if (animatedBackground != null) {
            animatedBackground.setAnimationEnabled(preferenceStore.isAnimationEnabled());
            animatedBackground.start();
        }
    }

    @Override
    protected void onPause() {
        if (animatedBackground != null) animatedBackground.stop();
        persistCurrentPreferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        imageExecutor.shutdownNow();
        super.onDestroy();
    }
}
