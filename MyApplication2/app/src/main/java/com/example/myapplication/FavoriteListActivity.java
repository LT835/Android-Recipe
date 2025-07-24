package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RecipeAdapter;
import com.example.myapplication.db.RecipeDao;
import com.example.myapplication.entity.Favorite;
import com.example.myapplication.entity.Recipe;
import com.example.myapplication.event.FavoriteEvent;
import com.example.myapplication.netrequest.OkHttpFavorite;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity {
    private static final String TAG = "FavoriteListActivity";
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private OkHttpFavorite okHttpFavorite;
    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        // 初始化数据库
        recipeDao = new RecipeDao(this);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("我的收藏");
        }

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // 初始化网络请求对象
        okHttpFavorite = new OkHttpFavorite();

        // 加载收藏列表
        loadFavoriteRecipes();
    }

    private void loadFavoriteRecipes() {
        new Thread(() -> {
            try {
                // 获取用户ID
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                int userId = sharedPreferences.getInt("id", -1);
                Log.d(TAG, "当前用户ID: " + userId);

                if (userId == -1) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                // 从网络获取收藏列表
                List<Favorite> favorites = okHttpFavorite.getUserFavorites(userId);
                Log.d(TAG, "获取到收藏列表: " + (favorites != null ? favorites.size() : 0));

                if (favorites != null && !favorites.isEmpty()) {
                    // 从本地数据库获取菜谱详情
                    List<Recipe> recipes = new ArrayList<>();
                    for (Favorite favorite : favorites) {
                        Recipe recipe = recipeDao.getRecipeById(favorite.getRecipeId());
                        if (recipe != null) {
                            recipe.setFavorite(true);
                            recipes.add(recipe);
                            Log.d(TAG, "添加收藏菜谱: " + recipe.getName());
                        } else {
                            Log.d(TAG, "未找到菜谱: " + favorite.getRecipeId());
                        }
                    }

                    runOnUiThread(() -> {
                        recipeList.clear();
                        recipeList.addAll(recipes);
                        adapter = new RecipeAdapter(recipeList);
                        if (recyclerView != null) {
                            recyclerView.setAdapter(adapter);
                        }
                        if (recipes.isEmpty()) {
                            Toast.makeText(this, "暂无收藏菜谱", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "暂无收藏菜谱", Toast.LENGTH_SHORT).show();
                        recipeList.clear();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "加载收藏列表失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载收藏列表失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            long recipeId = data.getLongExtra("RECIPE_ID", -1);
            boolean isFavorite = data.getBooleanExtra("IS_FAVORITE", false);
            if (recipeId != -1 && adapter != null) {
                if (!isFavorite) {
                    // 如果取消收藏，从列表中移除
                    adapter.removeRecipe(recipeId);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFavoriteEvent(FavoriteEvent event) {
        if (!event.isFavorite()) {
            // 如果取消收藏，从列表中移除
            if (adapter != null) {
                adapter.removeRecipe(event.getRecipeId());
            }
        }
    }
}