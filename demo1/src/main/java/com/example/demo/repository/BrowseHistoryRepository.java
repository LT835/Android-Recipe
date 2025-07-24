package com.example.demo.repository;

import com.example.demo.entity.BrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {
    List<BrowseHistory> findByUserId(Long userId);

    @Query("SELECT b FROM BrowseHistory b WHERE b.userId = :userId ORDER BY b.browseTime DESC")
    List<BrowseHistory> findUserBrowseHistoryOrderByTimeDesc(@Param("userId") Long userId);

    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);
}