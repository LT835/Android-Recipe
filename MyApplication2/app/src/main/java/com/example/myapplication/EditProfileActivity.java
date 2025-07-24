package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.netrequest.OkHttpUser;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private EditText ageEditText, emailEditText;
    private Spinner sexSpinner;
    private Button saveButton;
    private int userId; // 用户ID
    private String username; // 用户名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 设置 Toolbar 为 ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 显示返回按钮
        getSupportActionBar().setTitle("修改个人信息");

        // 获取控件
        ageEditText = findViewById(R.id.age_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        sexSpinner = findViewById(R.id.sex_spinner);
        saveButton = findViewById(R.id.save_button);

        // 初始化性别选择器
        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this,
                R.array.sex_options, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(sexAdapter);

        // 加载用户的当前信息
        loadUserProfile();

        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // 获取当前的用户信息
        userId = sharedPreferences.getInt("id", -1); // 获取用户ID
        username = sharedPreferences.getString("username", ""); // 获取用户名
        int age = sharedPreferences.getInt("age", -1); // 默认值为-1表示未设置
        String email = sharedPreferences.getString("email", "");
        String sex = sharedPreferences.getString("sex", "");

        // 将信息显示在控件中
        ageEditText.setText(age == -1 ? "" : String.valueOf(age));
        emailEditText.setText(email);

        // 设置默认的性别
        if (sex.equals("男")) {
            sexSpinner.setSelection(0);
        } else if (sex.equals("女")) {
            sexSpinner.setSelection(1);
        } else {
            sexSpinner.setSelection(0); // 默认设置为男
        }
    }

    private void saveUserProfile() {
        String newAge = ageEditText.getText().toString();
        String newEmail = emailEditText.getText().toString();
        String newSex = sexSpinner.getSelectedItem().toString();

        // 确保输入的邮箱和年龄是有效的
        if (newAge.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "年龄和邮箱不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 异步更新用户信息
        new Thread(() -> {
            try {
                OkHttpUser okHttpUser = new OkHttpUser();
                boolean updateSuccess = okHttpUser.updateUserInfo(userId, newSex, Integer.parseInt(newAge), newEmail);

                runOnUiThread(() -> {
                    if (updateSuccess) {
                        // 更新SharedPreferences中的个人信息
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("age", Integer.parseInt(newAge)); // 更新年龄
                        editor.putString("email", newEmail); // 更新邮箱
                        editor.putString("sex", newSex); // 更新性别
                        editor.apply();

                        // 提示用户更新成功
                        Toast.makeText(EditProfileActivity.this, "个人信息已保存", Toast.LENGTH_SHORT).show();

                        // 返回到个人信息界面，并传递更新的结果
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedAge", newAge);
                        resultIntent.putExtra("updatedEmail", newEmail);
                        resultIntent.putExtra("updatedSex", newSex);
                        setResult(RESULT_OK, resultIntent);
                        finish(); // 结束当前编辑界面
                    } else {
                        // 提示用户更新失败
                        Toast.makeText(EditProfileActivity.this, "保存失败，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    @Override
    public boolean onSupportNavigateUp() {
        // 点击返回按钮时执行的操作
        onBackPressed();
        return true;
    }
}

