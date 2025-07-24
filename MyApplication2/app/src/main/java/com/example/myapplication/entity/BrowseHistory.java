package com.example.myapplication.entity;

import java.time.LocalDateTime;

public class BrowseHistory {
    private Long id;
    private Long userId;
    private Long recipeId;
    private String browseTime;

    public BrowseHistory() {
    }

    public BrowseHistory(Long userId, Long recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.browseTime = LocalDateTime.now().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getBrowseTime() {
        return browseTime;
    }

    public void setBrowseTime(String browseTime) {
        this.browseTime = browseTime;
    }
}