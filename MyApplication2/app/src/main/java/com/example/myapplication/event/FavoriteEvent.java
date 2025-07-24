package com.example.myapplication.event;

public class FavoriteEvent {
    private long recipeId;
    private boolean isFavorite;

    public FavoriteEvent(long recipeId, boolean isFavorite) {
        this.recipeId = recipeId;
        this.isFavorite = isFavorite;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}