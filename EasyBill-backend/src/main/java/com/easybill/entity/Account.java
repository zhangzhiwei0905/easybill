package com.easybill.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "account_name", nullable = false, length = 50)
    private String accountName;
    
    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType; // BANK_CARD, ALIPAY, WECHAT, CASH
    
    @Column(name = "last_four_digits", length = 4)
    private String lastFourDigits;
    
    @Column(name = "source_identifier", length = 50)
    private String sourceIdentifier;
    
    @Column(precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
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
