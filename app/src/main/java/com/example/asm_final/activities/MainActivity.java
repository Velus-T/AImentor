package com.example.asm_final.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.asm_final.R;

/*
 * Role: Activity for App Shell, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public class MainActivity extends AppCompatActivity {

  private EditText edtUsername, edtPassword;
  private Button btnLogin;
  private TextView tvMessage, tvGoToRegister;
  private com.example.asm_final.accounts.UserAuthenticator accounts;
  private com.example.asm_final.utils.AsyncActionRunner actions;

  /*
   * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
   * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    com.example.asm_final.AppServices services =
        ((com.example.asm_final.AiMentorApplication) getApplication()).services();
    accounts = services.authenticator();
    actions = new com.example.asm_final.utils.AsyncActionRunner(services.io(), this::runOnUiThread);

    edtUsername = findViewById(R.id.edtLoginUsername);
    edtPassword = findViewById(R.id.edtLoginPassword);
    btnLogin = findViewById(R.id.btnLogin);
    tvMessage = findViewById(R.id.tvLoginMessage);
    tvGoToRegister = findViewById(R.id.tvGoToRegister);
    com.example.asm_final.utils.AuthKeyboard.bind(edtPassword, btnLogin);

    com.example.asm_final.utils.FormFeedback.bind(edtUsername, edtPassword);
    btnLogin.setOnClickListener(
        v -> {
          String username = edtUsername.getText().toString().trim();
          String password = edtPassword.getText().toString().trim();

          com.example.asm_final.utils.FormFeedback.clear(edtUsername, edtPassword);
          if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty())
              com.example.asm_final.utils.FormFeedback.error(
                  edtUsername, R.string.ux_required_username);
            if (password.isEmpty())
              com.example.asm_final.utils.FormFeedback.error(
                  edtPassword, R.string.ux_required_password);
            com.example.asm_final.utils.FormFeedback.focus(
                username.isEmpty() ? edtUsername : edtPassword);
            tvMessage.setText("");
            return;
          }

          btnLogin.setEnabled(false);
          tvGoToRegister.setEnabled(false);
          tvMessage.setText(R.string.auth_processing);
          actions.submit(
              () -> accounts.authenticate(username, password),
              authenticated -> {
                btnLogin.setEnabled(true);
                tvGoToRegister.setEnabled(true);
                if (!getLifecycle()
                    .getCurrentState()
                    .isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) return;
                if (authenticated) {
                  com.example.mentor.data.ActiveUser.set(
                      this,
                      ((com.example.asm_final.AiMentorApplication) getApplication())
                          .services()
                          .accountLookup()
                          .userId(username));
                  Toast.makeText(this, R.string.login_msg_success, Toast.LENGTH_SHORT).show();
                  Intent intent = new Intent(this, HomeActivity.class);
                  intent.putExtra("USERNAME", username);
                  startActivity(intent);
                  finish();
                } else {
                  tvMessage.setText(R.string.login_msg_fail);
                }
              },
              error -> {
                btnLogin.setEnabled(true);
                tvGoToRegister.setEnabled(true);
                tvMessage.setText(R.string.auth_storage_error);
              });
        });

    tvGoToRegister.setOnClickListener(
        v -> {
          startActivity(new Intent(this, RegisterActivity.class));
        });
  }

  @Override
  protected void onDestroy() {
    if (actions != null) actions.close();
    super.onDestroy();
  }
}
