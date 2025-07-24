package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.entity.Recipe;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private List<Recipe> recipes;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(Recipe recipe);
    }

    public BannerAdapter(List<Recipe> recipes, OnBannerClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_image, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        // 使用Glide加载图片
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.dangao) // 使用已有的图片资源
                .error(R.drawable.dangao) // 使用已有的图片资源
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBannerClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    // 更新数据的方法
    public void updateData(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.banner_image);
        }
    }
}