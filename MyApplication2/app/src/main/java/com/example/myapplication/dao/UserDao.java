package com.example.myapplication.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.myapplication.database.DBHelper;


public class UserDao {
    private SharedPreferences sharedPreferences;
    private DBHelper dbHelper;
    private SQLiteDatabase db; // 将数据库实例作为类的字段

    public UserDao(Context context) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase(); // 在构造函数中打开数据库
        sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    // 注册用户
    public boolean registerUser(String username, String password, String email) {
        if (db.isOpen()) {
            Log.d("Database", "Database is open.");
        } else {
            Log.d("Database", "Database is closed.");
            db = dbHelper.getWritableDatabase(); // 重新打开数据库
        }

        try {
            // 检查用户名和邮箱是否已存在
            if (checkUserExists(username) || checkEmailExists(email)) {
                return false; // 用户名或邮箱已存在
            }

            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", password);
            values.put("email", email);  // 存储邮箱

            long result = db.insert("Users", null, values);
            return result != -1; // 插入成功返回 true
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return false;
        }
    }

    // 登录验证
    public boolean loginUser(String username, String password) {
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean result = cursor.getCount() > 0; // 查询到用户
        cursor.close(); // 关闭 Cursor
        if (result) {
            // 登录成功，保存用户名到 SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currentUser", username);
            editor.apply();
        }
        return result;
    }
    // 清除用户登录信息
    public void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("currentUser");
        editor.apply();
    }

    // 检查用户名是否存在
    private boolean checkUserExists(String username) {
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM Users WHERE username = ?", new String[]{username});
            exists = cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close(); // 关闭 Cursor
            }
        }
        return exists;
    }

    // 检查邮箱是否存在
    private boolean checkEmailExists(String email) {
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM Users WHERE email = ?", new String[]{email});
            exists = cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close(); // 关闭 Cursor
            }
        }
        return exists;
    }

    // 获取用户信息
    public String[] getUserInfo(String username) {
        Cursor cursor = null;
        try {
            cursor = db.query("Users", new String[]{"username", "email"}, "username=?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String retrievedUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String retrievedEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                return new String[]{retrievedUsername, retrievedEmail};
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null; // 用户不存在时返回 null
    }

    // 关闭数据库
    public void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close(); // 关闭数据库
        }
    }
}
