package com.example.aimentor.activities;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.aimentor.R;
import com.example.aimentor.repository.UserRepository;
import com.example.aimentor.ui.AuthForm;
import com.example.aimentor.ui.InsetActivity;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends InsetActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        TextInputLayout username = findViewById(R.id.inputUsername);
        TextInputLayout email = findViewById(R.id.inputEmail);
        TextInputLayout password = findViewById(R.id.inputPassword);
        TextInputLayout phone = findViewById(R.id.inputPhone);
        Button submit = findViewById(R.id.btnSubmit);
        UserRepository repository = new UserRepository(this);
        AuthForm.clearErrorOnEdit(username, email, password, phone);
        AuthForm.submitOnDone(phone.getEditText(), submit);
        findViewById(R.id.tvLogin).setOnClickListener(v -> returnToLogin());
        submit.setOnClickListener(v -> {
            if (!AuthForm.require(username, R.string.auth_username_required)
                    || !AuthForm.require(email, R.string.auth_email_required)
                    || !AuthForm.require(password, R.string.auth_password_required)) return;
            long inserted = repository.saveUserAccount(AuthForm.value(username),
                    AuthForm.value(password), AuthForm.value(email), AuthForm.value(phone));
            if (inserted == -1) {
                Toast.makeText(this, R.string.auth_signup_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, R.string.auth_signup_success, Toast.LENGTH_SHORT).show();
            returnToLogin();
        });
    }
    private void returnToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
