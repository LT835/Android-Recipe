package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.myapplication.entity.Recipe;
import com.example.myapplication.netrequest.OkHttpFavorite;
import com.example.myapplication.netrequest.OkHttpRecipe;
import com.example.myapplication.event.FavoriteEvent;
import org.greenrobot.eventbus.EventBus;
import java.io.IOException;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_FAVORITE_RECIPES = "favorite_recipes";
    private static final String TAG = "ProductDetailActivity";
    private ImageButton favoriteButton;
    private boolean isFavorite = false;
    private Recipe recipe;
    private ImageView productImage;
    private TextView productName;
    private TextView productDescription;
    private TextView productPrice;
    private TextView difficultyLevel;
    private TextView ingredientsList;
    private TextView cookingSteps;
    private TextView cookingTips;
    private ImageView favoriteIcon;
    private OkHttpFavorite okHttpFavorite;
    private OkHttpRecipe okHttpRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        Log.d("ProductDetailActivity", "onCreate: 开始初始化");

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("菜谱详情");
        }

        // 初始化视图
        initializeViews();

        // 初始化网络请求对象
        okHttpFavorite = new OkHttpFavorite();
        okHttpRecipe = new OkHttpRecipe();

        // 获取传递过来的菜谱ID
        long recipeId = getIntent().getLongExtra("RECIPE_ID", -1);
        Log.d("ProductDetailActivity", "onCreate: 获取到的菜谱ID = " + recipeId);

        if (recipeId != -1) {
            loadRecipeDetails(recipeId);
        } else {
            Log.e("ProductDetailActivity", "onCreate: 未获取到有效的菜谱ID");
            Toast.makeText(this, "无法加载菜谱信息", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 设置收藏按钮点击事件
        favoriteIcon.setOnClickListener(v -> handleFavoriteClick());
    }

    private void initializeViews() {
        favoriteButton = findViewById(R.id.favorite_button);
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        productImage = findViewById(R.id.product_image_detail);
        productName = findViewById(R.id.product_name_detail);
        productDescription = findViewById(R.id.product_description_detail);
        productPrice = findViewById(R.id.cooking_time);
        difficultyLevel = findViewById(R.id.difficulty_level);
        ingredientsList = findViewById(R.id.ingredients_list);
        cookingSteps = findViewById(R.id.cooking_steps);
        cookingTips = findViewById(R.id.cooking_tips);
        favoriteIcon = findViewById(R.id.favorite_button);
    }

    private void loadRecipeDetails(long recipeId) {
        Log.d("ProductDetailActivity", "开始加载菜谱详情，ID: " + recipeId);
        new Thread(() -> {
            try {
                recipe = okHttpRecipe.getRecipeById(recipeId);
                if (recipe != null) {
                    // 检查收藏状态
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    int userId = sharedPreferences.getInt("id", -1);
                    if (userId != -1) {
                        boolean isFavorite = okHttpFavorite.checkFavorite(userId, recipeId);
                        recipe.setFavorite(isFavorite);
                    }

                    runOnUiThread(() -> updateUI());
                } else {
                    Log.e("ProductDetailActivity", "网络加载失败，且本地无数据");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "加载菜谱失败", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (IOException e) {
                Log.e("ProductDetailActivity", "网络请求异常", e);
                runOnUiThread(() -> {
                    if (recipe == null) {
                        Toast.makeText(this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }).start();
    }

    private void updateUI() {
        if (recipe != null) {
            productName.setText(recipe.getName());
            productDescription.setText(recipe.getDescription());
            productPrice.setText("烹饪时间: " + recipe.getTotalTime() + "分钟");
            difficultyLevel.setText("难度: " + recipe.getDifficulty());

            // 显示食材列表
            if (recipe.getIngredients() != null) {
                StringBuilder ingredients = new StringBuilder();
                for (String ingredient : recipe.getIngredients()) {
                    ingredients.append("• ").append(ingredient).append("\n");
                }
                ingredientsList.setText(ingredients.toString());
            }

            // 显示烹饪步骤
            if (recipe.getInstructions() != null) {
                cookingSteps.setText(recipe.getInstructions());
            }

            // 显示小贴士
            if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
                StringBuilder tips = new StringBuilder();
                for (String tag : recipe.getTags()) {
                    tips.append("• ").append(tag).append("\n");
                }
                cookingTips.setText(tips.toString());
            }

            // 加载图片
            String imageName = recipe.getImageUrl();
            if (imageName != null && !imageName.isEmpty()) {
                // 如果是网络图片URL
                if (imageName.startsWith("http")) {
                    Glide.with(this)
                            .load(imageName)
                            .placeholder(R.drawable.dangao)
                            .error(R.drawable.dangao)
                            .fitCenter()
                            .into(productImage);
                } else {
                    // 如果是本地图片资源
                    int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    if (imageResId != 0) {
                        Glide.with(this)
                                .load(imageResId)
                                .placeholder(R.drawable.dangao)
                                .error(R.drawable.dangao)
                                .fitCenter()
                                .into(productImage);
                    } else {
                        productImage.setImageResource(R.drawable.dangao);
                    }
                }
            } else {
                productImage.setImageResource(R.drawable.dangao);
            }

            // 设置收藏图标状态
            favoriteIcon.setImageResource(
                    recipe.isFavorite() ? R.drawable.favorite_border : R.drawable.favorite_filled);
        }
    }

    private void toggleFavorite() {
        if (recipe == null)
            return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String favoriteRecipes = prefs.getString(KEY_FAVORITE_RECIPES, "");

        if (isFavorite) {
            // 取消收藏
            favoriteRecipes = favoriteRecipes.replace(recipe.getId() + ",", "");
            favoriteButton.setImageResource(R.drawable.favorite_border);
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
        } else {
            // 添加收藏
            favoriteRecipes += recipe.getId() + ",";
            favoriteButton.setImageResource(R.drawable.favorite_filled);
            Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
        }

        isFavorite = !isFavorite;
        prefs.edit().putString(KEY_FAVORITE_RECIPES, favoriteRecipes).apply();
    }

    private void updateFavoriteStatus(long recipeId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String favoriteRecipes = prefs.getString(KEY_FAVORITE_RECIPES, "");
        isFavorite = favoriteRecipes.contains(recipeId + ",");
        favoriteButton.setImageResource(isFavorite ? R.drawable.favorite_filled : R.drawable.favorite_border);
    }

    private void saveRecipeToLocal(Recipe recipe) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("recipe_" + recipe.getId(), recipe.toString());
        editor.apply();
    }

    private Recipe loadRecipeFromLocal(long recipeId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String recipeJson = prefs.getString("recipe_" + recipeId, null);
        if (recipeJson != null) {
            return Recipe.fromJson(recipeJson);
        }
        return null;
    }

    private void handleFavoriteClick() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", -1);

        if (userId == -1) {
            Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                boolean success;
                if (recipe.isFavorite()) {
                    success = okHttpFavorite.removeFavorite(userId, recipe.getId());
                } else {
                    success = okHttpFavorite.addFavorite(userId, recipe.getId());
                }

                if (success) {
                    recipe.setFavorite(!recipe.isFavorite());
                    runOnUiThread(() -> {
                        favoriteIcon.setImageResource(
                                recipe.isFavorite() ? R.drawable.favorite_border : R.drawable.favorite_filled);
                        String message = recipe.isFavorite() ? "已收藏" : "已取消收藏";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        // 发送收藏状态改变事件
                        EventBus.getDefault().post(new FavoriteEvent(recipe.getId(), recipe.isFavorite()));
                    });
                } else {
                    Log.e(TAG, "收藏操作失败 - 用户ID: " + userId + ", 菜谱ID: " + recipe.getId() +
                            ", 当前状态: " + (recipe.isFavorite() ? "已收藏" : "未收藏"));
                    runOnUiThread(() -> Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e(TAG, "收藏操作异常", e);
                runOnUiThread(() -> Toast.makeText(this, "网络错误，请重试", Toast.LENGTH_SHORT).show());
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
    public void onBackPressed() {
        // 设置返回结果，通知首页刷新
        Intent resultIntent = new Intent();
        resultIntent.putExtra("RECIPE_ID", recipe.getId());
        resultIntent.putExtra("IS_FAVORITE", recipe.isFavorite());
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}