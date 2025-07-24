package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RecipeAdapter;
import com.example.myapplication.db.RecipeDao;
import com.example.myapplication.entity.Recipe;
import com.example.myapplication.event.FavoriteEvent;
import com.example.myapplication.netrequest.OkHttpFavorite;
import com.example.myapplication.netrequest.OkHttpRecipe;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    private static final String TAG = "SearchResultActivity";
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private OkHttpRecipe okHttpRecipe;
    private OkHttpFavorite okHttpFavorite;
    private EditText searchEditText;
    private Button searchButton;
    private String currentQuery;
    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // 初始化数据库
        recipeDao = new RecipeDao(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("搜索结果");
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize network request objects
        okHttpRecipe = new OkHttpRecipe();
        okHttpFavorite = new OkHttpFavorite();

        // Get search keyword
        currentQuery = getIntent().getStringExtra("SEARCH_QUERY");
        if (!TextUtils.isEmpty(currentQuery)) {
            searchEditText.setText(currentQuery);
            performSearch(currentQuery);
        }

        // Set search button click event
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                currentQuery = query;
                performSearch(query);
            } else {
                Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            }
        });

        // Set search edit text return event
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                currentQuery = query;
                performSearch(query);
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        new Thread(() -> {
            try {
                // 先尝试从网络搜索
                List<Recipe> results = okHttpRecipe.searchRecipes(query);
                if (results != null && !results.isEmpty()) {
                    // 检查每个菜谱的收藏状态
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    int userId = sharedPreferences.getInt("id", -1);

                    if (userId != -1) {
                        for (Recipe recipe : results) {
                            boolean isFavorite = okHttpFavorite.checkFavorite(userId, recipe.getId());
                            recipe.setFavorite(isFavorite);
                        }
                    }

                    runOnUiThread(() -> {
                        recipeList.clear();
                        recipeList.addAll(results);
                        adapter = new RecipeAdapter(recipeList);
                        recyclerView.setAdapter(adapter);
                    });
                } else {
                    // 如果网络搜索失败，从本地数据库搜索
                    List<Recipe> localResults = recipeDao.searchRecipes(query);
                    if (localResults != null && !localResults.isEmpty()) {
                        runOnUiThread(() -> {
                            recipeList.clear();
                            recipeList.addAll(localResults);
                            adapter = new RecipeAdapter(recipeList);
                            recyclerView.setAdapter(adapter);
                            Toast.makeText(this, "当前为离线搜索结果", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "未找到相关菜谱", Toast.LENGTH_SHORT).show();
                            recipeList.clear();
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "搜索失败", e);
                // 网络搜索失败时，从本地数据库搜索
                List<Recipe> localResults = recipeDao.searchRecipes(query);
                if (localResults != null && !localResults.isEmpty()) {
                    runOnUiThread(() -> {
                        recipeList.clear();
                        recipeList.addAll(localResults);
                        adapter = new RecipeAdapter(recipeList);
                        recyclerView.setAdapter(adapter);
                        Toast.makeText(this, "当前为离线搜索结果", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "搜索失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
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
                adapter.updateRecipeFavoriteStatus(recipeId, isFavorite);
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
        if (adapter != null) {
            adapter.updateRecipeFavoriteStatus(event.getRecipeId(), event.isFavorite());
        }
    }
}