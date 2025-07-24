package com.example.demo.service;

import com.example.demo.entity.Recipe;
import com.example.demo.resposity.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepository;

    // 1. 获取所有菜谱
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    // 2. 根据分类获取菜谱
    public List<Recipe> getRecipesByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            throw new IllegalArgumentException("分类不能为空");
        }
        return recipeRepository.findByCategory(category);
    }

    // 3. 根据难度级别获取菜谱
    public List<Recipe> getRecipesByDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            throw new IllegalArgumentException("难度级别不能为空");
        }
        return recipeRepository.findByDifficulty(difficulty);
    }

    // 4. 根据最大烹饪时间获取菜谱
    public List<Recipe> getRecipesByCookTimeLessThanEqual(int maxCookTime) {
        if (maxCookTime <= 0) {
            throw new IllegalArgumentException("烹饪时间必须大于0");
        }
        return recipeRepository.findByCookTimeLessThanEqual(maxCookTime);
    }

    // 5. 关键词搜索
    public List<Recipe> searchRecipes(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of(); // 返回空列表而不是null
        }
        return recipeRepository.searchByKeyword(keyword.toLowerCase());
    }

    // 6. 根据食材搜索
    public List<Recipe> searchRecipesByIngredient(String ingredient) {
        if (!StringUtils.hasText(ingredient)) {
            return List.of();
        }
        return recipeRepository.searchByIngredient(ingredient.toLowerCase());
    }

    // 7. 根据标签搜索
    public List<Recipe> searchRecipesByTag(String tag) {
        if (!StringUtils.hasText(tag)) {
            return List.of();
        }
        return recipeRepository.searchByTag(tag.toLowerCase());
    }

    // 8. 根据ID获取菜谱详情
    public Recipe getRecipeById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("无效的菜谱ID");
        }
        return recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的菜谱"));
    }

    // 9. 新增菜谱
    public Recipe createRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("菜谱信息不能为空");
        }
        // 可以添加更多验证逻辑
        return recipeRepository.save(recipe);
    }
}