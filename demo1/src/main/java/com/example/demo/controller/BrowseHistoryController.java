package com.example.demo.controller;

import com.example.demo.entity.BrowseHistory;
import com.example.demo.service.BrowseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/browse-history")
public class BrowseHistoryController {
    @Autowired
    private BrowseHistoryService browseHistoryService;

    @PostMapping("/{userId}/{recipeId}")
    public ResponseEntity<BrowseHistory> addBrowseHistory(@PathVariable Long userId, @PathVariable Long recipeId) {
        return ResponseEntity.ok(browseHistoryService.addBrowseHistory(userId, recipeId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BrowseHistory>> getUserBrowseHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(browseHistoryService.getUserBrowseHistory(userId));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearUserBrowseHistory(@PathVariable Long userId) {
        browseHistoryService.clearUserBrowseHistory(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/{recipeId}")
    public ResponseEntity<Void> removeBrowseHistory(@PathVariable Long userId, @PathVariable Long recipeId) {
        browseHistoryService.removeBrowseHistory(userId, recipeId);
        return ResponseEntity.ok().build();
    }
}