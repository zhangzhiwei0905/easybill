package com.easybill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsDTO {
    private Long categoryId;
    private String categoryName;
    private String type;
    private BigDecimal totalAmount;
    private Integer transactionCount;
    private Double percentage;
}
