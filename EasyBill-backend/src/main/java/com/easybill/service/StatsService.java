package com.easybill.service;

import com.easybill.dto.CategoryStatsDTO;
import com.easybill.dto.StatsSummaryDTO;
import com.easybill.entity.Transaction;
import com.easybill.entity.TransactionType;
import com.easybill.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TransactionRepository transactionRepository;

    /**
     * 获取统计摘要
     */
    public StatsSummaryDTO getSummary(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions;
        
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUser_IdAndTransactionTimeBetween(userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUser_IdOrderByTransactionTimeDesc(userId);
        }
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int incomeCount = 0;
        int expenseCount = 0;
        
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
                incomeCount++;
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(transaction.getAmount());
                expenseCount++;
            }
        }
        
        return StatsSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .transactionCount(transactions.size())
                .incomeCount(incomeCount)
                .expenseCount(expenseCount)
                .build();
    }

    /**
     * 获取分类统计
     */
    public List<CategoryStatsDTO> getCategoryStats(Long userId, TransactionType type, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions;
        
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUser_IdAndTransactionTimeBetween(userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUser_IdOrderByTransactionTimeDesc(userId);
        }
        
        // 按类型筛选
        if (type != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getType() == type)
                    .collect(Collectors.toList());
        }
        
        // 按分类分组统计
        Map<Long, List<Transaction>> groupedByCategory = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getId()));
        
        // 计算总金额（用于计算百分比）
        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<CategoryStatsDTO> stats = new ArrayList<>();
        
        for (Map.Entry<Long, List<Transaction>> entry : groupedByCategory.entrySet()) {
            List<Transaction> categoryTransactions = entry.getValue();
            Transaction firstTransaction = categoryTransactions.get(0);
            
            BigDecimal categoryTotal = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double percentage = 0.0;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = categoryTotal.divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
            
            CategoryStatsDTO stat = CategoryStatsDTO.builder()
                    .categoryId(firstTransaction.getCategory().getId())
                    .categoryName(firstTransaction.getCategory().getName())
                    .type(firstTransaction.getType().name())
                    .totalAmount(categoryTotal)
                    .transactionCount(categoryTransactions.size())
                    .percentage(percentage)
                    .build();
            
            stats.add(stat);
        }
        
        // 按金额降序排序
        stats.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        
        return stats;
    }
}
