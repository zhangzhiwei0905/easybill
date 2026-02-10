package com.easybill.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ParsedTransaction {
    
    private String type; // INCOME or EXPENSE
    
    private BigDecimal amount;
    
    private String merchant;
    
    private String cardLastFour;
    
    private String transactionTime;
    
    private String categoryHint;
}
