package com.easybill.dto;

import com.easybill.entity.TransactionStatus;
import com.easybill.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionUpdateRequest {
    private TransactionType type;
    private BigDecimal amount;
    private String merchant;
    private Long categoryId;
    private Long accountId;
    private LocalDateTime transactionTime;
    private String remark;
    private TransactionStatus status;
}
