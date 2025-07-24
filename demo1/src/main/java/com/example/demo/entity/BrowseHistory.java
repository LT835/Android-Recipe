package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "browse_history")
public class BrowseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "browse_time")
    private LocalDateTime browseTime;

    @PrePersist
    protected void onCreate() {
        browseTime = LocalDateTime.now();
    }
}