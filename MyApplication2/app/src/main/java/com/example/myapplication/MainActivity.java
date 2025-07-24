package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private HomeFragment homeFragment;
    private FavoriteFragment favoriteFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate 开始");

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "setContentView 完成");

            // 检查登录状态
            if (!isLoggedIn()) {
                Log.d(TAG, "用户未登录，跳转到登录页面");
                // 未登录，跳转到登录页面
                Intent intent = new Intent(this, login.class);
                startActivity(intent);
                finish();
                return;
            }

            Log.d(TAG, "开始初始化Fragment");
            // 初始化Fragment管理器
            fragmentManager = getSupportFragmentManager();
            Log.d(TAG, "FragmentManager 初始化完成");

            // 初始化Fragment
            try {
                homeFragment = new HomeFragment();
                favoriteFragment = new FavoriteFragment();
                profileFragment = new ProfileFragment();
                Log.d(TAG, "Fragment 初始化完成");
            } catch (Exception e) {
                Log.e(TAG, "Fragment 初始化失败", e);
                throw e;
            }

            // 设置默认显示的Fragment
            try {
                setFragment(homeFragment);
                Log.d(TAG, "默认Fragment设置完成");
            } catch (Exception e) {
                Log.e(TAG, "设置默认Fragment失败", e);
                throw e;
            }

            // 设置底部导航栏
            try {
                bottomNavigationView = findViewById(R.id.bottom_navigation);
                if (bottomNavigationView == null) {
                    Log.e(TAG, "找不到底部导航栏视图");
                    throw new IllegalStateException("找不到底部导航栏视图");
                }

                bottomNavigationView.setOnItemSelectedListener(item -> {
                    try {
                        int itemId = item.getItemId();
                        Fragment selectedFragment = null;

                        if (itemId == R.id.navigation_home) {
                            selectedFragment = homeFragment;
                        } else if (itemId == R.id.navigation_orders) {
                            selectedFragment = favoriteFragment;
                        } else if (itemId == R.id.navigation_profile) {
                            selectedFragment = profileFragment;
                        }

                        if (selectedFragment != null) {
                            setFragment(selectedFragment);
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        Log.e(TAG, "导航栏点击事件处理失败", e);
                        return false;
                    }
                });
                Log.d(TAG, "底部导航栏设置完成");
            } catch (Exception e) {
                Log.e(TAG, "设置底部导航栏失败", e);
                throw e;
            }

            Log.d(TAG, "MainActivity 初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "MainActivity初始化失败", e);
            Toast.makeText(this, "初始化失败，请重试", Toast.LENGTH_SHORT).show();
            // 如果初始化失败，返回登录页面
            Intent intent = new Intent(this, login.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean isLoggedIn() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
            int userId = sharedPreferences.getInt("id", -1);
            Log.d(TAG, "检查登录状态 - isLoggedIn: " + isLoggedIn + ", userId: " + userId);
            return isLoggedIn && userId != -1;
        } catch (Exception e) {
            Log.e(TAG, "检查登录状态失败", e);
            return false;
        }
    }

    private void setFragment(Fragment fragment) {
        try {
            Log.d(TAG, "开始切换Fragment: " + fragment.getClass().getSimpleName());
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
            Log.d(TAG, "Fragment切换完成");
        } catch (Exception e) {
            Log.e(TAG, "Fragment切换失败", e);
            Toast.makeText(this, "页面切换失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            long recipeId = data.getLongExtra("RECIPE_ID", -1);
            boolean isFavorite = data.getBooleanExtra("IS_FAVORITE", false);
            if (recipeId != -1) {
                // 通知HomeFragment更新收藏状态
                if (homeFragment != null) {
                    homeFragment.updateRecipeFavoriteStatus(recipeId, isFavorite);
                }
                // 通知FavoriteFragment更新收藏状态
                if (favoriteFragment != null) {
                    favoriteFragment.updateRecipeFavoriteStatus(recipeId, isFavorite);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy");
    }
}
