package com.easybill.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId; // NULL 表示系统预置
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 100)
    private String icon;
    
    @Column(nullable = false, length = 10)
    private String type; // INCOME, EXPENSE
    
    @Column(name = "is_system")
    private Boolean isSystem = false;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
