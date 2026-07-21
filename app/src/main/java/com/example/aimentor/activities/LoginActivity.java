package com.example.aimentor.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.MainActivity;
import com.example.aimentor.R;
import com.example.aimentor.models.UserModel;
import com.example.aimentor.repository.UserRepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    EditText edtUsername, edtPassword;
    Button btnLogin;
    UserRepository userRepository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_layout_login);
        btnLogin = findViewById(R.id.btnSubmit); // tim phan tu ngoai giao dien
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        userRepository = new UserRepository(LoginActivity.this);

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // nguoi dung chua co tk de dang nhap - can tao tai khoan
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        // bat su kien cho button - khi nguoi dung click vao
        checkLoginUser();
    }
    private void checkLoginUser(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edtUsername.getText().toString().trim();
                if (TextUtils.isEmpty(username)){
                    edtUsername.setError("Username is required");
                    return;
                }
                String password = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)){
                    edtPassword.setError("Password is required");
                    return;
                }
                // xu ly kiem tra xem tai khoan co ton tai trong co so du lieu hay ko?
                UserModel user = userRepository.loginUser(username, password);
                assert  user != null;
                if (user.getId() > 0 && !TextUtils.isEmpty(user.getUsername())){
                    // dang nhap thanh cong
                    // luu thong tin tai khoan - de xu ly o nhung man hinh khac
                    SharedPreferences sharePf = getSharedPreferences("USER_INFO", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharePf.edit();
                    editor.putInt("ID_USER", user.getId());
                    editor.putString("USERNAME_USER", user.getUsername());
                    editor.putString("EMAIL_USER", user.getEmail());
                    editor.putInt("ROLE_USER", user.getRole());
                    editor.apply();
                    // chuyen sang man hinh menu
                    Intent menu = new Intent(LoginActivity.this, MenuActivity.class);
                    // gui them du lieu sang man hinh khac
                    Bundle bundle = new Bundle();
                    bundle.putInt("ID_ACCOUNT", user.getId());
                    bundle.putString("USER_ACCOUNT", user.getUsername());
                    bundle.putString("EMAIL_ACCOUNT", user.getEmail());
                    menu.putExtras(bundle);
                    startActivity(menu);
                    finish();
                } else {
                    // dang nhap that bai
                    Toast.makeText(LoginActivity.this, "Account invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void checkLoginWithFileData(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(username)){
                    edtUsername.setError("Username is required");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    edtPassword.setError("Password is required");
                    return;
                }
                // xu ly doc du lieu tu file
                try {
                    FileInputStream inputStream = openFileInput("account.txt");
                    StringBuilder builder = new StringBuilder();
                    int read = -1;
                    while ((read = inputStream.read()) != -1){
                        builder.append((char) read);
                    }
                    inputStream.close();// dong file da mo lai
                    String[] infoAccount = null; // mang rong chua thong tin tai khoan
                    infoAccount = builder.toString().trim().split("\n");
                    boolean checkLogin = false;
                    for (int i = 0; i <infoAccount.length; i++){
                        String user = infoAccount[i].substring(0, infoAccount[i].indexOf("|"));
                        String pass = infoAccount[i].substring(infoAccount[i].indexOf("|")+1);
                        if (user.equals(username) && pass.equals(password)){
                            checkLogin = true;
                            break;
                        }
                    }
                    if (checkLogin){
                        // dang nhap thanh cong
                        Intent intentLogin = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intentLogin);
                        finish();// khong cho back lai
                    } else {
                        // dang nhap that bai - sai thong tin tai khoan
                        Toast.makeText(LoginActivity.this, "Account Invalid",Toast.LENGTH_SHORT).show();
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
