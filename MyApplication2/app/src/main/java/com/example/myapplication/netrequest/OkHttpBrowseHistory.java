package com.example.myapplication.netrequest;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.example.myapplication.entity.BrowseHistory;
import com.example.myapplication.entity.Recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpBrowseHistory {
    private static final String TAG = "OkHttpBrowseHistory";
    private static final String BASE_URL = "http://192.168.10.1:8080/api";
    private final OkHttpClient client = new OkHttpClient();

    public boolean addBrowseHistory(Long userId, Long recipeId) throws IOException {
        Log.d(TAG, "开始添加浏览历史 - 用户ID: " + userId + ", 菜谱ID: " + recipeId);

        String url = String.format("%s/browse-history/%d/%d", BASE_URL, userId, recipeId);
        Log.d(TAG, "请求URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json"), ""))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "无响应内容";
            Log.d(TAG, "服务器响应 - 状态码: " + response.code() + ", 响应内容: " + responseBody);

            if (!response.isSuccessful()) {
                Log.e(TAG, "请求失败 - HTTP状态码: " + response.code() + ", 响应内容: " + responseBody);
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "请求异常", e);
            Log.e(TAG, "异常详情: " + e.getMessage());
            throw e;
        }
    }

    public List<Recipe> getUserBrowseHistory(Long userId) throws IOException {
        Log.d(TAG, "获取用户浏览历史 - 用户ID: " + userId);

        String url = String.format("%s/browse-history/user/%d", BASE_URL, userId);
        Log.d(TAG, "请求URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "无响应内容";
            Log.d(TAG, "服务器响应 - 状态码: " + response.code() + ", 响应内容: " + responseBody);

            if (!response.isSuccessful()) {
                Log.e(TAG, "请求失败 - HTTP状态码: " + response.code() + ", 响应内容: " + responseBody);
                return new ArrayList<>();
            }

            // 首先解析浏览历史列表
            List<BrowseHistory> browseHistories = JSON.parseArray(responseBody, BrowseHistory.class);
            Log.d(TAG, "解析到浏览历史记录数量: " + (browseHistories != null ? browseHistories.size() : 0));

            // 获取每个浏览历史对应的菜谱详情
            List<Recipe> recipes = new ArrayList<>();
            if (browseHistories != null) {
                for (BrowseHistory history : browseHistories) {
                    Log.d(TAG, "正在获取菜谱详情 - 菜谱ID: " + history.getRecipeId());
                    Recipe recipe = getRecipeById(history.getRecipeId());
                    if (recipe != null) {
                        Log.d(TAG, "成功获取菜谱: " + recipe.getName());
                        recipes.add(recipe);
                    } else {
                        Log.e(TAG, "获取菜谱失败 - 菜谱ID: " + history.getRecipeId());
                    }
                }
            }

            Log.d(TAG, "最终获取到的菜谱数量: " + recipes.size());
            return recipes;
        } catch (Exception e) {
            Log.e(TAG, "请求异常", e);
            Log.e(TAG, "异常详情: " + e.getMessage());
            throw e;
        }
    }

    private Recipe getRecipeById(Long recipeId) {
        try {
            String url = BASE_URL + "/recipes/" + recipeId;
            Log.d(TAG, "获取菜谱详情 - URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                Log.d(TAG, "菜谱详情响应 - 状态码: " + response.code() + ", 响应内容: " + responseBody);

                if (response.isSuccessful()) {
                    return JSON.parseObject(responseBody, Recipe.class);
                } else {
                    Log.e(TAG, "获取菜谱详情失败 - HTTP状态码: " + response.code());
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取菜谱失败 - 菜谱ID: " + recipeId, e);
            return null;
        }
    }
}