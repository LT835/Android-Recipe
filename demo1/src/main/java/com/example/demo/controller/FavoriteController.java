package com.example.demo.controller;

import com.example.demo.entity.Favorite;
import com.example.demo.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{userId}/{recipeId}")
    public ResponseEntity<Favorite> toggleFavorite(@PathVariable Long userId, @PathVariable Long recipeId) {
        Favorite favorite = favoriteService.toggleFavorite(userId, recipeId);
        return favorite != null ? ResponseEntity.ok(favorite) : ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/{recipeId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long recipeId) {
        favoriteService.removeFavorite(userId, recipeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }

    @GetMapping("/check/{userId}/{recipeId}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long userId, @PathVariable Long recipeId) {
        return ResponseEntity.ok(favoriteService.isFavorite(userId, recipeId));
    }
}