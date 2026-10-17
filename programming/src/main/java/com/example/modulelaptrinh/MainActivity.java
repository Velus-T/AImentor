package com.example.modulelaptrinh;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.modulelaptrinh.fragments.ChatHistoryFragment;
import com.example.modulelaptrinh.fragments.HomeFragment;
import com.example.modulelaptrinh.fragments.NoteFragment;
import com.example.modulelaptrinh.fragments.QuizFragment;
import com.example.modulelaptrinh.fragments.QuizSelectionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * ACTIVITY GỐC CỦA PROGRAMMING MODULE.
 *
 * <p>Nhiệm vụ chính:</p>
 * <ul>
 *   <li>Nhận {@code userId} từ app chính qua Intent.</li>
 *   <li>Điều hướng 4 tab: Home, History, Notes, Quiz.</li>
 *   <li>Mở {@link com.example.modulelaptrinh.fragments.QuizFragment} khi người dùng bắt đầu Quiz.</li>
 * </ul>
 *
 * <p>Luồng cần nhớ: App chính -> ProgrammingFeature.open() -> MainActivity -> Fragment.</p>
 */
public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "programming_user_id";
    private boolean confirmedQuizExit;

    // Tham chiếu `bottomNav` tới thanh điều hướng dưới trong layout để đọc/cập nhật giao diện.
    private BottomNavigationView bottomNav;
    // Thuộc tính `toolbar` lưu đối tượng `Toolbar` mà class cần dùng xuyên suốt nhiều hàm.
    private Toolbar toolbar;
    // Biến `currentUserId` lưu giá trị số/trạng thái đếm phục vụ logic của class.
    private int currentUserId;

    @Override
    // Hàm `onCreate()`: Khởi tạo Activity và chuẩn bị các thành phần cần thiết khi màn hình được tạo.
    protected void onCreate(Bundle savedInstanceState) {
        // Gọi implementation `onCreate()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onCreate(savedInstanceState);
        // Gắn file layout XML làm giao diện cho Activity này.
        setContentView(R.layout.activity_programming_main);

        // userId là khóa để Chat/Note/Quiz/EXP tách riêng theo tài khoản.
        currentUserId = Math.max(0, getIntent().getIntExtra(EXTRA_USER_ID, 0));

        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        toolbar = findViewById(R.id.toolbar);
        // Gọi hàm `setSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        setSupportActionBar(toolbar);
        // Gọi `setNavigationOnClickListener()` để cập nhật thuộc tính/trạng thái của `toolbar`.
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
        bottomNav = findViewById(R.id.bottom_nav);

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (savedInstanceState == null) {
            // Gọi `setSelectedItemId()` để cập nhật thuộc tính/trạng thái của `bottomNav`.
            bottomNav.setSelectedItemId(R.id.nav_home);
            // Gọi hàm `showRootFragment()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            showRootFragment(new HomeFragment(), getString(R.string.prog_tab_home));
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Gọi hàm `updateTitleForSelectedItem()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            updateTitleForSelectedItem(bottomNav.getSelectedItemId());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            // Tính/đọc và lưu giá trị `itemId` (item id) để sử dụng ở các bước phía sau.
            int itemId = item.getItemId();
            Fragment active = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            com.example.mentor.ui.QuizFormView quiz = active == null || active.getView() == null ? null
                    : active.getView().findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
            if (!confirmedQuizExit && itemId != bottomNav.getSelectedItemId() && quiz != null
                    && quiz.getVisibility() == android.view.View.VISIBLE && !quiz.submitted()) {
                quiz.confirmReplacement(() -> {
                    confirmedQuizExit = true;
                    bottomNav.setSelectedItemId(itemId);
                    confirmedQuizExit = false;
                });
                return false;
            }
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (itemId == R.id.nav_home) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return showRootFragment(new HomeFragment(), getString(R.string.prog_tab_home));
            }
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (itemId == R.id.nav_history) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return showRootFragment(new ChatHistoryFragment(), getString(R.string.prog_tab_history));
            }
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (itemId == R.id.nav_notes) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return showRootFragment(new NoteFragment(), getString(R.string.prog_tab_notes));
            }
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (itemId == R.id.nav_quiz) {
                // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                return showRootFragment(new QuizSelectionFragment(), getString(R.string.prog_tab_quiz));
            }
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            // Hàm `handleOnBackPressed()`: Thực hiện chức năng `handleOnBackPressed` của class này.
            public void handleOnBackPressed() {
                com.example.mentor.ui.QuizFormView form = findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
                if (!confirmedQuizExit && form != null && form.getVisibility() == android.view.View.VISIBLE
                        && !form.submitted()) {
                    form.confirmReplacement(() -> {
                        confirmedQuizExit = true;
                        handleOnBackPressed();
                        confirmedQuizExit = false;
                    });
                    return;
                }
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    // Lấy FragmentManager để thực hiện chuyển/replace Fragment.
                    getSupportFragmentManager().popBackStack();
                    // Gọi `setSelectedItemId()` để cập nhật thuộc tính/trạng thái của `bottomNav`.
                    bottomNav.setSelectedItemId(R.id.nav_quiz);
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (getSupportActionBar() != null) {
                        // Gọi hàm `getSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                        getSupportActionBar().setTitle(R.string.prog_tab_quiz);
                    }
                    // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
                    return;
                }
                // Gọi hàm `finish()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                finish();
            }
        });
    }

    // Hàm `getCurrentUserId()`: Trả về giá trị CurrentUserId hiện đang được đối tượng lưu giữ.
    public int getCurrentUserId() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return currentUserId;
    }

    /** Thay Fragment hiện tại khi người dùng bấm bottom navigation. */
    private boolean showRootFragment(Fragment fragment, String title) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (fragment == null) {
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return false;
        }
        Fragment existing = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (existing != null && existing.getClass() == fragment.getClass()) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
            return true;
        }

        // Lấy FragmentManager để thực hiện chuyển/replace Fragment.
        getSupportFragmentManager().popBackStackImmediate();
        // Lấy FragmentManager để thực hiện chuyển/replace Fragment.
        getSupportFragmentManager()
                // Bắt đầu transaction để nhóm nhiều thao tác database thành một khối nguyên tử.
                .beginTransaction()
                // Thay Fragment hiện tại trong container bằng Fragment mới.
                .replace(R.id.fragment_container, fragment)
                // Commit transaction để Android thực sự áp dụng thay đổi Fragment.
                .commit();

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (getSupportActionBar() != null) {
            // Gọi hàm `getSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            getSupportActionBar().setTitle(title);
        }
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return true;
    }

    /**
     * Nhận cấu hình Quiz từ QuizSelectionFragment và truyền sang QuizFragment bằng Bundle.
     */
    public void openQuiz(String subject, String topic, String level, int questionCount) {
        // Tính/đọc và lưu giá trị `fragment` (fragment) để sử dụng ở các bước phía sau.
        QuizFragment fragment = QuizFragment.newInstance(subject, topic, level, questionCount);
        // Lấy FragmentManager để thực hiện chuyển/replace Fragment.
        getSupportFragmentManager()
                // Bắt đầu transaction để nhóm nhiều thao tác database thành một khối nguyên tử.
                .beginTransaction()
                // Thay Fragment hiện tại trong container bằng Fragment mới.
                .replace(R.id.fragment_container, fragment)
                // Đưa thao tác Fragment vào back stack để nút Back có thể quay lại màn hình trước.
                .addToBackStack("programming_quiz")
                // Commit transaction để Android thực sự áp dụng thay đổi Fragment.
                .commit();

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (getSupportActionBar() != null) {
            // Gọi hàm `getSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            getSupportActionBar().setTitle(getString(R.string.quiz_title_format, topic));
        }
    }

    // Hàm `updateTitleForSelectedItem()`: Cập nhật dữ liệu/trạng thái hiện tại theo giá trị mới.
    private void updateTitleForSelectedItem(int itemId) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (getSupportActionBar() == null) {
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (itemId == R.id.nav_history) {
            // Gọi hàm `getSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            getSupportActionBar().setTitle(R.string.prog_tab_history);
        // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
        } else if (itemId == R.id.nav_notes) {
            // Gọi hàm `getSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            getSupportActionBar().setTitle(R.string.prog_tab_notes);
        // Nếu điều kiện trước không thỏa, kiểm tra thêm điều kiện của nhánh này.
        } else if (itemId == R.id.nav_quiz) {
            // Gọi hàm `getSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            getSupportActionBar().setTitle(R.string.prog_tab_quiz);
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Gọi hàm `getSupportActionBar()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            getSupportActionBar().setTitle(R.string.prog_tab_home);
        }
    }
}
