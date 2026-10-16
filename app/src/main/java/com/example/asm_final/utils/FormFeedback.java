package com.example.asm_final.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;

public final class FormFeedback {
    private FormFeedback() {}
    private static TextInputLayout container(EditText field) {
        ViewParent parent = field.getParent();
        while (parent != null) {
            if (parent instanceof TextInputLayout) return (TextInputLayout)parent;
            parent = parent.getParent();
        }
        return null;
    }
    public static void bind(EditText... fields) {
        for (EditText field : fields) field.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            public void onTextChanged(CharSequence text, int start, int before, int count) { clear(field); }
            public void afterTextChanged(Editable text) {}
        });
    }
    public static void clear(EditText... fields) {
        for (EditText field : fields) {
            TextInputLayout layout = container(field);
            if (layout != null) layout.setError(null);
        }
    }
    public static void error(EditText field, int message) {
        TextInputLayout layout = container(field);
        if (layout != null) layout.setError(field.getContext().getString(message));
    }
    public static void focus(EditText field) {
        field.requestFocus();
        field.post(() -> field.requestRectangleOnScreen(new android.graphics.Rect(0, 0, field.getWidth(), field.getHeight()), false));
    }
}
