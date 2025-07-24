package com.example.demo.service;

import com.example.demo.entity.BrowseHistory;
import com.example.demo.repository.BrowseHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrowseHistoryService {
    @Autowired
    private BrowseHistoryRepository browseHistoryRepository;

    public BrowseHistory addBrowseHistory(Long userId, Long recipeId) {
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setRecipeId(recipeId);
        return browseHistoryRepository.save(history);
    }

    public List<BrowseHistory> getUserBrowseHistory(Long userId) {
        return browseHistoryRepository.findUserBrowseHistoryOrderByTimeDesc(userId);
    }

    public void clearUserBrowseHistory(Long userId) {
        List<BrowseHistory> histories = browseHistoryRepository.findByUserId(userId);
        browseHistoryRepository.deleteAll(histories);
    }

    public void removeBrowseHistory(Long userId, Long recipeId) {
        browseHistoryRepository.deleteByUserIdAndRecipeId(userId, recipeId);
    }
}