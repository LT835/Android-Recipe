package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.BannerAdapter;
import com.example.myapplication.adapter.RecipeAdapter;
import com.example.myapplication.db.RecipeDao;
import com.example.myapplication.entity.Recipe;
import com.example.myapplication.netrequest.OkHttpRecipe;
import com.example.myapplication.netrequest.OkHttpFavorite;
import com.example.myapplication.event.FavoriteEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private ViewPager2 bannerViewPager;
    private RecyclerView recipeRecyclerView;
    private EditText searchEditText;
    private Button searchButton;
    private Handler handler;
    private Runnable bannerRunnable;
    private static final long BANNER_DELAY = 3000;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private RecipeDao recipeDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipeDao = new RecipeDao(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        setupCategoryClickListeners(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFavoriteEvent(FavoriteEvent event) {
        if (recipeAdapter != null) {
            recipeAdapter.updateRecipeFavoriteStatus(event.getRecipeId(), event.isFavorite());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSearch();
        loadRecipes();

        return view;
    }

    private void initializeViews(View view) {
        bannerViewPager = view.findViewById(R.id.banner_viewpager);
        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
    }

    private void setupRecyclerView() {
        if (recipeRecyclerView != null) {
            recipeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recipeRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                        @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.top = 2;
                    outRect.bottom = 2;
                }
            });
        }
    }

    private void setupSearch() {
        searchButton.setOnClickListener(v -> performSearch());
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                // 隐藏输入法
                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        // 设置输入法类型
        searchEditText.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    private void performSearch() {
        String keyword = searchEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(keyword)) {
            // 隐藏输入法
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

            Intent intent = new Intent(getActivity(), SearchResultActivity.class);
            intent.putExtra("SEARCH_QUERY", keyword);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "请输入搜索关键词", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecipes() {
        Log.d(TAG, "开始加载菜谱数据");
        OkHttpRecipe okHttpRecipe = new OkHttpRecipe();
        OkHttpFavorite okHttpFavorite = new OkHttpFavorite();

        new Thread(() -> {
            try {
                // 先尝试从网络获取数据
                List<Recipe> recipeList = okHttpRecipe.getAllRecipes();
                if (recipeList != null && !recipeList.isEmpty()) {
                    Log.d(TAG, "从网络获取到 " + recipeList.size() + " 个菜谱");

                    // 检查每个菜谱的收藏状态
                    SharedPreferences sharedPreferences = requireContext()
                            .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    int userId = sharedPreferences.getInt("id", -1);

                    if (userId != -1) {
                        for (Recipe recipe : recipeList) {
                            try {
                                boolean isFavorite = okHttpFavorite.checkFavorite(userId, recipe.getId());
                                recipe.setFavorite(isFavorite);
                            } catch (Exception e) {
                                Log.e(TAG, "检查收藏状态失败: " + recipe.getId(), e);
                            }
                        }
                    }

                    // 保存到本地数据库
                    try {
                        recipeDao.saveRecipes(recipeList);
                        Log.d(TAG, "成功保存菜谱到本地数据库");
                    } catch (Exception e) {
                        Log.e(TAG, "保存菜谱到本地数据库失败", e);
                    }

                    requireActivity().runOnUiThread(() -> {
                        try {
                            updateUI(recipeList);
                            Log.d(TAG, "UI更新完成");
                        } catch (Exception e) {
                            Log.e(TAG, "更新UI失败", e);
                            Toast.makeText(getContext(), "加载失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.d(TAG, "网络获取菜谱失败，尝试从本地加载");
                    // 如果网络获取失败，从本地数据库加载
                    try {
                        List<Recipe> localRecipes = recipeDao.getAllRecipes();
                        if (localRecipes != null && !localRecipes.isEmpty()) {
                            requireActivity().runOnUiThread(() -> {
                                try {
                                    updateUI(localRecipes);
                                    Toast.makeText(getContext(), "当前为离线数据", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "成功加载本地菜谱数据");
                                } catch (Exception e) {
                                    Log.e(TAG, "更新UI失败", e);
                                    Toast.makeText(getContext(), "加载失败，请重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "本地数据库中没有菜谱数据");
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "暂无菜谱数据", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "从本地数据库加载菜谱失败", e);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "加载失败，请重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "加载菜谱数据失败", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "加载失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateUI(List<Recipe> recipeList) {
        // Setup banner
        List<Recipe> bannerRecipes = getRandomBannerRecipes(recipeList);
        setupBannerViewPager(bannerRecipes);

        // Setup recipe list
        recipeAdapter = new RecipeAdapter(recipeList);
        recipeRecyclerView.setAdapter(recipeAdapter);
    }

    private List<Recipe> getRandomBannerRecipes(List<Recipe> recipes) {
        List<Recipe> shuffled = new ArrayList<>(recipes);
        Collections.shuffle(shuffled);
        return shuffled.size() > 4 ? shuffled.subList(0, 4) : shuffled;
    }

    private void setupBannerViewPager(List<Recipe> recipes) {
        if (recipes.isEmpty())
            return;

        BannerAdapter bannerAdapter = new BannerAdapter(recipes, recipe -> {
            if (isAdded() && getActivity() != null) {
                navigateToRecipeDetail(recipe);
            }
        });

        bannerViewPager.setAdapter(bannerAdapter);

        // Auto-scroll banner
        handler = new Handler();
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % recipes.size();
                bannerViewPager.setCurrentItem(nextItem, true);
                handler.postDelayed(this, BANNER_DELAY);
            }
        };
        handler.postDelayed(bannerRunnable, BANNER_DELAY);

        // Infinite scroll
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                handler.removeCallbacks(bannerRunnable);
                handler.postDelayed(bannerRunnable, BANNER_DELAY);
            }
        });
    }

    private void navigateToRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("RECIPE_ID", recipe.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && bannerRunnable != null) {
            handler.postDelayed(bannerRunnable, BANNER_DELAY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && bannerRunnable != null) {
            handler.removeCallbacks(bannerRunnable);
        }
    }

    private void setupCategoryClickListeners(View view) {
        // 中餐分类
        view.findViewById(R.id.frameChinese).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoryActivity.class);
            intent.putExtra("CATEGORY_TYPE", "中餐");
            startActivity(intent);
        });

        // 西餐分类
        view.findViewById(R.id.frameWestern).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoryActivity.class);
            intent.putExtra("CATEGORY_TYPE", "西餐");
            startActivity(intent);
        });

        // 甜点分类
        view.findViewById(R.id.frameDessert).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoryActivity.class);
            intent.putExtra("CATEGORY_TYPE", "甜点");
            startActivity(intent);
        });

        // 饮品分类
        view.findViewById(R.id.frameJapanese).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoryActivity.class);
            intent.putExtra("CATEGORY_TYPE", "其它");
            startActivity(intent);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            long recipeId = data.getLongExtra("RECIPE_ID", -1);
            boolean isFavorite = data.getBooleanExtra("IS_FAVORITE", false);
            if (recipeId != -1) {
                updateRecipeFavoriteStatus(recipeId, isFavorite);
            }
        }
    }

    public void updateRecipeFavoriteStatus(long recipeId, boolean isFavorite) {
        if (recipeAdapter != null) {
            recipeAdapter.updateRecipeFavoriteStatus(recipeId, isFavorite);
        }
    }
}
