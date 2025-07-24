package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RecipeAdapter;
import com.example.myapplication.entity.Recipe;
import com.example.myapplication.netrequest.OkHttpBrowseHistory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BrowseHistoryActivity extends AppCompatActivity {
    private static final String TAG = "BrowseHistoryActivity";
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private OkHttpBrowseHistory okHttpBrowseHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_history);

        // 初始化Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(adapter);

        // 初始化网络请求对象
        okHttpBrowseHistory = new OkHttpBrowseHistory();

        // 加载浏览历史
        loadBrowseHistory();
    }

    private void loadBrowseHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", -1);

        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            try {
                List<Recipe> recipes = okHttpBrowseHistory.getUserBrowseHistory((long) userId);
                runOnUiThread(() -> {
                    if (recipes != null && !recipes.isEmpty()) {
                        recipeList.clear();
                        recipeList.addAll(recipes);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(BrowseHistoryActivity.this, "暂无浏览历史", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "加载浏览历史失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(BrowseHistoryActivity.this, "加载失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}