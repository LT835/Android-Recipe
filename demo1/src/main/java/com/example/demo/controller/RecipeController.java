package com.example.demo.controller;

import com.example.demo.entity.Recipe;
import com.example.demo.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    @Autowired
    private RecipeService recipeService;

    // 1. 获取所有菜谱
    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    // 2. 根据分类获取菜谱（如中餐、西餐等）
    @GetMapping("/category")
    public List<Recipe> getRecipesByCategory(@RequestParam String category) {
        return recipeService.getRecipesByCategory(category);
    }

    // 3. 根据难度级别获取菜谱
    @GetMapping("/difficulty")
    public List<Recipe> getRecipesByDifficulty(@RequestParam String difficulty) {
        return recipeService.getRecipesByDifficulty(difficulty);
    }

    // 4. 根据最大烹饪时间获取菜谱
    @GetMapping("/time")
    public List<Recipe> getRecipesByMaxCookTime(@RequestParam int maxCookTime) {
        return recipeService.getRecipesByCookTimeLessThanEqual(maxCookTime);
    }

    // 5. 关键词搜索（名称、描述、分类）
    @GetMapping("/search")
    public List<Recipe> searchRecipes(@RequestParam String keyword) {
        return recipeService.searchRecipes(keyword);
    }

    // 6. 根据食材搜索
    @GetMapping("/ingredient")
    public List<Recipe> searchRecipesByIngredient(@RequestParam String ingredient) {
        return recipeService.searchRecipesByIngredient(ingredient);
    }

    // 7. 根据标签搜索（如素食、辣味等）
    @GetMapping("/tag")
    public List<Recipe> searchRecipesByTag(@RequestParam String tag) {
        return recipeService.searchRecipesByTag(tag);
    }

    // 8. 获取菜谱详情（按ID）
    @GetMapping("/{id}")
    public Recipe getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id);
    }
}