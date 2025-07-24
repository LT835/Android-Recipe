package com.example.myapplication.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String instructions; // 详细做法
    private int prepTime; // 准备时间(分钟)
    private int cookTime; // 烹饪时间(分钟)
    private String difficulty; // 难度级别
    private String imageUrl;
    private String category; // 分类(如中餐、西餐等)
    private List<String> ingredients; // 食材列表
    private List<String> tags; // 标签(如素食、辣味等)
    private boolean isFavorite;
    private List<String> steps;
    private String cookingTime;

    // 默认构造方法
    public Recipe() {
    }

    // 简化构造方法（用于测试）
    public Recipe(Long id, String name, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // 全字段构造方法
    public Recipe(Long id, String name, String description, String instructions,
            int prepTime, int cookTime, String difficulty, String imageUrl,
            String category, List<String> ingredients, List<String> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
        this.category = category;
        this.ingredients = ingredients;
        this.tags = tags;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(String cookingTime) {
        this.cookingTime = cookingTime;
    }

    // 辅助方法：获取总耗时（准备+烹饪）
    public int getTotalTime() {
        return prepTime + cookTime;
    }

    // 示例：将食材列表转为字符串
    public String getIngredientsString() {
        return ingredients != null ? String.join(", ", ingredients) : "";
    }

    // 将Recipe对象转换为JSON字符串
    @Override
    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
            json.put("instructions", instructions);
            json.put("prepTime", prepTime);
            json.put("cookTime", cookTime);
            json.put("difficulty", difficulty);
            json.put("imageUrl", imageUrl);
            json.put("category", category);

            if (ingredients != null) {
                JSONArray ingredientsArray = new JSONArray();
                for (String ingredient : ingredients) {
                    ingredientsArray.put(ingredient);
                }
                json.put("ingredients", ingredientsArray);
            }

            if (tags != null) {
                JSONArray tagsArray = new JSONArray();
                for (String tag : tags) {
                    tagsArray.put(tag);
                }
                json.put("tags", tagsArray);
            }

            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    // 从JSON字符串创建Recipe对象
    public static Recipe fromJson(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            Recipe recipe = new Recipe();
            recipe.setId(json.optLong("id"));
            recipe.setName(json.optString("name"));
            recipe.setDescription(json.optString("description"));
            recipe.setInstructions(json.optString("instructions"));
            recipe.setPrepTime(json.optInt("prepTime"));
            recipe.setCookTime(json.optInt("cookTime"));
            recipe.setDifficulty(json.optString("difficulty"));
            recipe.setImageUrl(json.optString("imageUrl"));
            recipe.setCategory(json.optString("category"));

            // 解析食材列表
            JSONArray ingredientsArray = json.optJSONArray("ingredients");
            if (ingredientsArray != null) {
                List<String> ingredients = new ArrayList<>();
                for (int i = 0; i < ingredientsArray.length(); i++) {
                    ingredients.add(ingredientsArray.getString(i));
                }
                recipe.setIngredients(ingredients);
            }

            // 解析标签列表
            JSONArray tagsArray = json.optJSONArray("tags");
            if (tagsArray != null) {
                List<String> tags = new ArrayList<>();
                for (int i = 0; i < tagsArray.length(); i++) {
                    tags.add(tagsArray.getString(i));
                }
                recipe.setTags(tags);
            }

            return recipe;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}