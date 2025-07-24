package com.example.myapplication.netrequest;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.example.myapplication.entity.Favorite;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import java.io.IOException;
import java.util.List;

public class OkHttpFavorite {
    private static final String BASE_URL = "http://192.168.10.1:8080/api/favorites";
    private final OkHttpClient client = new OkHttpClient();

    // 检查菜谱是否已收藏
    public boolean checkFavorite(long userId, long recipeId) throws IOException {
        String url = BASE_URL + "/check/" + userId + "/" + recipeId;
        Log.d("OkHttpFavorite", "检查收藏状态: " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            Log.d("OkHttpFavorite", "收藏状态响应 - 状态码: " + response.code() +
                    ", 响应内容: " + responseBody);

            if (response.isSuccessful() && responseBody != null) {
                return Boolean.parseBoolean(responseBody);
            }
            Log.e("OkHttpFavorite", "检查收藏状态失败 - 状态码: " + response.code() +
                    ", 响应内容: " + responseBody);
            return false;
        } catch (Exception e) {
            Log.e("OkHttpFavorite", "检查收藏状态异常", e);
            throw e;
        }
    }

    // 添加收藏
    public boolean addFavorite(long userId, long recipeId) throws IOException {
        String url = BASE_URL + "/" + userId + "/" + recipeId;
        Log.d("OkHttpFavorite", "添加收藏: " + url);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, new byte[0]))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            boolean success = response.isSuccessful();
            Log.d("OkHttpFavorite", "添加收藏响应 - 状态码: " + response.code() +
                    ", 响应内容: " + responseBody);

            if (!success) {
                Log.e("OkHttpFavorite", "添加收藏失败 - 状态码: " + response.code() +
                        ", 响应内容: " + responseBody);
            }
            return success;
        } catch (Exception e) {
            Log.e("OkHttpFavorite", "添加收藏异常", e);
            throw e;
        }
    }

    // 取消收藏
    public boolean removeFavorite(long userId, long recipeId) throws IOException {
        String url = BASE_URL + "/" + userId + "/" + recipeId;
        Log.d("OkHttpFavorite", "取消收藏: " + url);
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            boolean success = response.isSuccessful();
            Log.d("OkHttpFavorite", "取消收藏响应 - 状态码: " + response.code() +
                    ", 响应内容: " + responseBody);

            if (!success) {
                Log.e("OkHttpFavorite", "取消收藏失败 - 状态码: " + response.code() +
                        ", 响应内容: " + responseBody);
            }
            return success;
        } catch (Exception e) {
            Log.e("OkHttpFavorite", "取消收藏异常", e);
            throw e;
        }
    }

    // 获取用户收藏列表
    public List<Favorite> getUserFavorites(long userId) throws IOException {
        String url = BASE_URL + "/user/" + userId;
        Log.d("OkHttpFavorite", "获取用户收藏列表: " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.d("OkHttpFavorite", "收藏列表响应: " + responseBody);
                return JSON.parseArray(responseBody, Favorite.class);
            }
            Log.e("OkHttpFavorite", "获取收藏列表失败: " + response.code());
            return null;
        }
    }
}