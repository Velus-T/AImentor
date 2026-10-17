package com.brightpath.science.presentation;

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

import com.brightpath.science.R;
import com.brightpath.science.data.local.ScienceImageStore;
import com.brightpath.science.data.remote.GeminiApiClient;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.ExplanationStyle;
import com.brightpath.science.domain.model.ScienceAnswer;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.domain.model.ScienceQuestion;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.ScienceHistoryRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.usecase.AskScienceQuestionUseCase;
import com.brightpath.science.domain.usecase.SaveBookmarkUseCase;
import com.brightpath.science.presentation.chat.ScienceChatAdapter;
import com.brightpath.science.presentation.chat.ScienceChatMessage;
import com.brightpath.science.presentation.view.ScienceAnimatedBackgroundView;
import com.brightpath.science.util.ScienceImageReader;
import com.brightpath.science.util.SciencePreferenceStore;
import com.brightpath.science.util.ScienceUserSession;
import com.brightpath.science.util.ScienceUiFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Science AI chat screen with separate Physics, Chemistry and Biology conversations.
 *
 * <p>review MAP — when the lecturer asks "where does the Send button go?":</p>
 * <pre>
 * btnAskScience / IME Send
 *   -> setupQuestionActions()
 *   -> askQuestion()
 *   -> create ScienceQuestion
 *   -> executeQuestion()
 *   -> AskScienceQuestionUseCase.execute()
 *   -> validation -> cache -> ScienceAnswerService
 *   -> GeminiScienceService / Local fallback
 *   -> ScienceHistoryRepository.save()
 *   -> callback -> chat UI
 * </pre>
 *
 * <p>Subject separation is controlled by {@link ScienceSubject}. Each subject owns an
 * independent in-memory conversation, while the prompt factory applies a separate tutor
 * strategy and instructs Gemini to reject questions outside the selected subject.</p>
 */
public final class ScienceActivity extends AppCompatActivity {
    public static final String EXTRA_SUBJECT = "com.brightpath.science.extra.SUBJECT";
    public static final String EXTRA_OPEN_CAMERA = "com.brightpath.science.extra.OPEN_CAMERA";
    public static final String EXTRA_FOCUS_INPUT = "com.brightpath.science.extra.FOCUS_INPUT";
    public static final String EXTRA_HISTORY_ID = "com.brightpath.science.extra.HISTORY_ID";
    private static final int MAX_CONTEXT_MESSAGES = 12;
    private static final int MAX_CONTEXT_CHARACTERS = 12000;
    private static final int MAX_USER_CONTEXT_CHARACTERS = 1800;
    private static final int MAX_ASSISTANT_CONTEXT_CHARACTERS = 4200;
    private static final String STATE_SUBJECT = "science_chat_subject";
    private static final String STATE_CONVERSATIONS = "science_chat_conversations";
    private static final String STATE_CONVERSATION_IDS = "science_chat_conversation_ids";
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
    private TextView modelInfoText;
    private View modelInfoLayout;
    private Button askButton;
    private Button clearImageButton;
    private Button pickImageButton;
    private Button takePhotoButton;
    private Button newChatButton;
    private Button displaySettingsButton;
    private RecyclerView chatRecyclerView;
    private ScienceAnimatedBackgroundView animatedBackground;

    private final Map<ScienceSubject, List<ScienceChatMessage>> conversations =
            new EnumMap<>(ScienceSubject.class);
    private final Map<ScienceSubject, String> conversationIds =
            new EnumMap<>(ScienceSubject.class);
    private List<ScienceChatMessage> messages;
    private ScienceSubject activeSubject = ScienceSubject.PHYSICS;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private final ScienceImageReader imageReader = new ScienceImageReader();

    private ScienceChatAdapter chatAdapter;
    private AskScienceQuestionUseCase askUseCase;
    private SaveBookmarkUseCase bookmarkUseCase;
    private ScienceHistoryRepository historyRepository;
    private GeminiApiClient aiClient;
    private ScienceUserSession userSession;
    private SciencePreferenceStore preferenceStore;
    private ScienceImageStore privateImageStore;

    private Uri selectedImageUri;
    private Uri pendingCameraUri;
    private File pendingCameraFile;
    private boolean loading;
    private boolean suppressSubjectCallback;
    private volatile boolean destroyed;

    // region Camera / gallery result launchers

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

    // endregion

    // region Lifecycle and dependency wiring

        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science);

        ScienceModule module = ScienceModule.get(this);
        askUseCase = module.askScienceQuestionUseCase();
        bookmarkUseCase = module.saveBookmarkUseCase();
        historyRepository = module.historyRepository();
        aiClient = new GeminiApiClient(); // Used for status/model management
        userSession = new ScienceUserSession(this);
        preferenceStore = new SciencePreferenceStore(this);
        privateImageStore = new ScienceImageStore(this);

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
        handleLaunchIntent(savedInstanceState == null);
    }

    // endregion

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `createHistoryIntent`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
public static Intent createHistoryIntent(Context context, ScienceHistory item) {
        Intent intent = new Intent(context, ScienceActivity.class);
        if (item != null) {
            intent.putExtra(EXTRA_HISTORY_ID, item.getId());
            intent.putExtra(EXTRA_SUBJECT, item.getSubject().getCode());
        }
        return intent;
    }

        /*
 * Responsibility: Handles the `handleLaunchIntent` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void handleLaunchIntent(boolean freshLaunch) {
        if (freshLaunch) restoreHistoryFromIntent();
        if (getIntent().getBooleanExtra(EXTRA_OPEN_CAMERA, false)) {
            getIntent().removeExtra(EXTRA_OPEN_CAMERA);
            takePhotoButton.post(this::requestOrLaunchCamera);
        }
        if (getIntent().getBooleanExtra(EXTRA_FOCUS_INPUT, false)) {
            getIntent().removeExtra(EXTRA_FOCUS_INPUT);
            questionInput.post(() -> {
                questionInput.requestFocus();
                android.view.inputmethod.InputMethodManager keyboard =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (keyboard != null) keyboard.showSoftInput(questionInput,
                        android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            });
        }
    }

        /*
 * Responsibility: Restores the previously saved state handled by `restoreHistoryFromIntent`.
 * Purpose: Allows the learner to continue the same Science flow after navigation, rotation, or reopening saved content.
 */
private void restoreHistoryFromIntent() {
        long historyId = getIntent().getLongExtra(EXTRA_HISTORY_ID, 0L);
        getIntent().removeExtra(EXTRA_HISTORY_ID);
        if (historyId <= 0L || historyRepository == null) return;

        ScienceHistory item;
        try {
            item = historyRepository.findById(userSession.getUserId(), historyId);
        } catch (Exception error) {
            Toast.makeText(this, "Không thể mở lịch sử chat trên thiết bị này.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (item == null) {
            Toast.makeText(this, "Đoạn chat cũ không còn tồn tại hoặc thuộc tài khoản khác.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        activeSubject = item.getSubject();
        suppressSubjectCallback = true;
        subjectGroup.check(subjectButtonId(activeSubject));
        suppressSubjectCallback = false;
        switchConversation(activeSubject, false);

        conversationIds.put(activeSubject, item.getConversationId());
        messages.clear();

        List<ScienceHistory> conversation;
        try {
            conversation = historyRepository.findConversation(
                    userSession.getUserId(), item.getConversationId());
        } catch (Exception ignored) {
            conversation = new ArrayList<>();
        }
        if (conversation.isEmpty()) conversation.add(item);

        for (ScienceHistory row : conversation) {
            messages.add(ScienceChatMessage.user(
                    row.getQuestion(), row.getImageUri(), row.getSubject()));
            boolean bookmarked = false;
            try {
                bookmarked = bookmarkUseCase.isBookmarked(
                        userSession.getUserId(), row.getId());
            } catch (Exception ignored) {
                // The chat remains readable even when bookmark storage is unavailable.
            }
            messages.add(ScienceChatMessage.assistant(
                    row.getAnswer(), row.getId(), row.getSubject(), row.getQuestion(),
                    false, bookmarked));
        }

        selectStoredPreferences(item);
        chatAdapter.submitMessages(messages);
        updateEmptyState();
        updatePreferenceStatus();
        scrollToBottom(false);
    }

        /*
 * Responsibility: Changes the active selection/state handled by `selectStoredPreferences`.
 * Purpose: Keeps the UI and subject-specific Science context synchronized.
 */
private void selectStoredPreferences(ScienceHistory item) {
        try {
            EducationLevel level = EducationLevel.valueOf(item.getEducationLevel());
            educationSpinner.setSelection(level.ordinal(), false);
        } catch (Exception ignored) {
            // Keep the current preference for legacy history rows.
        }
        try {
            ExplanationStyle style = ExplanationStyle.valueOf(item.getExplanationStyle());
            styleSpinner.setSelection(style.ordinal(), false);
        } catch (Exception ignored) {
            // Keep the current preference for legacy history rows.
        }
    }

        /*
 * Responsibility: Configures the dependencies, UI elements, listeners, or initial state required by `initializeConversations`.
 * Purpose: Keeps setup logic grouped and makes the main lifecycle/business method easier to read.
 */
private void initializeConversations() {
        for (ScienceSubject subject : ScienceSubject.values()) {
            conversations.put(subject, new ArrayList<>());
            conversationIds.put(subject, newConversationId());
        }
        String requestedSubject = getIntent().getStringExtra(EXTRA_SUBJECT);
        activeSubject = requestedSubject == null
                ? (preferenceStore == null ? ScienceSubject.PHYSICS : preferenceStore.getSubject())
                : ScienceSubject.fromCode(requestedSubject);
        messages = conversations.get(activeSubject);
    }

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindViews`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
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
        modelInfoText = findViewById(R.id.tvModelInfo);
        modelInfoLayout = findViewById(R.id.layoutModelInfo);
        askButton = findViewById(R.id.btnAskScience);
        clearImageButton = findViewById(R.id.btnClearImage);
        pickImageButton = findViewById(R.id.btnPickImage);
        takePhotoButton = findViewById(R.id.btnTakePhoto);
        newChatButton = findViewById(R.id.btnNewScienceChat);
        chatRecyclerView = findViewById(R.id.rvScienceChat);
        displaySettingsButton = findViewById(R.id.btnScienceDisplaySettings);
        animatedBackground = findViewById(R.id.scienceAnimatedBackground);
    }

        /*
 * Responsibility: Updates the state or value handled by `setupConversation` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setupConversation() {
        chatAdapter = new ScienceChatAdapter(messages, new ScienceChatAdapter.MessageActionListener() {
                        /*
 * Responsibility: Performs the `onBookmarkClicked` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onBookmarkClicked(ScienceChatMessage message, int position) {
                toggleBookmark(message, position);
            }

                        /*
 * Responsibility: Performs the `onCopyClicked` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
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

        /*
 * Responsibility: Updates the state or value handled by `setupSpinners` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setupSpinners() {
        String[] levels = new String[EducationLevel.values().length];
        for (int i = 0; i < levels.length; i++) levels[i] = getString(EducationLevel.values()[i].getDisplayNameResId());
        educationSpinner.setAdapter(scienceSpinnerAdapter(levels));

        String[] styles = new String[ExplanationStyle.values().length];
        for (int i = 0; i < styles.length; i++) styles[i] = getString(ExplanationStyle.values()[i].getDisplayNameResId());
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
                        /*
 * Responsibility: Performs the `onItemSelected` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                persistCurrentPreferences();
                updatePreferenceStatus();
            }

                        /*
 * Responsibility: Performs the `onNothingSelected` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onNothingSelected(AdapterView<?> parent) {
                updatePreferenceStatus();
            }
        };
        educationSpinner.setOnItemSelectedListener(selectionListener);
        styleSpinner.setOnItemSelectedListener(selectionListener);
    }

        /*
 * Responsibility: Handles the `scienceSpinnerAdapter` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private android.widget.ArrayAdapter<String> scienceSpinnerAdapter(String[] values) {
        return ScienceUiFactory.spinnerAdapter(this, values);
    }

    // region Navigation and user actions

        /*
 * Responsibility: Updates the state or value handled by `setupNavigation` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
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
        displaySettingsButton.setOnClickListener(view -> safeStartActivity(ScienceSettingsActivity.class));
    }

        /*
 * Responsibility: Creates the required navigation/action and opens the Science destination handled by `safeStartActivity`.
 * Purpose: Keeps navigation code separate from the business logic executed by the destination screen.
 */
private void safeStartActivity(Class<?> activityClass) {
        try {
            startActivity(new Intent(this, activityClass));
        } catch (Exception error) {
            Toast.makeText(this, "Unable to open this Science tool.", Toast.LENGTH_LONG).show();
        }
    }

        /*
 * Responsibility: Updates the state or value handled by `setupQuestionActions` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setupQuestionActions() {
        modelInfoLayout.setOnClickListener(view -> showModelSelectionMenu());
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

        /*
 * Responsibility: Updates the state or value handled by `setupSuggestions` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setupSuggestions() {
        findViewById(R.id.btnSuggestionPhenomenon).setOnClickListener(view ->
                useSuggestion(getString(R.string.science_suggestion_step_by_step) + ": "));
        findViewById(R.id.btnSuggestionChemistry).setOnClickListener(view -> {
            selectSubject(ScienceSubject.CHEMISTRY);
            useSuggestion(getString(R.string.science_suggestion_equations));
        });
        findViewById(R.id.btnSuggestionBiology).setOnClickListener(view -> {
            selectSubject(ScienceSubject.BIOLOGY);
            useSuggestion(getString(R.string.science_suggestion_phenomenon));
        });
    }

        /*
 * Responsibility: Handles the `useSuggestion` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void useSuggestion(String suggestion) {
        questionInput.setText(suggestion);
        questionInput.setSelection(suggestion.length());
        questionInput.requestFocus();
    }

        /*
 * Responsibility: Changes the active selection/state handled by `selectSubject`.
 * Purpose: Keeps the UI and subject-specific Science context synchronized.
 */
private void selectSubject(ScienceSubject subject) {
        if (loading || subject == null || subject == activeSubject) return;
        suppressSubjectCallback = true;
        subjectGroup.check(subjectButtonId(subject));
        suppressSubjectCallback = false;
        switchConversation(subject, true);
    }

        /*
 * Responsibility: Switches the visible chat to the selected Physics, Chemistry, or Biology conversation and restores its state.
 * Purpose: Keeps subject conversations separate so messages and prompts are not mixed between Science subjects.
 */
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
                    subjectLabel(activeSubject) + " đã được chọn. Mỗi môn có một cuộc trò chuyện riêng.",
                    Toast.LENGTH_SHORT).show();
        }
    }

        /*
 * Responsibility: Persists the data supplied to `persistCurrentPreferences` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
private void persistCurrentPreferences() {
        if (preferenceStore == null || educationSpinner == null || styleSpinner == null) return;
        preferenceStore.save(activeSubject,
                educationSpinner.getSelectedItemPosition(),
                styleSpinner.getSelectedItemPosition());
    }

    // endregion

    // region Image input

        /*
 * Responsibility: Checks camera permission/state and then starts the Science camera flow when it is safe to do so.
 * Purpose: Prevents invalid camera access and keeps image-question handling controlled by Android permissions.
 */
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

        /*
 * Responsibility: Creates a safe temporary image target/URI and launches the camera capture contract.
 * Purpose: Provides an image for the Science AI flow without exposing an unsafe raw file path.
 */
private void launchCamera() {
        try {
            File directory = new File(getCacheDir(), "science_camera");
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IllegalStateException("Camera cache directory could not be created.");
            }
            pendingCameraFile = File.createTempFile(
                    "science_question_" + System.currentTimeMillis() + "_", ".jpg", directory);
            pendingCameraUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", pendingCameraFile);
            takePhotoLauncher.launch(pendingCameraUri);
        } catch (Exception error) {
            deletePendingCameraFile();
            pendingCameraUri = null;
            Toast.makeText(this, "Unable to open the camera on this device.", Toast.LENGTH_LONG).show();
        }
    }

        /*
 * Responsibility: Removes or resets the data/state handled by `deletePendingCameraFile`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
private void deletePendingCameraFile() {
        if (pendingCameraFile != null && pendingCameraFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            pendingCameraFile.delete();
        }
        pendingCameraFile = null;
    }

        /*
 * Responsibility: Accepts the selected/captured image URI, updates the preview, and stores the current image-input state.
 * Purpose: Ensures the same image that the learner sees is the image prepared for the AI request.
 */
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

        /*
 * Responsibility: Removes or resets the data/state handled by `clearImage`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
private void clearImage() {
        clearImage(true);
    }

        /*
 * Responsibility: Removes or resets the data/state handled by `clearImage`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
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

    // endregion

    // region Send question -> use case -> AI -> UI

        /*
 * Responsibility: Reads the current text/image input and Science selections from the UI, builds a ScienceQuestion, and prepares it for the use-case.
 * Purpose: This is the entry point of the Send/Ask flow; the Activity collects UI state but does not call Gemini or SQLite directly.
 */
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
        String activeConversationId = currentConversationId();
        final int requestUserId = userSession.getUserId();
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
                        requestUserId,
                        selectedSubject,
                        level,
                        style,
                        currentQuestion,
                        conversationContext,
                        activeConversationId,
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

        /*
 * Responsibility: Passes the prepared ScienceQuestion to AskScienceQuestionUseCase and handles the asynchronous success/error callbacks.
 * Purpose: Connects the chat UI to the domain flow while keeping validation, caching, AI access, and persistence outside the Activity.
 */
private void executeQuestion(ScienceQuestion question,
                                 int loadingIndex,
                                 List<ScienceChatMessage> targetConversation) {
        askUseCase.execute(question, new ResultCallback<ScienceAnswer>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
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
                updateModelInfo(result);
                setLoading(false);
                scrollToBottom(true);
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
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

        /*
 * Responsibility: Prepares and presents the data/state handled by `showRequestError` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
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

        /*
 * Responsibility: Handles the `replaceMessage` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void replaceMessage(List<ScienceChatMessage> targetConversation,
                                int index,
                                ScienceChatMessage replacement) {
        if (targetConversation == null || index < 0 || index >= targetConversation.size()) return;
        targetConversation.set(index, replacement);
        if (targetConversation == messages && chatAdapter != null) chatAdapter.notifyItemChanged(index);
    }

    // endregion

    // region Conversation context and saved-chat continuity

    /**
     * Builds context from newest to oldest so the immediately preceding exchange is never
     * dropped by an older long answer. The returned transcript is reordered chronologically
     * before it is sent to Gemini.
     */
        /*
 * Responsibility: Builds compact context from the active Science conversation for a follow-up question.
 * Purpose: Allows Gemini to understand follow-up messages without mixing Physics, Chemistry, and Biology conversations.
 */
private String buildConversationContext() {
        if (messages == null || messages.isEmpty()) return "";

        java.util.LinkedList<String> blocks = new java.util.LinkedList<>();
        int usedCharacters = 0;
        int includedMessages = 0;

        for (int i = messages.size() - 1;
             i >= 0 && includedMessages < MAX_CONTEXT_MESSAGES;
             i--) {
            ScienceChatMessage message = messages.get(i);
            if (message == null || message.isLoading() || message.isError()) continue;

            boolean isUser = message.getRole() == ScienceChatMessage.Role.USER;
            String text = message.getText();
            if (text == null || text.trim().isEmpty()) {
                text = isUser ? "[The student sent an image question]" : "";
            }
            if (text.trim().isEmpty()) continue;

            int perMessageLimit = isUser
                    ? MAX_USER_CONTEXT_CHARACTERS
                    : MAX_ASSISTANT_CONTEXT_CHARACTERS;
            text = compactContextText(text.trim(), perMessageLimit);

            String label = isUser
                    ? "STUDENT"
                    : activeSubject.getDisplayName().toUpperCase(java.util.Locale.ROOT) + " TUTOR";
            String block = label + ":\n" + text + "\n";
            int remaining = MAX_CONTEXT_CHARACTERS - usedCharacters;
            if (remaining < 120) break;

            if (block.length() > remaining) {
                String compacted = compactContextText(text, Math.max(80, remaining - label.length() - 4));
                block = label + ":\n" + compacted + "\n";
            }

            blocks.addFirst(block);
            usedCharacters += block.length();
            includedMessages++;
        }

        if (blocks.isEmpty()) return "";
        StringBuilder context = new StringBuilder();
        context.append("Chronological transcript; the final block is the most recent completed message.\n");
        for (String block : blocks) {
            context.append("---\n").append(block);
        }
        return context.toString().trim();
    }

    /** Keeps both the beginning and the ending of a long answer because a follow-up may
     * refer either to an early definition or to the latest calculation/conclusion. */
        /*
 * Responsibility: Normalizes or formats the value handled by `compactContextText` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String compactContextText(String text, int maximumCharacters) {
        if (text == null) return "";
        String normalized = text.trim();
        if (maximumCharacters <= 0 || normalized.length() <= maximumCharacters) {
            return normalized;
        }
        if (maximumCharacters < 120) {
            return normalized.substring(0, Math.max(1, maximumCharacters));
        }

        int headLength = (int) (maximumCharacters * 0.62);
        int tailLength = maximumCharacters - headLength - 24;
        return normalized.substring(0, headLength).trim()
                + "\n… [middle shortened] …\n"
                + normalized.substring(normalized.length() - tailLength).trim();
    }

        /*
 * Responsibility: Adds or removes the selected AI answer from Science bookmarks through the bookmark use-case/repository.
 * Purpose: Lets the learner keep important explanations for later revision while preserving user ownership.
 */
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
                    bookmarked ? "Đã lưu câu trả lời." : "Đã bỏ lưu câu trả lời.",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Toast.makeText(this, "Bookmark storage is unavailable on this device.",
                    Toast.LENGTH_LONG).show();
        }
    }

        /*
 * Responsibility: Copies the value/content handled by `copyAnswer` to the intended destination.
 * Purpose: Provides the learner action without changing the underlying Science record.
 */
private void copyAnswer(String answer) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(this, "Clipboard is unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("Science AI answer", answer == null ? "" : answer));
        Toast.makeText(this, "Đã sao chép câu trả lời.", Toast.LENGTH_SHORT).show();
    }

        /*
 * Responsibility: Creates the required navigation/action and opens the Science destination handled by `startNewChat`.
 * Purpose: Keeps navigation code separate from the business logic executed by the destination screen.
 */
private void startNewChat() {
        if (loading) {
            Toast.makeText(this, "Wait for the current answer before starting a new chat.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int oldSize = messages.size();
        messages.clear();
        conversationIds.put(activeSubject, newConversationId());
        if (oldSize > 0) chatAdapter.notifyItemRangeRemoved(0, oldSize);
        questionInput.setText("");
        clearImage();
        toolsPanel.setVisibility(View.GONE);
        updateEmptyState();
        Toast.makeText(this,
                "Đã bắt đầu cuộc trò chuyện " + subjectLabel(activeSubject) + " mới.",
                Toast.LENGTH_SHORT).show();
    }

        /*
 * Responsibility: Handles the `currentConversationId` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private String currentConversationId() {
        String id = conversationIds.get(activeSubject);
        if (id == null || id.trim().isEmpty()) {
            id = newConversationId();
            conversationIds.put(activeSubject, id);
        }
        return id;
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `newConversationId`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private String newConversationId() {
        return UUID.randomUUID().toString();
    }

        /*
 * Responsibility: Updates the state or value handled by `updatePreferenceStatus` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void updatePreferenceStatus() {
        if (subjectStatus == null || educationSpinner.getAdapter() == null
                || styleSpinner.getAdapter() == null) return;
        EducationLevel level = EducationLevel.fromPosition(educationSpinner.getSelectedItemPosition());
        ExplanationStyle style = ExplanationStyle.fromPosition(styleSpinner.getSelectedItemPosition());
        subjectStatus.setText(subjectLabel(activeSubject) + " · "
                + getString(level.getDisplayNameResId()) + " · " + getString(style.getDisplayNameResId()));
    }


    // endregion

    // region UI state and rendering

        /*
 * Responsibility: Updates the state or value handled by `setLoading` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
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

        /*
 * Responsibility: Updates the state or value handled by `updateEmptyState` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void updateEmptyState() {
        emptyState.setVisibility(messages == null || messages.isEmpty() ? View.VISIBLE : View.GONE);
    }

        /*
 * Responsibility: Handles the `scrollToBottom` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void scrollToBottom(boolean smooth) {
        if (messages == null || messages.isEmpty()) return;
        int last = messages.size() - 1;
        chatRecyclerView.post(() -> {
            if (destroyed) return;
            if (smooth) chatRecyclerView.smoothScrollToPosition(last);
            else chatRecyclerView.scrollToPosition(last);
        });
    }

        /*
 * Responsibility: Handles the `subjectLabel` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private String subjectLabel(ScienceSubject subject) {
        return getString(subject.getDisplayNameResId());
    }

        /*
 * Responsibility: Handles the `subjectButtonId` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private int subjectButtonId(ScienceSubject subject) {
        if (subject == ScienceSubject.CHEMISTRY) return R.id.rbChemistry;
        if (subject == ScienceSubject.BIOLOGY) return R.id.rbBiology;
        return R.id.rbPhysics;
    }

        /*
 * Responsibility: Handles the `subjectFromButtonId` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private ScienceSubject subjectFromButtonId(int checkedId) {
        if (checkedId == R.id.rbChemistry) return ScienceSubject.CHEMISTRY;
        if (checkedId == R.id.rbBiology) return ScienceSubject.BIOLOGY;
        return ScienceSubject.PHYSICS;
    }

        /*
 * Responsibility: Handles the `safeMessage` UI interaction/state operation for ScienceActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private String safeMessage(Exception error, String fallback) {
        String message = error == null ? null : error.getMessage();
        return message == null || message.trim().isEmpty() ? fallback : message;
    }

        /*
 * Responsibility: Restores the previously saved state handled by `restoreConversationState`.
 * Purpose: Allows the learner to continue the same Science flow after navigation, rotation, or reopening saved content.
 */
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
        String serializedIds = state.getString(STATE_CONVERSATION_IDS, "");
        if (!serializedIds.isEmpty()) {
            try {
                JSONObject ids = new JSONObject(serializedIds);
                for (ScienceSubject subject : ScienceSubject.values()) {
                    String id = ids.optString(subject.name(), "");
                    if (!id.trim().isEmpty()) conversationIds.put(subject, id);
                }
            } catch (Exception ignored) {
                // Fresh IDs created during initialization remain valid.
            }
        }
        messages = conversations.get(activeSubject);
    }

        /*
 * Responsibility: Restores the previously saved state handled by `restoreViewState`.
 * Purpose: Allows the learner to continue the same Science flow after navigation, rotation, or reopening saved content.
 */
private void restoreViewState(@Nullable Bundle state) {
        if (state == null) return;
        questionInput.setText(state.getString(STATE_INPUT, ""));
        toolsPanel.setVisibility(state.getBoolean(STATE_TOOLS_VISIBLE, false)
                ? View.VISIBLE : View.GONE);
        String image = state.getString(STATE_SELECTED_IMAGE, "");
        if (!image.isEmpty()) showSelectedImage(Uri.parse(image));
    }

        /*
 * Responsibility: Writes the required UI/conversation state into the Bundle before Android may recreate the component.
 * Purpose: Allows rotation/process recreation without losing the learner's current Science flow.
 */
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

        JSONObject ids = new JSONObject();
        try {
            for (ScienceSubject subject : ScienceSubject.values()) {
                ids.put(subject.name(), conversationIds.get(subject));
            }
        } catch (Exception ignored) {
            // Conversation messages are still saved even if an ID cannot be serialized.
        }
        outState.putString(STATE_CONVERSATION_IDS, ids.toString());
    }

        /*
 * Responsibility: Updates the state or value handled by `updateModelInfo` for ScienceActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void updateModelInfo(ScienceAnswer latestAnswer) {
        int label = latestAnswer != null && latestAnswer.isFromCache() ? R.string.science_source_saved
                : (latestAnswer != null && latestAnswer.isLocal()) || !aiClient.isConfigured() ? R.string.science_source_local : R.string.science_source_remote;
        modelInfoText.setText(label);
        modelInfoText.setTextColor(androidx.core.content.ContextCompat.getColor(this,R.color.science_text_secondary));

    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showModelSelectionMenu` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showModelSelectionMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, modelInfoLayout);
        popup.getMenu().add("gemini-flash-latest (Auto)");
        popup.getMenu().add("gemini-3.5-flash (Frontier)");
        popup.getMenu().add("gemini-3.1-flash-lite (Stable)");

        popup.setOnMenuItemClickListener(item -> {
            CharSequence title = item.getTitle();
            if (title == null) return false;
            String choice = title.toString().split(" ")[0];
            aiClient.setModelName(choice);
            updateModelInfo(null);
            Toast.makeText(this, "Model switched to " + choice, Toast.LENGTH_SHORT).show();
            return true;
        });
        popup.show();
    }

        /*
 * Responsibility: Refreshes the current state/data when the component returns to the foreground.
 * Purpose: Ensures the UI reflects changes made on other Science screens.
 */
@Override
    protected void onResume() {
        super.onResume();
        if (animatedBackground != null) {
            animatedBackground.setAnimationEnabled(preferenceStore.isAnimationEnabled());
            animatedBackground.start();
        }
    }

        /*
 * Responsibility: Persists or pauses temporary state when the screen leaves the foreground.
 * Purpose: Prevents transient UI/session state from being lost or continuing unnecessarily in the background.
 */
@Override
    protected void onPause() {
        if (animatedBackground != null) animatedBackground.stop();
        persistCurrentPreferences();
        super.onPause();
    }

    // endregion

        /*
 * Responsibility: Releases temporary resources, listeners, or component state during destruction.
 * Purpose: Reduces leaks and prevents stale callbacks from affecting a destroyed Science screen.
 */
@Override
    protected void onDestroy() {
        destroyed = true;
        imageExecutor.shutdownNow();
        super.onDestroy();
    }
}
