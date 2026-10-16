package com.example.modulelaptrinh;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modulelaptrinh.adapters.PendingImageAdapter;
import com.example.modulelaptrinh.adapters.ProgrammingChatAdapter;
import com.example.modulelaptrinh.database.ProgrammingDbHelper;
import com.example.modulelaptrinh.utils.GeminiApiClient;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TRUNG TÂM CỦA CHỨC NĂNG AI CHAT PROGRAMMING.
 *
 * <p>Luồng chính:</p>
 * <pre>
 * User bấm Send
 *   -> sendMessage()
 *   -> ProgrammingDbHelper.insertMessage() lưu câu hỏi
 *   -> buildContextualPrompt() tạo prompt có lịch sử
 *   -> GeminiApiClient.generateText() gọi Gemini
 *   -> handleApiResponse()
 *   -> ProgrammingDbHelper.insertMessage() lưu câu trả lời AI
 * </pre>
 *
 * <p>Database: sessions + messages trong ProgrammingMentor.db.</p>
 */
public class ProgrammingChatActivity extends AppCompatActivity {
    private static final String TAG = "ProgrammingChatActivity";
    // Tính/đọc và lưu giá trị `MAX_CONTEXT_MESSAGES` (max context messages) để sử dụng ở các bước phía sau.
    private static final int MAX_CONTEXT_MESSAGES = 12;
    // Tính/đọc và lưu giá trị `MAX_CONTEXT_CHARS` (max context chars) để sử dụng ở các bước phía sau.
    private static final int MAX_CONTEXT_CHARS = 12_000;
    // Tính/đọc và lưu giá trị `MAX_CONTINUATIONS` (max continuations) để sử dụng ở các bước phía sau.
    private static final int MAX_CONTINUATIONS = 2;

    // Tham chiếu `recyclerView` tới RecyclerView trong layout để đọc/cập nhật giao diện.
    private RecyclerView recyclerView;
    // Tham chiếu `rvPendingImages` tới RecyclerView trong layout để đọc/cập nhật giao diện.
    private RecyclerView rvPendingImages;
    // Tham chiếu `edtQuestion` tới ô nhập liệu trong layout để đọc/cập nhật giao diện.
    private EditText edtQuestion;
    // Tham chiếu `btnSend` tới ImageView trong layout để đọc/cập nhật giao diện.
    private ImageView btnSend;
    // Tham chiếu `btnUploadImage` tới ImageView trong layout để đọc/cập nhật giao diện.
    private ImageView btnUploadImage;
    // Tham chiếu `btnBack` tới ImageView trong layout để đọc/cập nhật giao diện.
    private ImageView btnBack;
    // Tham chiếu `pbChatLoading` tới thanh tiến trình trong layout để đọc/cập nhật giao diện.
    private ProgressBar pbChatLoading;
    // Adapter `chatAdapter` nối dữ liệu Java với RecyclerView tương ứng.
    private ProgrammingChatAdapter chatAdapter;
    // Adapter `pendingImageAdapter` nối dữ liệu Java với RecyclerView tương ứng.
    private PendingImageAdapter pendingImageAdapter;
    // Danh sách `messageList` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<ProgrammingChatMessage> messageList;
    // Helper `dbHelper` chịu trách nhiệm đọc/ghi SQLite `ProgrammingMentor.db`.
    private ProgrammingDbHelper dbHelper;

    // Tính/đọc và lưu giá trị `language` (language) để sử dụng ở các bước phía sau.
    private String language = "Lập trình";
    // Tính/đọc và lưu ID của phiên chat hiện tại để sử dụng ở các bước phía sau.
    private long sessionId = -1;
    // Biến `userId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int userId;
    // Cờ `isFirstMessageSent` lưu trạng thái true/false để điều khiển luồng xử lý.
    private boolean isFirstMessageSent;
    // Biến `previewDialog` giữ dialog dùng để hiển thị trạng thái/thông báo cho người dùng.
    private Dialog previewDialog;
    // Cờ `isSending` lưu trạng thái true/false để điều khiển luồng xử lý.
    private boolean isSending;
    // Client `geminiClient` dùng để gửi request và nhận phản hồi từ Gemini API.
    private GeminiApiClient geminiClient;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                            return;
                        }

                        // Khởi tạo giá trị `selectedUris` (selected uris) phục vụ cho bước xử lý tiếp theo.
                        List<Uri> selectedUris = new ArrayList<>();
                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (result.getData().getClipData() != null) {
                            // Tính/đọc và lưu giá trị `count` (count) để sử dụng ở các bước phía sau.
                            int count = result.getData().getClipData().getItemCount();
                            // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
                            for (int i = 0; i < count; i++) {
                                // Tính/đọc và lưu giá trị `uri` (uri) để sử dụng ở các bước phía sau.
                                Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                                if (uri != null) {
                                    // Thêm phần tử mới vào danh sách đang quản lý trong bộ nhớ.
                                    selectedUris.add(uri);
                                }
                            }
                        // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
                        } else if (result.getData().getData() != null) {
                            // Thêm phần tử mới vào danh sách đang quản lý trong bộ nhớ.
                            selectedUris.add(result.getData().getData());
                        }

                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (selectedUris.isEmpty()) {
                            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                            return;
                        }

                        // Tính/đọc và lưu giá trị `remainingSlots` (remaining slots) để sử dụng ở các bước phía sau.
                        int remainingSlots = pendingImageAdapter.getRemainingSlots();
                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (remainingSlots <= 0) {
                            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                            Toast.makeText(this, "Đã đạt giới hạn 5 ảnh", Toast.LENGTH_SHORT).show();
                            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                            return;
                        }

                        // Tính/đọc và lưu giá trị `urisToAdd` (uris to add) để sử dụng ở các bước phía sau.
                        List<Uri> urisToAdd = selectedUris.size() > remainingSlots
                                ? selectedUris.subList(0, remainingSlots)
                                : selectedUris;
                        // Gọi `addImages()` trên `pendingImageAdapter` để thực hiện thao tác tương ứng.
                        pendingImageAdapter.addImages(urisToAdd);
                        // Gọi hàm `updatePendingImagesVisibility()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                        updatePendingImagesVisibility();

                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (selectedUris.size() > remainingSlots) {
                            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                            Toast.makeText(
                                    this,
                                    "Chỉ thêm " + remainingSlots + " ảnh đầu tiên",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    // Hàm `onCreate()`: Khởi tạo Activity và chuẩn bị các thành phần cần thiết khi màn hình được tạo.
    protected void onCreate(Bundle savedInstanceState) {
        // Gọi implementation `onCreate()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onCreate(savedInstanceState);
        // Gắn file layout XML làm giao diện cho Activity này.
        setContentView(R.layout.fragment_chat_detail);

        // Nhận userId để kiểm tra quyền sở hữu session và lưu dữ liệu đúng tài khoản.
        userId = Math.max(0, getIntent().getIntExtra(MainActivity.EXTRA_USER_ID, 0));

        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Cập nhật giá trị `dbHelper` (db helper) theo trạng thái/kết quả vừa tính được.
            dbHelper = new ProgrammingDbHelper(this);
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi khởi tạo database", e);
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(this, R.string.prog_chat_error_open_db, Toast.LENGTH_SHORT).show();
            // Gọi hàm `finish()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            finish();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Client network dùng chung; Usage.CHAT giữ cấu hình key dành cho Chat.
        geminiClient = new GeminiApiClient(GeminiApiClient.Usage.CHAT);
        // Lấy Intent đã dùng để mở Activity nhằm đọc các tham số được truyền vào.
        language = getIntent().getStringExtra("LANGUAGE");
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (language == null || language.trim().isEmpty()) {
            // Cập nhật giá trị `language` (language) theo trạng thái/kết quả vừa tính được.
            language = getString(R.string.programming_app_name);
        }

        // Lấy Intent đã dùng để mở Activity nhằm đọc các tham số được truyền vào.
        String initialQuestion = getIntent().getStringExtra("QUESTION");
        // Lấy Intent đã dùng để mở Activity nhằm đọc các tham số được truyền vào.
        sessionId = getIntent().getLongExtra("SESSION_ID", -1);

        // Gọi hàm `initViews()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        initViews();
        // Gọi hàm `initAdapters()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        initAdapters();
        // Gọi hàm `initChatHistory()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        initChatHistory(initialQuestion);
        // Gọi hàm `setupListeners()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setupListeners();
    }

    // Hàm `initViews()`: Ánh xạ các View XML sang biến Java để code có thể thao tác giao diện.
    private void initViews() {
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        recyclerView = findViewById(R.id.rv_chat_detail);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        rvPendingImages = findViewById(R.id.rv_pending_images);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        edtQuestion = findViewById(R.id.et_chat_input);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        btnSend = findViewById(R.id.iv_send);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        btnUploadImage = findViewById(R.id.iv_attach);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        btnBack = findViewById(R.id.iv_back);
        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        pbChatLoading = findViewById(R.id.pb_chat_loading);
    }

    // Hàm `initAdapters()`: Thực hiện chức năng `initAdapters` của class này.
    private void initAdapters() {
        // Cập nhật danh sách tin nhắn theo trạng thái/kết quả vừa tính được.
        messageList = new ArrayList<>();
        // Cập nhật giá trị `chatAdapter` (chat adapter) theo trạng thái/kết quả vừa tính được.
        chatAdapter = new ProgrammingChatAdapter(messageList, this::showImagePreviewDialog);

        // Khởi tạo giá trị `layoutManager` (layout manager) phục vụ cho bước xử lý tiếp theo.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Gọi `setStackFromEnd()` để cập nhật thuộc tính/trạng thái của `layoutManager`.
        layoutManager.setStackFromEnd(true);
        // Thiết lập cách RecyclerView sắp xếp các item trên màn hình.
        recyclerView.setLayoutManager(layoutManager);
        // Gắn Adapter vào RecyclerView để biến danh sách dữ liệu thành các item giao diện.
        recyclerView.setAdapter(chatAdapter);

        pendingImageAdapter = new PendingImageAdapter(
                new PendingImageAdapter.OnImageActionListener() {
                    @Override
                    // Hàm `onImageClick()`: Thực hiện chức năng `onImageClick` của class này.
                    public void onImageClick(int position, Uri uri) {
                        // Gọi hàm `showImagePreviewDialog()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                        showImagePreviewDialog(uri);
                    }

                    @Override
                    // Hàm `onRemoveClick()`: Thực hiện chức năng `onRemoveClick` của class này.
                    public void onRemoveClick(int position) {
                        // Gọi `removeImage()` trên `pendingImageAdapter` để thực hiện thao tác tương ứng.
                        pendingImageAdapter.removeImage(position);
                        // Gọi hàm `updatePendingImagesVisibility()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                        updatePendingImagesVisibility();
                    }
                }
        );
        // Thiết lập cách RecyclerView sắp xếp các item trên màn hình.
        rvPendingImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        // Gắn Adapter vào RecyclerView để biến danh sách dữ liệu thành các item giao diện.
        rvPendingImages.setAdapter(pendingImageAdapter);
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        rvPendingImages.setVisibility(View.GONE);
    }

    /**
     * Nếu có SESSION_ID thì load chat cũ; nếu chưa có thì tạo session mới + welcome message.
     */
    private void initChatHistory(String initialQuestion) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (sessionId != -1) {
            // Chống mở session của tài khoản khác.
            if (!dbHelper.isSessionOwnedByUser(sessionId, userId)) {
                // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                Toast.makeText(this, R.string.prog_chat_error_session_owner, Toast.LENGTH_SHORT).show();
                // Gọi hàm `finish()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                finish();
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }
            // Gọi hàm `loadChatHistory()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            loadChatHistory(sessionId);
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Tạo một phiên chat mới trong bảng sessions và nhận lại sessionId.
            sessionId = dbHelper.createNewSession(userId);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (sessionId == -1) {
                // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                Toast.makeText(this, R.string.prog_chat_error_create_session, Toast.LENGTH_SHORT).show();
                // Gọi hàm `finish()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                finish();
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }

            // Lấy chuỗi từ resources và lưu vào `welcomeMessage` để hỗ trợ đa ngôn ngữ.
            String welcomeMessage = getString(R.string.prog_chat_welcome_format, language);
            // Gọi tầng database để persist tin nhắn hiện tại vào bảng messages.
            dbHelper.insertMessage(sessionId, welcomeMessage, false, null);
            // Gọi hàm `addMessage()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            addMessage(welcomeMessage, false, null);
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (initialQuestion != null
                && !initialQuestion.trim().isEmpty()
                && !isFirstMessageSent) {
            // Cập nhật giá trị `isFirstMessageSent` (is first message sent) theo trạng thái/kết quả vừa tính được.
            isFirstMessageSent = true;
            // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
            edtQuestion.setText(initialQuestion.trim());
            // Gọi `postDelayed()` trên `edtQuestion` để thực hiện thao tác tương ứng.
            edtQuestion.postDelayed(this::sendMessage, 300);
        }
    }

    // Hàm `setupListeners()`: Đăng ký các sự kiện tương tác của người dùng cho các View.
    private void setupListeners() {
        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        btnBack.setOnClickListener(v -> finish());

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        btnUploadImage.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (isSending) {
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (pendingImageAdapter.isFull()) {
                // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
                Toast.makeText(this, R.string.prog_chat_limit_images, Toast.LENGTH_SHORT).show();
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }

            // Tạo Intent để điều hướng sang Activity khác hoặc thực hiện một action Android.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            // Gọi `setType()` để cập nhật thuộc tính/trạng thái của `intent`.
            intent.setType("image/*");
            // Gọi `addCategory()` trên `intent` để thực hiện thao tác tương ứng.
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Gắn dữ liệu bổ sung vào Intent để màn hình đích có thể đọc lại.
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            // Gọi `launch()` trên `imagePickerLauncher` để thực hiện thao tác tương ứng.
            imagePickerLauncher.launch(intent);
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        btnSend.setOnClickListener(v -> sendMessage());
        edtQuestion.setOnEditorActionListener((v, actionId, event) -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // Gọi hàm `sendMessage()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                sendMessage();
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return true;
            }
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return false;
        });
    }

    // Hàm `updatePendingImagesVisibility()`: Cập nhật dữ liệu/trạng thái hiện tại theo giá trị mới.
    private void updatePendingImagesVisibility() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (pendingImageAdapter.getImageCount() > 0) {
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            rvPendingImages.setVisibility(View.VISIBLE);
            // Tính/đọc và lưu giá trị `fadeIn` (fade in) để sử dụng ở các bước phía sau.
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            // Gọi `startAnimation()` trên `rvPendingImages` để thực hiện thao tác tương ứng.
            rvPendingImages.startAnimation(fadeIn);
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            rvPendingImages.setVisibility(View.GONE);
        }
    }

    /** SELECT messages theo session_id rồi đưa lại lên RecyclerView. */
    private void loadChatHistory(long targetSessionId) {
        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Đọc toàn bộ message thuộc session để khôi phục nội dung cuộc trò chuyện.
            List<ProgrammingChatMessage> history = dbHelper.getMessagesBySession(targetSessionId);
            // Xóa dữ liệu cũ trong danh sách để chuẩn bị nạp lại dữ liệu mới.
            messageList.clear();
            // Gọi `addAll()` trên `messageList` để thực hiện thao tác tương ứng.
            messageList.addAll(history);
            // Thông báo Adapter rằng dữ liệu đã thay đổi để RecyclerView vẽ lại danh sách.
            chatAdapter.notifyDataSetChanged();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (!messageList.isEmpty()) {
                // Gọi `scrollToPosition()` trên `recyclerView` để thực hiện thao tác tương ứng.
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi tải lịch sử chat", e);
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(this, R.string.prog_chat_error_load_history, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * HÀM QUAN TRỌNG NHẤT CỦA CHAT - chạy khi bấm nút Send.
     * Bước 1 đọc text/ảnh -> bước 2 lưu message User -> bước 3 gọi AI ở background thread
     * -> bước 4 lưu response AI và cập nhật UI.
     */
    private void sendMessage() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (isSending || sessionId == -1) {
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Đọc nội dung hiện tại người dùng đã nhập/đang hiển thị trên View.
        String question = edtQuestion.getText().toString().trim();
        // Tính/đọc và lưu giá trị `pendingUris` (pending uris) để sử dụng ở các bước phía sau.
        List<Uri> pendingUris = pendingImageAdapter.getImageUris();

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (question.isEmpty() && pendingUris.isEmpty()) {
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(this, R.string.prog_chat_input_empty, Toast.LENGTH_SHORT).show();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Khởi tạo giá trị `imageBytesList` (image bytes list) phục vụ cho bước xử lý tiếp theo.
        List<byte[]> imageBytesList = new ArrayList<>();
        // Khởi tạo giá trị `imageMimeTypes` (image mime types) phục vụ cho bước xử lý tiếp theo.
        List<String> imageMimeTypes = new ArrayList<>();
        // Khởi tạo danh sách ảnh đã mã hóa Base64 phục vụ cho bước xử lý tiếp theo.
        List<String> imageBase64List = new ArrayList<>();

        // Ảnh được nén thành JPEG byte[] để gửi AI, đồng thời encode Base64 để lưu SQLite.
        for (Uri uri : pendingUris) {
            // Tính/đọc và lưu giá trị `imageBytes` (image bytes) để sử dụng ở các bước phía sau.
            byte[] imageBytes = convertUriToBytes(uri);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (imageBytes != null && imageBytes.length > 0) {
                // Thêm phần tử vừa tạo/xử lý vào danh sách `imageBytesList`.
                imageBytesList.add(imageBytes);
                // Thêm phần tử mới vào danh sách đang quản lý trong bộ nhớ.
                imageMimeTypes.add("image/jpeg");
                // Mã hóa dữ liệu ảnh thành Base64 để có thể gửi qua JSON hoặc lưu trong cột TEXT.
                imageBase64List.add(Base64.encodeToString(imageBytes, Base64.NO_WRAP));
            }
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (!pendingUris.isEmpty() && imageBytesList.isEmpty()) {
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(this, R.string.prog_chat_error_read_image, Toast.LENGTH_SHORT).show();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Tính/đọc và lưu giá trị `hasImages` (has images) để sử dụng ở các bước phía sau.
        boolean hasImages = !imageBytesList.isEmpty();
        // Tính/đọc và lưu giá trị `displayMessage` (display message) để sử dụng ở các bước phía sau.
        String displayMessage = question;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (displayMessage.isEmpty() && hasImages) {
            // Cập nhật giá trị `displayMessage` (display message) theo trạng thái/kết quả vừa tính được.
            displayMessage = getString(R.string.prog_chat_sent_images, imageBytesList.size());
        }

        // Tính/đọc và lưu giá trị `promptToSend` (prompt to send) để sử dụng ở các bước phía sau.
        String promptToSend = buildContextualPrompt(question, hasImages);
        // Khởi tạo giá trị `immutableImages` (immutable images) phục vụ cho bước xử lý tiếp theo.
        List<byte[]> immutableImages = new ArrayList<>(imageBytesList);
        // Khởi tạo giá trị `immutableMimeTypes` (immutable mime types) phục vụ cho bước xử lý tiếp theo.
        List<String> immutableMimeTypes = new ArrayList<>(imageMimeTypes);
        // Tính/đọc và lưu giá trị `savedDisplayMessage` (saved display message) để sử dụng ở các bước phía sau.
        String savedDisplayMessage = displayMessage;

        // Lưu câu hỏi User TRƯỚC khi gọi AI.
        // isUser=true -> cột messages.is_user = 1.
        long inserted = dbHelper.insertMessage(
                sessionId,
                savedDisplayMessage,
                true,
                imageBase64List
        );
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (inserted == -1) {
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(this, R.string.prog_chat_error_save_msg, Toast.LENGTH_SHORT).show();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Cập nhật giá trị `isSending` (is sending) theo trạng thái/kết quả vừa tính được.
        isSending = true;
        // Gọi hàm `setSendingState()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setSendingState(true);
        // Gọi hàm `addMessage()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        addMessage(savedDisplayMessage, true, imageBase64List);
        // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
        edtQuestion.setText("");
        // Gọi `clearAll()` trên `pendingImageAdapter` để thực hiện thao tác tương ứng.
        pendingImageAdapter.clearAll();
        // Gọi hàm `updatePendingImagesVisibility()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        updatePendingImagesVisibility();

        // Gọi network ở background thread để không block Main/UI Thread.
        new Thread(() -> {
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Đây là điểm ProgrammingChatActivity chuyển prompt sang GeminiApiClient.
                GeminiApiClient.Result result = geminiClient.generateText(
                        promptToSend,
                        immutableImages,
                        immutableMimeTypes,
                        0.55,
                        4096
                );
                // Tính/đọc và lưu giá trị `completed` (completed) để sử dụng ở các bước phía sau.
                GeminiApiClient.Result completed = continueIfNeeded(
                        result,
                        promptToSend
                );

                // Chuyển callback về UI thread vì chỉ UI thread mới được cập nhật View.
                runOnUiThread(() -> {
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (isFinishing() || isDestroyed()) {
                        // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                        return;
                    }
                    // Cập nhật giá trị `isSending` (is sending) theo trạng thái/kết quả vừa tính được.
                    isSending = false;
                    // Gọi hàm `setSendingState()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    setSendingState(false);
                    // Gọi hàm `handleApiResponse()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    handleApiResponse(completed, savedDisplayMessage);
                });
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception error) {
                // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
                Log.e(TAG, "Lỗi gọi AI Lập trình", error);
                // Chuyển callback về UI thread vì chỉ UI thread mới được cập nhật View.
                runOnUiThread(() -> {
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (isFinishing() || isDestroyed()) {
                        // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                        return;
                    }
                    // Cập nhật giá trị `isSending` (is sending) theo trạng thái/kết quả vừa tính được.
                    isSending = false;
                    // Gọi hàm `setSendingState()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    setSendingState(false);
                    // Gọi hàm `addMessage()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                    addMessage(buildErrorMessage(error), false, null);
                });
            }
        // Khởi chạy luồng nền vừa tạo.
        }).start();
    }

    /**
     * Ghép tối đa các message gần nhất thành context để AI hiểu cuộc hội thoại trước đó.
     * Giới hạn số message/ký tự giúp prompt không tăng vô hạn.
     */
    private String buildContextualPrompt(String question, boolean hasImages) {
        // Khởi tạo Context Android hiện tại phục vụ cho bước xử lý tiếp theo.
        StringBuilder context = new StringBuilder();
        // Tính/đọc và lưu giá trị `start` (start) để sử dụng ở các bước phía sau.
        int start = Math.max(0, messageList.size() - MAX_CONTEXT_MESSAGES);

        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int i = start; i < messageList.size(); i++) {
            // Tính/đọc và lưu đối tượng/nội dung tin nhắn để sử dụng ở các bước phía sau.
            ProgrammingChatMessage message = messageList.get(i);
            // Tính/đọc và lưu nội dung văn bản để sử dụng ở các bước phía sau.
            String content = message.getContent();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (content == null || content.trim().isEmpty()) {
                // Cập nhật nội dung văn bản theo trạng thái/kết quả vừa tính được.
                content = message.hasImages() ? "[IMAGE]" : "";
            }
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (content.isEmpty()) {
                // Bỏ qua phần còn lại của lượt lặp hiện tại và chuyển sang lượt tiếp theo.
                continue;
            }

            // Tính/đọc và lưu giá trị `line` (line) để sử dụng ở các bước phía sau.
            String line = (message.isUser() ? "USER: " : "AI: ")
                    + content.trim() + "\n";
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (context.length() + line.length() > MAX_CONTEXT_CHARS) {
                // Tính/đọc và lưu giá trị `remaining` (remaining) để sử dụng ở các bước phía sau.
                int remaining = MAX_CONTEXT_CHARS - context.length();
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (remaining > 0) {
                    // Gọi `append()` trên `context` để thực hiện thao tác tương ứng.
                    context.append(line, 0, Math.min(remaining, line.length()));
                }
                // Thoát khỏi vòng lặp hoặc nhánh switch hiện tại.
                break;
            }
            // Gọi `append()` trên `context` để thực hiện thao tác tương ứng.
            context.append(line);
        }

        // Tính/đọc và lưu giá trị `currentQuestion` (current question) để sử dụng ở các bước phía sau.
        String currentQuestion = question == null ? "" : question.trim();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (currentQuestion.isEmpty() && hasImages) {
            // Cập nhật giá trị `currentQuestion` (current question) theo trạng thái/kết quả vừa tính được.
            currentQuestion = "Analyze attached code/error images.";
        }

        // Lấy chuỗi từ resources và lưu vào `prompt` để hỗ trợ đa ngôn ngữ.
        StringBuilder prompt = new StringBuilder(getString(R.string.prog_chat_ai_prefix))
                .append("\nSelected Subject: ")
                .append(language)
                .append("\n\nRECENT CONTEXT:\n")
                .append(context.length() == 0 ? "(No previous context)\n" : context)
                .append("\nCURRENT REQUEST:\n")
                .append(currentQuestion);

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (hasImages) {
            // Gọi `append()` trên `prompt` để thực hiện thao tác tương ứng.
            prompt.append("\n\n").append(getString(R.string.prog_chat_image_prompt));
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return prompt.toString();
    }

    /** Nếu Gemini dừng vì MAX_TOKENS thì yêu cầu AI viết tiếp, tối đa vài lần. */
    private GeminiApiClient.Result continueIfNeeded(
            GeminiApiClient.Result initial,
            String originalPrompt
    ) throws Exception {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (initial == null || !initial.wasTruncated()) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return initial;
        }

        // Tính/đọc và lưu giá trị `answer` (answer) để sử dụng ở các bước phía sau.
        String answer = initial.text;
        // Tính/đọc và lưu giá trị `promptTokens` (prompt tokens) để sử dụng ở các bước phía sau.
        int promptTokens = initial.promptTokens;
        // Tính/đọc và lưu giá trị `candidateTokens` (candidate tokens) để sử dụng ở các bước phía sau.
        int candidateTokens = initial.candidateTokens;
        // Tính/đọc và lưu giá trị `totalTokens` (total tokens) để sử dụng ở các bước phía sau.
        int totalTokens = initial.totalTokens;
        // Tính/đọc và lưu giá trị `finishReason` (finish reason) để sử dụng ở các bước phía sau.
        String finishReason = initial.finishReason;

        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int i = 0; i < MAX_CONTINUATIONS
                && "MAX_TOKENS".equalsIgnoreCase(finishReason); i++) {
            // Tính/đọc và lưu giá trị `answerTail` (answer tail) để sử dụng ở các bước phía sau.
            String answerTail = answer.length() > 5_000
                    ? answer.substring(answer.length() - 5_000)
                    : answer;

            // Lấy chuỗi từ resources và lưu vào `continuationPrompt` để hỗ trợ đa ngôn ngữ.
            String continuationPrompt = getString(R.string.prog_chat_ai_prefix)
                    + "\n\nThis is the original request:\n"
                    + trimToLength(originalPrompt, 7_000)
                    + "\n\nThis is the end of the partial answer:\n"
                    + answerTail
                    + "\n\nPlease continue exactly from where it left off. "
                    + "Do not repeat the previous parts and do not restart the introduction.";

            // Gọi Gemini client để gửi prompt và nhận nội dung AI trả về.
            GeminiApiClient.Result continuation = geminiClient.generateText(
                    continuationPrompt,
                    // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                    Collections.emptyList(),
                    // Sử dụng tiện ích Collections để sắp xếp/biến đổi danh sách.
                    Collections.emptyList(),
                    0.35,
                    4096
            );

            // Cập nhật giá trị `answer` (answer) theo trạng thái/kết quả vừa tính được.
            answer = mergeContinuation(answer, continuation.text);
            // Cộng thêm giá trị mới vào `promptTokens` và lưu lại tổng mới.
            promptTokens += continuation.promptTokens;
            // Cộng thêm giá trị mới vào `candidateTokens` và lưu lại tổng mới.
            candidateTokens += continuation.candidateTokens;
            // Cộng thêm giá trị mới vào `totalTokens` và lưu lại tổng mới.
            totalTokens += continuation.totalTokens;
            // Cập nhật giá trị `finishReason` (finish reason) theo trạng thái/kết quả vừa tính được.
            finishReason = continuation.finishReason;
        }

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new GeminiApiClient.Result(
                answer,
                promptTokens,
                candidateTokens,
                totalTokens,
                initial.status,
                finishReason
        );
    }

    // Hàm `mergeContinuation()`: Thực hiện chức năng `mergeContinuation` của class này.
    private String mergeContinuation(String original, String continuation) {
        // Tính/đọc và lưu giá trị `left` (left) để sử dụng ở các bước phía sau.
        String left = original == null ? "" : original.trim();
        // Tính/đọc và lưu giá trị `right` (right) để sử dụng ở các bước phía sau.
        String right = continuation == null ? "" : continuation.trim();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (left.isEmpty()) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return right;
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (right.isEmpty() || left.endsWith(right)) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return left;
        }

        // Tính/đọc và lưu giá trị `maxOverlap` (max overlap) để sử dụng ở các bước phía sau.
        int maxOverlap = Math.min(600, Math.min(left.length(), right.length()));
        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
        for (int overlap = maxOverlap; overlap >= 20; overlap--) {
            // Tính/đọc và lưu giá trị `leftEnd` (left end) để sử dụng ở các bước phía sau.
            String leftEnd = left.substring(left.length() - overlap);
            // Tính/đọc và lưu giá trị `rightStart` (right start) để sử dụng ở các bước phía sau.
            String rightStart = right.substring(0, overlap);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (leftEnd.equals(rightStart)) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return left + right.substring(overlap);
            }
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return left + "\n" + right;
    }

    // Hàm `trimToLength()`: Thực hiện chức năng `trimToLength` của class này.
    private String trimToLength(String value, int maxLength) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (value == null || value.length() <= maxLength) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return value == null ? "" : value;
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return value.substring(value.length() - maxLength);
    }

    // Hàm `buildErrorMessage()`: Xây dựng đối tượng/dữ liệu đầu ra từ các tham số hiện có.
    private String buildErrorMessage(Exception error) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (error instanceof GeminiApiClient.ApiException) {
            // Tính/đọc và lưu giá trị `geminiError` (gemini error) để sử dụng ở các bước phía sau.
            GeminiApiClient.ApiException geminiError = (GeminiApiClient.ApiException) error;
            // Tính/đọc và lưu giá trị `status` (status) để sử dụng ở các bước phía sau.
            int status = geminiError.getHttpStatus();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (status == 429) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return getString(R.string.prog_chat_error_generic);
            }
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return getString(R.string.prog_chat_error_api, status, geminiError.getMessage());
        }

        // Tính/đọc và lưu đối tượng/nội dung tin nhắn để sử dụng ở các bước phía sau.
        String message = error.getMessage();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (message != null && message.toLowerCase().contains("blocked")) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return getString(R.string.prog_chat_error_blocked);
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (error instanceof IllegalStateException && error.getCause() != null) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return getString(R.string.prog_chat_error_connect);
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return getString(R.string.prog_chat_error_generic);
    }

    // Hàm `setSendingState()`: Cập nhật giá trị SendingState cho đối tượng hiện tại.
    private void setSendingState(boolean sending) {
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        pbChatLoading.setVisibility(sending ? View.VISIBLE : View.GONE);
        // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
        btnSend.setEnabled(!sending);
        // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
        btnUploadImage.setEnabled(!sending);
        // Bật hoặc khóa khả năng tương tác với View theo trạng thái hiện tại.
        edtQuestion.setEnabled(!sending);
        // Gọi `setAlpha()` để cập nhật thuộc tính/trạng thái của `btnSend`.
        btnSend.setAlpha(sending ? 0.45f : 1f);
        // Gọi `setAlpha()` để cập nhật thuộc tính/trạng thái của `btnUploadImage`.
        btnUploadImage.setAlpha(sending ? 0.45f : 1f);
    }

    /**
     * Nhận text AI, hiển thị lên RecyclerView và persist xuống bảng messages.
     */
    private void handleApiResponse(GeminiApiClient.Result result, String question) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (result == null || result.text == null || result.text.trim().isEmpty()) {
            // Gọi hàm `addMessage()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            addMessage(getString(R.string.prog_chat_ai_no_text), false, null);
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Tính/đọc và lưu giá trị `aiAnswer` (ai answer) để sử dụng ở các bước phía sau.
        String aiAnswer = result.text.trim();
        // Gọi hàm `addMessage()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        addMessage(aiAnswer, false, null);

        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // isUser=false -> messages.is_user = 0, tức message của AI.
            dbHelper.insertMessage(sessionId, aiAnswer, false, null);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (messageList.size() <= 4) {
                // Lấy chuỗi từ resources và lưu vào `sessionName` để hỗ trợ đa ngôn ngữ.
                String sessionName = question == null ? getString(R.string.prog_chat_new_conv) : question.trim();
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (sessionName.length() > 40) {
                    // Cập nhật giá trị `sessionName` (session name) theo trạng thái/kết quả vừa tính được.
                    sessionName = sessionName.substring(0, 40) + "...";
                }
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (!sessionName.isEmpty()) {
                    // Gọi `updateSessionName()` trên `dbHelper` để thực hiện thao tác tương ứng.
                    dbHelper.updateSessionName(sessionId, sessionName);
                }
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi lưu câu trả lời", e);
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (result.wasTruncated()) {
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            Toast.makeText(
                    this,
                    R.string.prog_chat_truncated,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /** Đọc ảnh từ Uri, resize/nén để giảm RAM và kích thước request gửi Gemini. */
    private byte[] convertUriToBytes(Uri uri) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (uri == null) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return null;
        }

        try (InputStream boundsStream = getContentResolver().openInputStream(uri)) {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (boundsStream == null) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return null;
            }

            // Khởi tạo danh sách các lựa chọn trả lời phục vụ cho bước xử lý tiếp theo.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // Gọi `decodeStream()` trên `BitmapFactory` để thực hiện thao tác tương ứng.
            BitmapFactory.decodeStream(boundsStream, null, options);

            // Tính/đọc và lưu giá trị `targetSize` (target size) để sử dụng ở các bước phía sau.
            int targetSize = 1280;
            // Tính/đọc và lưu giá trị `sampleSize` (sample size) để sử dụng ở các bước phía sau.
            int sampleSize = 1;
            // Tính/đọc và lưu giá trị `maxDimension` (max dimension) để sử dụng ở các bước phía sau.
            int maxDimension = Math.max(options.outWidth, options.outHeight);
            // Tiếp tục lặp khi điều kiện vẫn còn đúng.
            while (maxDimension / sampleSize > targetSize) {
                sampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = Math.max(1, sampleSize);
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            try (InputStream imageStream = getContentResolver().openInputStream(uri)) {
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (imageStream == null) {
                    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                    return null;
                }
                // Tính/đọc và lưu ảnh Bitmap đã giải mã để sử dụng ở các bước phía sau.
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (bitmap == null) {
                    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                    return null;
                }

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    // Gọi `compress()` trên `bitmap` để thực hiện thao tác tương ứng.
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 82, outputStream);
                    // Gọi `recycle()` trên `bitmap` để thực hiện thao tác tương ứng.
                    bitmap.recycle();
                    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                    return outputStream.toByteArray();
                }
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (OutOfMemoryError error) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Ảnh quá lớn", error);
            // Hiển thị thông báo ngắn cho người dùng về kết quả hoặc lỗi thao tác.
            runOnUiThread(() -> Toast.makeText(
                    this,
                    R.string.prog_chat_error_large_image,
                    Toast.LENGTH_SHORT
            ).show());
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return null;
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception error) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi chuyển đổi ảnh", error);
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return null;
        }
    }

    /** Chỉ cập nhật danh sách/RecyclerView; việc persist DB nằm ở insertMessage(). */
    private void addMessage(String text, boolean isUser, List<String> imageBase64List) {
        // Tính/đọc và lưu giá trị `action` (action) để sử dụng ở các bước phía sau.
        Runnable action = () -> {
            // Khởi tạo đối tượng/nội dung tin nhắn phục vụ cho bước xử lý tiếp theo.
            ProgrammingChatMessage message = new ProgrammingChatMessage(
                    text == null ? "" : text,
                    isUser,
                    imageBase64List
            );
            // Thêm phần tử vừa tạo/xử lý vào danh sách `messageList`.
            messageList.add(message);
            // Tính/đọc và lưu giá trị `position` (position) để sử dụng ở các bước phía sau.
            int position = messageList.size() - 1;
            // Thông báo Adapter cập nhật item tương ứng mà không cần vẽ lại toàn bộ danh sách.
            chatAdapter.notifyItemInserted(position);
            // Gọi `scrollToPosition()` trên `recyclerView` để thực hiện thao tác tương ứng.
            recyclerView.scrollToPosition(position);
        };

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (Thread.currentThread() == getMainLooper().getThread()) {
            // Gọi `run()` trên `action` để thực hiện thao tác tương ứng.
            action.run();
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Chuyển callback về UI thread vì chỉ UI thread mới được cập nhật View.
            runOnUiThread(action);
        }
    }

    // Hàm `showImagePreviewDialog()`: Cập nhật giao diện để hiển thị trạng thái/nội dung tương ứng.
    private void showImagePreviewDialog(String imageBase64) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (imageBase64 == null || imageBase64.isEmpty()) {
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }
        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Giải mã Base64 trở lại byte[] để khôi phục dữ liệu ảnh.
            byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
            // Chuyển byte[] của ảnh thành Bitmap để hiển thị trên ImageView.
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    decodedBytes,
                    0,
                    decodedBytes.length
            );
            // Gọi hàm `showImagePreviewDialog()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            showImagePreviewDialog(bitmap);
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi decode ảnh", e);
        }
    }

    // Hàm `showImagePreviewDialog()`: Cập nhật giao diện để hiển thị trạng thái/nội dung tương ứng.
    private void showImagePreviewDialog(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (inputStream == null) {
                // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                return;
            }
            // Tính/đọc và lưu ảnh Bitmap đã giải mã để sử dụng ở các bước phía sau.
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // Gọi hàm `showImagePreviewDialog()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            showImagePreviewDialog(bitmap);
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi load ảnh", e);
        }
    }

    // Hàm `showImagePreviewDialog()`: Cập nhật giao diện để hiển thị trạng thái/nội dung tương ứng.
    private void showImagePreviewDialog(Bitmap bitmap) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (bitmap == null) {
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (previewDialog != null && previewDialog.isShowing()) {
            // Đóng dialog/loading sau khi tác vụ đã hoàn thành hoặc gặp lỗi.
            previewDialog.dismiss();
        }

        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            previewDialog = new Dialog(
                    this,
                    android.R.style.Theme_Black_NoTitleBar_Fullscreen
            );
            // Gọi `requestWindowFeature()` trên `previewDialog` để thực hiện thao tác tương ứng.
            previewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // Gắn file layout XML làm giao diện cho Activity này.
            previewDialog.setContentView(R.layout.dialog_image_preview);

            // Tính/đọc và lưu giá trị `window` (window) để sử dụng ở các bước phía sau.
            Window window = previewDialog.getWindow();
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (window != null) {
                // Tính/đọc và lưu giá trị `layoutParams` (layout params) để sử dụng ở các bước phía sau.
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                // Gọi `setAttributes()` để cập nhật thuộc tính/trạng thái của `window`.
                window.setAttributes(layoutParams);
            }

            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ImageView ivPreview = previewDialog.findViewById(R.id.iv_preview);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ImageView ivClosePreview = previewDialog.findViewById(R.id.iv_close_preview);
            // Gọi `setImageBitmap()` để cập nhật thuộc tính/trạng thái của `ivPreview`.
            ivPreview.setImageBitmap(bitmap);
            // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
            ivClosePreview.setOnClickListener(v -> previewDialog.dismiss());
            // Gọi `setOnDismissListener()` để cập nhật thuộc tính/trạng thái của `previewDialog`.
            previewDialog.setOnDismissListener(dialog -> previewDialog = null);
            // Hiển thị dialog/loading lên màn hình.
            previewDialog.show();
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi hiển thị preview", e);
        }
    }

    @Override
    // Hàm `onDestroy()`: Giải phóng tài nguyên khi đối tượng/màn hình bị hủy.
    protected void onDestroy() {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (previewDialog != null && previewDialog.isShowing()) {
            // Đóng dialog/loading sau khi tác vụ đã hoàn thành hoặc gặp lỗi.
            previewDialog.dismiss();
            // Cập nhật giá trị `previewDialog` (preview dialog) theo trạng thái/kết quả vừa tính được.
            previewDialog = null;
        }
        // Gọi implementation `onDestroy()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onDestroy();
    }
}
