package com.easybill.dto;

import com.easybill.entity.TransactionStatus;
import com.easybill.entity.TransactionType;
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
public class TransactionDTO {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String merchant;
    private Long categoryId;
    private String categoryName;
    private Long accountId;
    private String accountName;
    private LocalDateTime transactionTime;
    private String remark;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
