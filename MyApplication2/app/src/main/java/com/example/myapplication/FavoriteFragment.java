package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapter.RecipeAdapter;
import com.example.myapplication.entity.Favorite;
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

public class FavoriteFragment extends Fragment {
    private static final String TAG = "FavoriteFragment";
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private OkHttpFavorite okHttpFavorite;
    private OkHttpRecipe okHttpRecipe;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFavoriteEvent(FavoriteEvent event) {
        if (adapter != null) {
            adapter.updateRecipeFavoriteStatus(event.getRecipeId(), event.isFavorite());
            // 如果取消收藏，从列表中移除
            if (!event.isFavorite()) {
                for (int i = 0; i < recipeList.size(); i++) {
                    if (recipeList.get(i).getId() == event.getRecipeId()) {
                        recipeList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.favorite_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化网络请求对象
        okHttpFavorite = new OkHttpFavorite();
        okHttpRecipe = new OkHttpRecipe();

        // 加载收藏的菜谱
        loadFavoriteRecipes();

        return view;
    }

    private void loadFavoriteRecipes() {
        SharedPreferences sharedPreferences = requireContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                List<Favorite> favorites = okHttpFavorite.getUserFavorites(userId);
                if (favorites != null && !favorites.isEmpty()) {
                    List<Recipe> recipeList = new ArrayList<>();
                    for (Favorite favorite : favorites) {
                        Recipe recipe = okHttpRecipe.getRecipeById(favorite.getRecipeId());
                        if (recipe != null) {
                            recipe.setFavorite(true);
                            recipeList.add(recipe);
                        }
                    }

                    if (!recipeList.isEmpty()) {
                        requireActivity().runOnUiThread(() -> {
                            this.recipeList.clear();
                            this.recipeList.addAll(recipeList);
                            adapter = new RecipeAdapter(this.recipeList);
                            recyclerView.setAdapter(adapter);
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "暂无收藏菜谱", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "暂无收藏菜谱", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "加载收藏菜谱失败", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "加载失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    public void updateRecipeFavoriteStatus(long recipeId, boolean isFavorite) {
        if (adapter != null) {
            adapter.updateRecipeFavoriteStatus(recipeId, isFavorite);
            // 如果取消收藏，从列表中移除
            if (!isFavorite) {
                for (int i = 0; i < recipeList.size(); i++) {
                    if (recipeList.get(i).getId() == recipeId) {
                        recipeList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }
        }
    }
}