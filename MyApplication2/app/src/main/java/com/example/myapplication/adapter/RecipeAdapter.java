package com.example.myapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.ProductDetailActivity;
import com.example.myapplication.R;
import com.example.myapplication.entity.Recipe;
import com.example.myapplication.netrequest.OkHttpFavorite;
import com.example.myapplication.netrequest.OkHttpBrowseHistory;

import java.io.IOException;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final List<Recipe> recipeList;

    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        Log.d("RecipeAdapter", "绑定菜谱数据: ID=" + recipe.getId() +
                ", 名称=" + recipe.getName() +
                ", 图片URL=" + recipe.getImageUrl());

        // Bind recipe data to existing product layout
        holder.productName.setText(recipe.getName());
        holder.productDescription.setText(recipe.getDescription());
        holder.productPrice.setText(recipe.getTotalTime() + "分钟");

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.dangao)
                .into(holder.productImage);

        // 设置收藏状态
        holder.favoriteIcon.setImageResource(
                recipe.isFavorite() ? R.drawable.favorite_border : R.drawable.favorite_filled);

        // 确保itemView可点击
        holder.itemView.setClickable(true);
        holder.itemView.setFocusable(true);

        // 点击整个item跳转到详情页
        holder.itemView.setOnClickListener(v -> {
            Log.d("RecipeAdapter", "点击事件触发 - 菜谱ID: " + recipe.getId());
            Context context = holder.itemView.getContext();
            Toast.makeText(context, "正在记录浏览历史...", Toast.LENGTH_SHORT).show();

            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("id", -1);
            Log.d("RecipeAdapter", "当前用户ID: " + userId);

            if (userId != -1) {
                new Thread(() -> {
                    try {
                        Log.d("RecipeAdapter", "开始记录浏览历史 - 用户ID: " + userId + ", 菜谱ID: " + recipe.getId());
                        OkHttpBrowseHistory okHttpBrowseHistory = new OkHttpBrowseHistory();
                        boolean success = okHttpBrowseHistory.addBrowseHistory((long) userId, recipe.getId());
                        holder.itemView.post(() -> {
                            Toast.makeText(context,
                                    success ? "浏览历史记录成功" : "浏览历史记录失败",
                                    Toast.LENGTH_SHORT).show();
                        });
                        Log.d("RecipeAdapter", "浏览历史记录" + (success ? "成功" : "失败"));
                    } catch (IOException e) {
                        Log.e("RecipeAdapter", "记录浏览历史失败", e);
                        holder.itemView.post(() -> {
                            Toast.makeText(context, "浏览历史记录失败: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                Toast.makeText(context, "请先登录后再浏览", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            ((Activity) context).startActivityForResult(intent, 1);
        });

        // 点击收藏图标
        holder.favoriteIcon.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = holder.itemView.getContext()
                    .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("id", -1);

            if (userId == -1) {
                Toast.makeText(holder.itemView.getContext(),
                        "请先登录！", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    OkHttpFavorite okHttpFavorite = new OkHttpFavorite();
                    boolean success;
                    if (recipe.isFavorite()) {
                        success = okHttpFavorite.removeFavorite(userId, recipe.getId());
                    } else {
                        success = okHttpFavorite.addFavorite(userId, recipe.getId());
                    }

                    if (success) {
                        recipe.setFavorite(!recipe.isFavorite());
                        holder.itemView.post(() -> {
                            holder.favoriteIcon.setImageResource(
                                    recipe.isFavorite() ? R.drawable.favorite_border : R.drawable.favorite_filled);
                            String message = recipe.isFavorite() ? "已收藏" : "已取消收藏";
                            Toast.makeText(holder.itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e("RecipeAdapter", "收藏操作失败 - 用户ID: " + userId + ", 菜谱ID: " + recipe.getId() +
                                ", 当前状态: " + (recipe.isFavorite() ? "已收藏" : "未收藏"));
                        holder.itemView.post(() -> Toast
                                .makeText(holder.itemView.getContext(), "操作失败，请重试", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    Log.e("RecipeAdapter", "网络请求异常", e);
                    Log.e("RecipeAdapter", "异常详情 - 用户ID: " + userId + ", 菜谱ID: " + recipe.getId() +
                            ", 当前状态: " + (recipe.isFavorite() ? "已收藏" : "未收藏"));
                    holder.itemView.post(
                            () -> Toast.makeText(holder.itemView.getContext(), "网络错误，请重试", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateRecipeFavoriteStatus(long recipeId, boolean isFavorite) {
        for (int i = 0; i < recipeList.size(); i++) {
            Recipe recipe = recipeList.get(i);
            if (recipe.getId() == recipeId) {
                recipe.setFavorite(isFavorite);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeRecipe(long recipeId) {
        for (int i = 0; i < recipeList.size(); i++) {
            if (recipeList.get(i).getId() == recipeId) {
                recipeList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productDescription, productPrice;
        ImageView productImage, favoriteIcon;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productDescription = itemView.findViewById(R.id.product_description);
            productPrice = itemView.findViewById(R.id.product_price);
            productImage = itemView.findViewById(R.id.product_image);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
        }
    }
}
