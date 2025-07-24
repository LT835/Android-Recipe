package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.entity.User;
import com.example.myapplication.netrequest.OkHttpUser;
import java.io.IOException;

public class login extends AppCompatActivity {
    // 登录表单相关视图
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewSwitch;
    private LinearLayout loginForm;

    // 注册表单相关视图
    private TextInputEditText editTextRegUsername;
    private TextInputEditText editTextRegPassword;
    private TextInputEditText editTextConfirmPassword;
    private TextInputEditText editTextEmail;
    private Button buttonRegister;
    private TextView textViewSwitchToLogin;
    private LinearLayout registerForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化登录表单视图
        loginForm = findViewById(R.id.loginForm);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSwitch = findViewById(R.id.textViewSwitch);

        // 初始化注册表单视图
        registerForm = findViewById(R.id.registerForm);
        editTextRegUsername = findViewById(R.id.editTextRegUsername);
        editTextRegPassword = findViewById(R.id.editTextRegPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewSwitchToLogin = findViewById(R.id.textViewSwitchToLogin);

        // 登录按钮点击事件
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // 输入验证
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(login.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(login.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 发起登录请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpUser okHttpUser = new OkHttpUser();
                        try {
                            Log.d("login", "开始登录请求");
                            User user = okHttpUser.userLogin(username, password);

                            if (user == null) {
                                Log.e("login", "登录失败：服务器返回空数据");
                                runOnUiThread(() -> {
                                    Toast.makeText(login.this, "登录失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }

                            if (user.getId() <= 0) {
                                Log.e("login", "登录失败：用户ID无效");
                                runOnUiThread(() -> {
                                    Toast.makeText(login.this, "登录失败，用户名或密码错误", Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }

                            Log.d("login", "登录成功，准备保存用户数据");
                            // 存储用户信息到本地
                            saveUserToLocal(user);

                            // 验证保存的数据
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                            int savedUserId = sharedPreferences.getInt("id", -1);

                            if (!isLoggedIn || savedUserId != user.getId()) {
                                Log.e("login",
                                        "用户数据保存失败 - isLoggedIn: " + isLoggedIn + ", savedUserId: " + savedUserId);
                                runOnUiThread(() -> {
                                    Toast.makeText(login.this, "登录失败，请重试", Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }

                            Log.d("login", "用户数据保存成功，准备跳转到主页面");
                            // 登录成功后跳转到主页面
                            runOnUiThread(() -> {
                                Intent intent = new Intent(login.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });

                        } catch (Exception e) {
                            Log.e("login", "登录过程发生异常", e);
                            runOnUiThread(() -> {
                                Toast.makeText(login.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }).start();
            }
        });

        // 注册按钮点击事件
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextRegUsername.getText().toString().trim();
                String password = editTextRegPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();

                // 输入验证
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(login.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(login.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(login.this, "请确认密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(login.this, "请输入邮箱", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(login.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(login.this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 发起注册请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpUser okHttpUser = new OkHttpUser();
                        try {
                            // 创建User对象
                            User user = new User();
                            user.setUsername(username);
                            user.setPassword(password);
                            user.setEmail(email);

                            boolean success = okHttpUser.userRegister(user);
                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(login.this, "注册成功！", Toast.LENGTH_SHORT).show();
                                    // 注册成功后切换到登录表单
                                    switchToLoginForm();
                                } else {
                                    Toast.makeText(login.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(login.this, "网络错误，请重试", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }).start();
            }
        });

        // 切换到注册表单
        textViewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToRegisterForm();
            }
        });

        // 切换到登录表单
        textViewSwitchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToLoginForm();
            }
        });
    }

    // 切换到注册表单
    private void switchToRegisterForm() {
        loginForm.setVisibility(View.GONE);
        registerForm.setVisibility(View.VISIBLE);
        // 清空登录表单
        editTextUsername.setText("");
        editTextPassword.setText("");
    }

    // 切换到登录表单
    private void switchToLoginForm() {
        registerForm.setVisibility(View.GONE);
        loginForm.setVisibility(View.VISIBLE);
        // 清空注册表单
        editTextRegUsername.setText("");
        editTextRegPassword.setText("");
        editTextConfirmPassword.setText("");
        editTextEmail.setText("");
    }

    // 存储用户信息到本地
    private void saveUserToLocal(User user) {
        if (user == null || user.getId() <= 0) {
            Log.e("login", "Invalid user data: " + (user == null ? "user is null" : "user id is invalid"));
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putInt("id", user.getId());
        // 处理age可能为null的情况
        Integer age = user.getAge();
        editor.putInt("age", age != null ? age : 0);
        editor.putString("sex", user.getSex());
        editor.putBoolean("isLoggedIn", true); // 添加登录状态标志
        editor.apply();

        // 验证保存是否成功
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        int userId = sharedPreferences.getInt("id", -1);
        Log.d("login", "Saved user data - isLoggedIn: " + isLoggedIn + ", userId: " + userId);
    }
}
