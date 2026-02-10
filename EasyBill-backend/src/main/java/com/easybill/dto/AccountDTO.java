package com.easybill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;
    private String accountName;
    private String accountType;
    private String lastFourDigits;
    private BigDecimal balance;
    private Boolean isActive;
    private Integer transactionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
