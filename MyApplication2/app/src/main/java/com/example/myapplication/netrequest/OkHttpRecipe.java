package com.example.myapplication.netrequest;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.myapplication.entity.Recipe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class OkHttpRecipe {
    private static final String BASE_URL = "http://192.168.10.1:8080/api/recipes";
    private final OkHttpClient client = new OkHttpClient();

    // 获取所有菜谱列表
    public List<Recipe> getAllRecipes() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return JSON.parseArray(responseBody, Recipe.class);
            } else {
                Log.e("OkHttpRecipe", "获取菜谱失败，状态码: " + response.code());
                return null;
            }
        }
    }

    // 根据ID获取菜谱详情
    public Recipe getRecipeById(long recipeId) throws IOException {
        String url = BASE_URL + "/" + recipeId;
        Log.d("OkHttpRecipe", "开始获取菜谱详情，URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Log.d("OkHttpRecipe", "服务器响应: " + responseBody);

            if (response.isSuccessful()) {
                Recipe recipe = JSON.parseObject(responseBody, Recipe.class);
                Log.d("OkHttpRecipe", "解析结果: " + (recipe != null ? "成功" : "失败"));
                return recipe;
            } else {
                Log.e("OkHttpRecipe", "获取菜谱详情失败，状态码: " + response.code() +
                        ", 错误信息: " + responseBody);
                if (response.code() == 404) {
                    // 如果是404错误，尝试从本地缓存加载
                    Log.d("OkHttpRecipe", "尝试从本地缓存加载菜谱数据");
                    return null;
                }
                return null;
            }
        } catch (Exception e) {
            Log.e("OkHttpRecipe", "请求异常", e);
            throw e;
        }
    }

    // 根据分类获取菜谱列表
    public List<Recipe> getRecipesByCategory(String category) throws IOException {
        if ("其它".equals(category)) {
            // 获取所有菜谱
            List<Recipe> allRecipes = getAllRecipes();
            if (allRecipes != null) {
                // 过滤掉中餐、西餐和日料的菜谱
                return allRecipes.stream()
                        .filter(recipe -> !"中餐".equals(recipe.getCategory())
                                && !"西餐".equals(recipe.getCategory())
                                && !"甜点".equals(recipe.getCategory()))
                        .collect(Collectors.toList());
            }
            return null;
        }

        // 对分类名称进行URL编码
        String encodedCategory = java.net.URLEncoder.encode(category, "UTF-8");
        String url = BASE_URL + "/category?category=" + encodedCategory;
        Log.d("OkHttpRecipe", "请求分类菜谱URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Log.d("OkHttpRecipe", "分类菜谱响应: " + responseBody);

            if (response.isSuccessful()) {
                List<Recipe> recipes = JSON.parseArray(responseBody, Recipe.class);
                Log.d("OkHttpRecipe", "解析到菜谱数量: " + (recipes != null ? recipes.size() : 0));
                return recipes;
            } else {
                Log.e("OkHttpRecipe", "按分类获取菜谱失败，状态码: " + response.code() +
                        ", 响应内容: " + responseBody);
                return null;
            }
        } catch (Exception e) {
            Log.e("OkHttpRecipe", "请求异常", e);
            throw e;
        }
    }

    // 根据食材搜索菜谱
    public List<Recipe> searchByIngredient(String ingredient) throws IOException {
        String url = BASE_URL + "/ingredient?ingredient=" + ingredient;
        Log.d("OkHttpRecipe", "搜索URL: " + url);
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Log.d("OkHttpRecipe", "搜索结果: " + json);
                return JSON.parseObject(json, new TypeReference<List<Recipe>>() {
                });
            }
            return null;
        }
    }

    // 多条件搜索菜谱
    public List<Recipe> searchRecipes(String keyword) throws IOException {
        String url = BASE_URL + "/search?keyword=" + keyword;
        Log.d("OkHttpRecipe", "搜索URL: " + url);
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                return JSON.parseObject(json, new TypeReference<List<Recipe>>() {
                });
            }
            return null;
        }
    }

    // 根据标签获取菜谱
    public List<Recipe> getRecipesByTag(String tag) throws IOException {
        String url = BASE_URL + "/tag/" + tag;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return JSON.parseArray(responseBody, Recipe.class);
            }
            return null;
        }
    }
}