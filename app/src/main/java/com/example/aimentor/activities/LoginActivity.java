package com.example.aimentor.activities;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.Nullable;
import com.example.aimentor.R;
import com.example.aimentor.models.UserModel;
import com.example.aimentor.repository.UserRepository;
import com.example.aimentor.ui.AuthForm;
import com.example.aimentor.ui.InsetActivity;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends InsetActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_layout_login);
        TextInputLayout username = findViewById(R.id.inputUsername);
        TextInputLayout password = findViewById(R.id.inputPassword);
        Button submit = findViewById(R.id.btnSubmit);
        UserRepository repository = new UserRepository(this);
        AuthForm.clearErrorOnEdit(username, password);
        AuthForm.submitOnDone(password.getEditText(), submit);
        findViewById(R.id.tvSignUp).setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        submit.setOnClickListener(v -> {
            if (!AuthForm.require(username, R.string.auth_username_required)
                    || !AuthForm.require(password, R.string.auth_password_required)) return;
            UserModel user = repository.loginUser(AuthForm.value(username), AuthForm.value(password));
            if (user == null || user.getId() <= 0) {
                password.setError(getString(R.string.auth_invalid_account));
                return;
            }
            SharedPreferences preferences = getSharedPreferences("USER_INFO", MODE_PRIVATE);
            preferences.edit().putInt("ID_USER", user.getId())
                    .putString("USERNAME_USER", user.getUsername())
                    .putString("EMAIL_USER", user.getEmail())
                    .putInt("ROLE_USER", user.getRole()).apply();
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("ID_ACCOUNT", user.getId());
            intent.putExtra("USER_ACCOUNT", user.getUsername());
            intent.putExtra("EMAIL_ACCOUNT", user.getEmail());
            startActivity(intent);
            finish();
        });
    }
}
