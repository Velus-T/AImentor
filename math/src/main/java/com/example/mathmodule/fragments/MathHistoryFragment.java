package com.example.mathmodule.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathmodule.R;
import com.example.mathmodule.adapter.MathHistoryAdapter;
import com.example.mathmodule.databases.MathHistoryItem;
import com.example.mathmodule.databases.SqliteDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Role: Fragment for Math, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public class MathHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private MathHistoryAdapter adapter;
    private SqliteDbHelper dbHelper;

    private List<MathHistoryItem> fullHistoryList;
    private List<MathHistoryItem> displayList;

    // Khai báo 3 nút lọc
    private Button btnAll, btnToday, btn7Days;

    /*
     * Responsibility: Handles the Android callback or event `onCreateView` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_math_history, container, false);
        view.findViewById(R.id.btn_math_module_back)
                .setOnClickListener(v -> requireActivity().finish());

        recyclerView = view.findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new SqliteDbHelper(getContext());

        // Ánh xạ 3 nút lọc từ XML
        btnAll = view.findViewById(R.id.btn_filter_all);
        btnToday = view.findViewById(R.id.btn_filter_today);
        btn7Days = view.findViewById(R.id.btn_filter_7days);

        // Lấy toàn bộ danh sách gốc từ SQLite
        fullHistoryList = dbHelper.getAllHistory();
        displayList = new ArrayList<>();

        // Khởi tạo Adapter với danh sách rỗng ban đầu
        adapter = new MathHistoryAdapter(displayList);
        recyclerView.setAdapter(adapter);

        // Chạy cài đặt bộ lọc
        setupFilters();

        return view;
    }

    /*
     * Responsibility: Configures `setup filters` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupFilters() {
        // Kiểm tra xem oni-chan đã dán UI nút bấm vào XML chưa, nếu chưa thì hiển thị tất cả tránh crash app
        if (btnAll == null || btnToday == null || btn7Days == null) {
            adapter.updateData(fullHistoryList);
            return;
        }

        btnAll.setOnClickListener(v -> {
            updateButtonColors(btnAll, btnToday, btn7Days);
            filterData("ALL");
        });

        btnToday.setOnClickListener(v -> {
            updateButtonColors(btnToday, btnAll, btn7Days);
            filterData("TODAY");
        });

        btn7Days.setOnClickListener(v -> {
            updateButtonColors(btn7Days, btnAll, btnToday);
            filterData("7DAYS");
        });

        // Tự động bấm chọn nút "Tất cả" khi vừa mở màn hình
        btnAll.performClick();
    }

    /*
     * Responsibility: Performs the `filter data` responsibility of `MathHistoryFragment` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void filterData(String filterType) {
        displayList.clear();

        // Lấy thời gian hiện tại
        long todayMillis = System.currentTimeMillis();
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000;
        long past7DaysThreshold = todayMillis - sevenDaysMillis;

        // Chuẩn hóa chuỗi ngày hôm nay
        SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        String todayString = sdfOutput.format(new java.util.Date(todayMillis));

        // Bộ dò đa định dạng: Chấp mọi kiểu lưu ngày của SQLite
        String[] possibleFormats = {
                "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy",
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd",
                "MM/dd/yyyy", "dd-MM-yyyy"
        };

        for (MathHistoryItem item : fullHistoryList) {
            if (filterType.equals("ALL")) {
                displayList.add(item);
                continue;
            }

            String timestamp = item.getTimestamp();
            if (timestamp == null || timestamp.isEmpty()) continue;

            java.util.Date itemDate = null;

            // Thử ép kiểu ngày để xem SQLite đang dùng định dạng nào
            for (String format : possibleFormats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, java.util.Locale.getDefault());
                    itemDate = sdf.parse(timestamp);
                    if (itemDate != null) break; // Parse thành công là thoát vòng lặp
                } catch (Exception ignored) {
                }
            }

            // Bắt đầu phân loại sau khi đã lấy được ngày chuẩn
            if (itemDate != null) {
                if (filterType.equals("TODAY")) {
                    String itemDateString = sdfOutput.format(itemDate);
                    if (itemDateString.equals(todayString)) {
                        displayList.add(item);
                    }
                } else if (filterType.equals("7DAYS")) {
                    if (itemDate.getTime() >= past7DaysThreshold) {
                        displayList.add(item);
                    }
                }
            } else {
                // Đề phòng chuỗi thời gian dị biệt, so sánh chay bằng chữ
                if (filterType.equals("TODAY") && timestamp.contains(todayString)) {
                    displayList.add(item);
                }
            }
        }

        adapter.updateData(displayList);
    }

    /*
     * Responsibility: Updates or synchronizes `update button colors` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateButtonColors(Button active, Button... inactives) {
        // Nút đang được chọn: Nền xanh lá (hoặc primary history), chữ trắng
        active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.math_history_icon)));
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.math_active_text));

        // Các nút không được chọn: Nền theme surface/button_bg, chữ theme text
        for (Button inactive : inactives) {
            inactive.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.math_button_inactive_bg)));
            inactive.setTextColor(ContextCompat.getColor(requireContext(), R.color.math_button_inactive_text));
        }
    }
}