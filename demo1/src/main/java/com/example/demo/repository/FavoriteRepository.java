package com.example.demo.repository;

import com.example.demo.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);

    @Query("SELECT f FROM Favorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findUserFavoritesOrderByCreatedAtDesc(@Param("userId") Long userId);
}