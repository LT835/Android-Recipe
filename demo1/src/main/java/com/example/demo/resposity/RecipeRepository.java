package com.example.demo.resposity;

import com.example.demo.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // 1. 根据分类查询菜谱（如中餐、西餐）
    List<Recipe> findByCategory(String category);

    // 2. 根据难度级别查询
    List<Recipe> findByDifficulty(String difficulty);

    // 3. 根据烹饪时间范围查询（查找小于等于指定时间的菜谱）
    List<Recipe> findByCookTimeLessThanEqual(int maxCookTime);

    // 4. 多条件模糊搜索（名称、描述、分类）
    @Query("SELECT r FROM Recipe r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recipe> searchByKeyword(@Param("keyword") String keyword);

    // 5. 根据食材搜索（需要关联查询 ingredients 列表）
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i " +
            "WHERE LOWER(i) LIKE LOWER(CONCAT('%', :ingredient, '%'))")
    List<Recipe> searchByIngredient(@Param("ingredient") String ingredient);

    // 6. 根据标签搜索（如素食、辣味等）
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.tags t " +
            "WHERE LOWER(t) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Recipe> searchByTag(@Param("tag") String tag);
}