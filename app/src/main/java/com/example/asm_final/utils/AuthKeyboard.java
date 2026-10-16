package com.example.asm_final.utils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Button;

/** Allows submitting both authentication forms from the final keyboard action. */
public final class AuthKeyboard {
    private AuthKeyboard() { }
    public static void bind(EditText field, Button submit) {
        field.setOnEditorActionListener((view, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) {
                submit.performClick();
                return true;
            }
            return false;
        });
    }
}
