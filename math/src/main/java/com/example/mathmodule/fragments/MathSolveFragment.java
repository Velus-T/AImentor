package com.example.mathmodule.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.example.mathmodule.MathChatActivity;
import com.example.mathmodule.R;

/*
 * Role: Fragment for Math, responsible for a specific functional area of the screen and its user interactions.
 * Purpose: Using Fragments gives each tab or feature a focused responsibility and makes the screen flow easier to explain.
 */
public class MathSolveFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Nạp giao diện màn hình Giải Toán AI (Xanh dương) vào Fragment
        View view = inflater.inflate(R.layout.activity_math_solve, container, false);
        view.findViewById(R.id.btn_math_module_back)
                .setOnClickListener(v -> requireActivity().finish());

        // 1. Ánh xạ 2 nút chức năng thông qua biến 'view'
        CardView btnScanMath = view.findViewById(R.id.btn_scan_math);
        CardView btnTypeMath = view.findViewById(R.id.btn_type_math);

        // 2. Xử lý nút "Nhập đề bài" -> Mở phòng Chat AI
        btnTypeMath.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MathChatActivity.class);
            intent.putExtra("ACTION_TYPE", "TYPE_TEXT");
            startActivity(intent);
        });

        // 3. Xử lý nút "Chụp đề bài" -> Mở phòng Chat AI
        btnScanMath.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MathChatActivity.class);
            intent.putExtra("ACTION_TYPE", "OPEN_IMAGE");
            startActivity(intent);
        });

        view.findViewById(R.id.button_math_calculator).setOnClickListener(v -> com.example.mathmodule.calculator.MathCalculator.show(requireActivity()));
        return view; // Trả về view đã hoàn thiện
    }
}
