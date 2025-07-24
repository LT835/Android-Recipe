package com.example.demo.service;

import com.example.demo.entity.Favorite;
import com.example.demo.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Transactional
    public Favorite toggleFavorite(Long userId, Long recipeId) {
        if (favoriteRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
            favoriteRepository.deleteByUserIdAndRecipeId(userId, recipeId);
            return null;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setRecipeId(recipeId);
            return favoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void removeFavorite(Long userId, Long recipeId) {
        favoriteRepository.deleteByUserIdAndRecipeId(userId, recipeId);
    }

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findUserFavoritesOrderByCreatedAtDesc(userId);
    }

    public boolean isFavorite(Long userId, Long recipeId) {
        return favoriteRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }
}