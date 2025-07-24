package com.example.myapplication.netrequest;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import com.example.myapplication.entity.User;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUser {
    private static final String BASE_URL = "http://192.168.10.1:8080/api/users";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    // 用户注册
    public boolean userRegister(User user) throws IOException {
        String json = JSON.toJSONString(user);
        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(BASE_URL + "/register")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        if ("true".equals(responseBody)) {
            return true;
        } else if ("false".equals(responseBody)) {
            return false;
        } else {
            try {
                JSON.parseObject(responseBody);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    // 用户登录
    public User userLogin(String username, String password) throws IOException {
        Log.d("OkHttpUser", "开始登录请求 - 用户名: " + username);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        String json = JSON.toJSONString(user);
        Log.d("OkHttpUser", "请求数据: " + json);

        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            Log.d("OkHttpUser", "服务器响应 - 状态码: " + response.code() + ", 响应内容: " + responseBody);

            if (response.isSuccessful()) {
                User responseUser = JSON.parseObject(responseBody, User.class);
                if (responseUser != null) {
                    Log.d("OkHttpUser", "登录成功 - 用户ID: " + responseUser.getId());
                    return responseUser;
                } else {
                    Log.e("OkHttpUser", "解析用户数据失败");
                    return null;
                }
            } else {
                Log.e("OkHttpUser", "登录请求失败 - 状态码: " + response.code());
                return null;
            }
        } catch (Exception e) {
            Log.e("OkHttpUser", "登录请求异常", e);
            throw e;
        }
    }

    // 修改用户个人信息（sex, age, email）
    public boolean updateUserInfo(int userId, String sex, Integer age, String email) throws IOException {
        // 构建更新请求的 URL，添加查询参数
        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/" + userId + "/update");

        if (sex != null) {
            urlBuilder.append("?sex=").append(sex);
        }
        if (age != null) {
            if (urlBuilder.indexOf("?") > -1) {
                urlBuilder.append("&");
            } else {
                urlBuilder.append("?");
            }
            urlBuilder.append("age=").append(age);
        }
        if (email != null) {
            if (urlBuilder.indexOf("?") > -1) {
                urlBuilder.append("&");
            } else {
                urlBuilder.append("?");
            }
            urlBuilder.append("email=").append(email);
        }

        // 发起 PUT 请求
        Request request = new Request.Builder()
                .url(urlBuilder.toString()) // 使用构建的 URL
                .put(RequestBody.create("", null)) // PUT 请求时没有请求体
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        // 检查返回的响应体，判断是否更新成功
        return "true".equals(responseBody);
    }
}
