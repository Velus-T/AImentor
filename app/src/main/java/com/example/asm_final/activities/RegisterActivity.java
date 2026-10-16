package com.example.asm_final.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.asm_final.R;
import com.example.asm_final.activities.database.DatabaseHelper;

/*
 * Role: Activity for App Shell, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText edtUser, edtPass, edtConfirm, edtEmail, edtPhone;
    private TextView tvMsg;
    private com.example.asm_final.accounts.UserRegistration accounts;
    private com.example.asm_final.utils.AsyncActionRunner actions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        com.example.asm_final.AppServices services = ((com.example.asm_final.AiMentorApplication) getApplication()).services();
        accounts = services.registration();
        actions = new com.example.asm_final.utils.AsyncActionRunner(services.io(), this::runOnUiThread);

        edtUser = findViewById(R.id.edtRegisterUsername);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPhone = findViewById(R.id.edtRegisterPhone);
        edtPass = findViewById(R.id.edtRegisterPassword);
        edtConfirm = findViewById(R.id.edtRegisterConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        com.example.asm_final.utils.AuthKeyboard.bind(edtConfirm, btnRegister);
        tvMsg = findViewById(R.id.tvRegisterMessage);
        TextView tvLogin = findViewById(R.id.tvGoToLogin);

        com.example.asm_final.utils.FormFeedback.bind(edtUser, edtEmail, edtPhone, edtPass, edtConfirm);
        btnRegister.setOnClickListener(v -> {
            String u = edtUser.getText().toString().trim();
            String e = edtEmail.getText().toString().trim();
            String ph = edtPhone.getText().toString().trim();
            String p = edtPass.getText().toString().trim();
            String c = edtConfirm.getText().toString().trim();

            EditText[] fields = {edtUser, edtEmail, edtPhone, edtPass, edtConfirm};
            String[] values = {u, e, ph, p, c};
            int[] messages = {R.string.ux_required_username, R.string.ux_required_email, R.string.ux_required_phone, R.string.ux_required_password, R.string.ux_required_confirm};
            com.example.asm_final.utils.FormFeedback.clear(fields);
            EditText firstMissing = null;
            for (int i = 0; i < fields.length; i++) if (values[i].isEmpty()) {
                com.example.asm_final.utils.FormFeedback.error(fields[i], messages[i]);
                if (firstMissing == null) firstMissing = fields[i];
            }
            if (firstMissing != null) {
                tvMsg.setText(R.string.reg_msg_empty);
                com.example.asm_final.utils.FormFeedback.focus(firstMissing);
                return;
            }

            // Email validation
            if (!e.toLowerCase(java.util.Locale.ROOT).endsWith("@gmail.com")) {
                tvMsg.setText(R.string.reg_msg_invalid_email);
                com.example.asm_final.utils.FormFeedback.error(edtEmail, R.string.reg_msg_invalid_email);
                com.example.asm_final.utils.FormFeedback.focus(edtEmail);
                return;
            }

            // Password validation
            if (!isValidPassword(p)) {
                tvMsg.setText(R.string.reg_msg_weak_pass);
                com.example.asm_final.utils.FormFeedback.error(edtPass, R.string.reg_msg_weak_pass);
                com.example.asm_final.utils.FormFeedback.focus(edtPass);
                return;
            }

            if (!p.equals(c)) {
                tvMsg.setText(R.string.reg_msg_mismatch);
                com.example.asm_final.utils.FormFeedback.error(edtConfirm, R.string.reg_msg_mismatch);
                com.example.asm_final.utils.FormFeedback.focus(edtConfirm);
                return;
            }

            btnRegister.setEnabled(false);
            tvMsg.setText(R.string.auth_processing);
            actions.submit(() -> accounts.register(u,p,e,ph), result -> {
            btnRegister.setEnabled(true);
            if (result != -1) {
                tvMsg.setText("");
                Toast.makeText(this, R.string.reg_msg_success, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                tvMsg.setText(R.string.reg_msg_exists);
                com.example.asm_final.utils.FormFeedback.error(edtUser, R.string.reg_msg_exists);
                com.example.asm_final.utils.FormFeedback.focus(edtUser);
            }
            }, error -> { btnRegister.setEnabled(true); tvMsg.setText(R.string.auth_storage_error); });
        });

        tvLogin.setOnClickListener(v -> finish());
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `is valid password` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    private boolean isValidPassword(String password) { return com.example.asm_final.accounts.PasswordPolicy.isValid(password); }
    @Override protected void onDestroy() {
        if (actions != null) actions.close();
        super.onDestroy();
    }
}
