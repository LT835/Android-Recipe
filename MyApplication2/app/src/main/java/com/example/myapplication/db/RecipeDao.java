package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapplication.entity.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class RecipeDao {
    private static final String TAG = "RecipeDao";
    private RecipeDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private Gson gson;

    public RecipeDao(Context context) {
        try {
            dbHelper = new RecipeDatabaseHelper(context.getApplicationContext());
            gson = new Gson();
        } catch (Exception e) {
            Log.e(TAG, "RecipeDao初始化失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    public void open() {
        try {
            if (database == null || !database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
        } catch (Exception e) {
            Log.e(TAG, "打开数据库失败", e);
            throw new RuntimeException("打开数据库失败", e);
        }
    }

    public void close() {
        try {
            if (database != null && database.isOpen()) {
                database.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "关闭数据库失败", e);
        }
    }

    // 保存菜谱列表到数据库
    public void saveRecipes(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            Log.d(TAG, "没有菜谱数据需要保存");
            return;
        }

        try {
            open();
            database.beginTransaction();
            try {
                // 清空现有数据
                database.delete(RecipeDatabaseHelper.TABLE_RECIPES, null, null);

                // 插入新数据
                for (Recipe recipe : recipes) {
                    ContentValues values = new ContentValues();
                    values.put(RecipeDatabaseHelper.COLUMN_ID, recipe.getId());
                    values.put(RecipeDatabaseHelper.COLUMN_NAME, recipe.getName());
                    values.put(RecipeDatabaseHelper.COLUMN_DESCRIPTION, recipe.getDescription());
                    values.put(RecipeDatabaseHelper.COLUMN_INGREDIENTS, gson.toJson(recipe.getIngredients()));
                    values.put(RecipeDatabaseHelper.COLUMN_STEPS, gson.toJson(recipe.getSteps()));
                    values.put(RecipeDatabaseHelper.COLUMN_IMAGE_URL, recipe.getImageUrl());
                    values.put(RecipeDatabaseHelper.COLUMN_CATEGORY, recipe.getCategory());
                    values.put(RecipeDatabaseHelper.COLUMN_FAVORITE, recipe.isFavorite() ? 1 : 0);

                    long result = database.insert(RecipeDatabaseHelper.TABLE_RECIPES, null, values);
                    if (result == -1) {
                        Log.e(TAG, "保存菜谱失败: " + recipe.getName());
                    }
                }
                database.setTransactionSuccessful();
                Log.d(TAG, "成功保存 " + recipes.size() + " 个菜谱");
            } finally {
                database.endTransaction();
            }
        } catch (Exception e) {
            Log.e(TAG, "保存菜谱列表失败", e);
            throw new RuntimeException("保存菜谱失败", e);
        } finally {
            close();
        }
    }

    // 从数据库获取所有菜谱
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            cursor = database.query(RecipeDatabaseHelper.TABLE_RECIPES, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Recipe recipe = cursorToRecipe(cursor);
                    if (recipe != null) {
                        recipes.add(recipe);
                    }
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "成功获取 " + recipes.size() + " 个菜谱");
            return recipes;
        } catch (Exception e) {
            Log.e(TAG, "获取菜谱列表失败", e);
            throw new RuntimeException("获取菜谱失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
    }

    // 根据分类获取菜谱
    public List<Recipe> getRecipesByCategory(String category) {
        List<Recipe> recipes = new ArrayList<>();
        open();
        String selection = RecipeDatabaseHelper.COLUMN_CATEGORY + " = ?";
        String[] selectionArgs = { category };
        Cursor cursor = database.query(RecipeDatabaseHelper.TABLE_RECIPES, null, selection, selectionArgs, null, null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Recipe recipe = cursorToRecipe(cursor);
                recipes.add(recipe);
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();
        return recipes;
    }

    // 搜索菜谱
    public List<Recipe> searchRecipes(String keyword) {
        List<Recipe> recipes = new ArrayList<>();
        open();
        String selection = RecipeDatabaseHelper.COLUMN_NAME + " LIKE ? OR " +
                RecipeDatabaseHelper.COLUMN_DESCRIPTION + " LIKE ? OR " +
                RecipeDatabaseHelper.COLUMN_INGREDIENTS + " LIKE ?";
        String[] selectionArgs = { "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%" };
        Cursor cursor = database.query(RecipeDatabaseHelper.TABLE_RECIPES, null, selection, selectionArgs, null, null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Recipe recipe = cursorToRecipe(cursor);
                recipes.add(recipe);
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();
        return recipes;
    }

    // 更新菜谱收藏状态
    public void updateRecipeFavorite(long recipeId, boolean isFavorite) {
        open();
        ContentValues values = new ContentValues();
        values.put(RecipeDatabaseHelper.COLUMN_FAVORITE, isFavorite ? 1 : 0);
        String whereClause = RecipeDatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(recipeId) };
        database.update(RecipeDatabaseHelper.TABLE_RECIPES, values, whereClause, whereArgs);
        close();
    }

    // 根据ID获取菜谱
    public Recipe getRecipeById(long recipeId) {
        open();
        String selection = RecipeDatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(recipeId) };
        Cursor cursor = database.query(RecipeDatabaseHelper.TABLE_RECIPES, null, selection, selectionArgs, null, null,
                null);

        Recipe recipe = null;
        if (cursor != null && cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
            cursor.close();
        }
        close();
        return recipe;
    }

    // 将Cursor转换为Recipe对象
    private Recipe cursorToRecipe(Cursor cursor) {
        try {
            Recipe recipe = new Recipe();
            recipe.setId(cursor.getLong(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_ID)));
            recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_NAME)));
            recipe.setDescription(
                    cursor.getString(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_DESCRIPTION)));

            String ingredientsJson = cursor
                    .getString(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_INGREDIENTS));
            List<String> ingredients = gson.fromJson(ingredientsJson, new TypeToken<List<String>>() {
            }.getType());
            recipe.setIngredients(ingredients);

            String stepsJson = cursor.getString(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_STEPS));
            List<String> steps = gson.fromJson(stepsJson, new TypeToken<List<String>>() {
            }.getType());
            recipe.setSteps(steps);

            recipe.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_IMAGE_URL)));
            recipe.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_CATEGORY)));
            recipe.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(RecipeDatabaseHelper.COLUMN_FAVORITE)) == 1);

            return recipe;
        } catch (Exception e) {
            Log.e(TAG, "转换菜谱数据失败", e);
            return null;
        }
    }
}