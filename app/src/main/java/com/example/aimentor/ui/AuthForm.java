package com.example.aimentor.ui;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;

/** Shared field feedback and keyboard submission for both authentication forms. */
public final class AuthForm {
    private AuthForm() { }
    public static void clearErrorOnEdit(TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            field.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override public void afterTextChanged(Editable s) { field.setError(null); }
            });
        }
    }
    public static boolean require(TextInputLayout field, int message) {
        if (value(field).isEmpty()) {
            field.setError(field.getContext().getString(message));
            field.getEditText().requestFocus();
            return false;
        }
        return true;
    }
    public static String value(TextInputLayout field) {
        return field.getEditText().getText().toString().trim();
    }
    public static void submitOnDone(EditText lastField, Button submit) {
        lastField.setOnEditorActionListener((view, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) {
                submit.performClick();
                return true;
            }
            return false;
        });
    }
}
