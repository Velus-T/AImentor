package com.example.aimentor.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.R;
import com.example.aimentor.repository.UserRepository;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class SignUpActivity extends com.example.aimentor.ui.InsetActivity {
    EditText edtUsername, edtPassword, edtEmail, edtPhone;
    Button btnSignup;
    TextView tvLogin;
    UserRepository userRepository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignup   = findViewById(R.id.btnSubmit);
        tvLogin     = findViewById(R.id.tvLogin);
        edtEmail    = findViewById(R.id.edtEmail);
        edtPhone    = findViewById(R.id.edtPhone);
        userRepository = new UserRepository(SignUpActivity.this);

        // vi da co tai khoan roi quay luon ve dang nhap
        // khong muon tao tai khoan moi
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(login); // quay lai dang nhap
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = edtUsername.getText().toString().trim();
                if (TextUtils.isEmpty(user)){
                    edtUsername.setError("Username is required");
                    return;
                }
                String pass = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(pass)) {
                    edtPassword.setError("Password is required");
                    return;
                }
                String email = edtEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    edtEmail.setError("Email is required");
                    return;
                }
                String phone = edtPhone.getText().toString().trim();
                // xu ly luu thong tin tai khoan vao database
                long insert = userRepository.saveUserAccount(user, pass, email, phone);
                if (insert == -1){
                    // insert loi
                    Toast.makeText(SignUpActivity.this, "Signup Fail", Toast.LENGTH_SHORT).show();
                    return;
                }
                // dang ky tai khoan thanh cong
                Toast.makeText(SignUpActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                /*
                // xu ly luu tru thong tin nguoi dung vao file
                FileOutputStream outputStream = null;
                try {
                    user = user + "|"; // ngan cach giua username va password
                    outputStream = openFileOutput("account.txt", Context.MODE_APPEND);
                    outputStream.write(user.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(pass.getBytes(StandardCharsets.UTF_8));
                    outputStream.write('\n'); // xuong dong cho moi cap tai khoan
                    outputStream.close();// dong file
                    edtUsername.setText("");
                    edtPassword.setText("");
                    // cho quay ve trang login
                    Toast.makeText(SignUpActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                 */
            }
        });
    }
}
