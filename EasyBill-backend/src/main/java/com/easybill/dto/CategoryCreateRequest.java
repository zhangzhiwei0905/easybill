package com.easybill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    private String name;
    private String type; // INCOME, EXPENSE
    private String icon;
    private String color;
    private Long parentId;
}
