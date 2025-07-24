package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RecipeAdapter;
import com.example.myapplication.db.RecipeDao;
import com.example.myapplication.entity.Recipe;
import com.example.myapplication.netrequest.OkHttpRecipe;

import java.io.IOException;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // 初始化数据库
        recipeDao = new RecipeDao(this);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 获取分类类型
        String category = getIntent().getStringExtra("CATEGORY_TYPE");
        if (category != null) {
            getSupportActionBar().setTitle(category);
            loadCategoryData(category);
        }
    }

    private void loadCategoryData(String category) {
        OkHttpRecipe okHttpRecipe = new OkHttpRecipe();
        Log.d("CategoryActivity", "开始加载分类数据: " + category);

        new Thread(() -> {
            try {
                // 先尝试从网络获取数据
                List<Recipe> recipeList = okHttpRecipe.getRecipesByCategory(category);
                if (recipeList != null && !recipeList.isEmpty()) {
                    // 保存到本地数据库
                    recipeDao.saveRecipes(recipeList);
                    runOnUiThread(() -> {
                        RecipeAdapter adapter = new RecipeAdapter(recipeList);
                        recyclerView.setAdapter(adapter);
                        Log.d("CategoryActivity", "成功显示菜谱列表");
                    });
                } else {
                    // 如果网络获取失败，从本地数据库加载
                    List<Recipe> localRecipes = recipeDao.getRecipesByCategory(category);
                    if (localRecipes != null && !localRecipes.isEmpty()) {
                        runOnUiThread(() -> {
                            RecipeAdapter adapter = new RecipeAdapter(localRecipes);
                            recyclerView.setAdapter(adapter);
                            Toast.makeText(this, "当前为离线数据", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e("CategoryActivity", "没有找到分类菜谱: " + category);
                        runOnUiThread(() -> Toast.makeText(this, "暂无" + category + "菜谱", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e) {
                Log.e("CategoryActivity", "加载菜谱失败", e);
                // 网络请求失败时，从本地数据库加载
                List<Recipe> localRecipes = recipeDao.getRecipesByCategory(category);
                if (localRecipes != null && !localRecipes.isEmpty()) {
                    runOnUiThread(() -> {
                        RecipeAdapter adapter = new RecipeAdapter(localRecipes);
                        recyclerView.setAdapter(adapter);
                        Toast.makeText(this, "当前为离线数据", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "加载菜谱失败，请稍后重试", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}