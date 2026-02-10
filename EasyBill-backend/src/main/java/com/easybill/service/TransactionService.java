package com.easybill.service;

import com.easybill.dto.PageResponse;
import com.easybill.dto.TransactionDTO;
import com.easybill.dto.TransactionUpdateRequest;
import com.easybill.entity.*;
import com.easybill.repository.AccountRepository;
import com.easybill.repository.CategoryRepository;
import com.easybill.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    /**
     * 获取交易列表（支持筛选、排序、分页）
     */
    public PageResponse<TransactionDTO> getTransactions(
            Long userId,
            TransactionType type,
            TransactionStatus status,
            Long categoryId,
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        // 构建查询条件
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 用户ID必须匹配
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            
            // 类型筛选
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            
            // 状态筛选
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            // 分类筛选
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            
            // 账户筛选
            if (accountId != null) {
                predicates.add(cb.equal(root.get("account").get("id"), accountId));
            }
            
            // 日期范围筛选
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionTime"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionTime"), endDate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 构建排序
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
            sortBy != null ? sortBy : "transactionTime"
        );
        
        // 分页查询
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
        
        // 转换为 DTO
        List<TransactionDTO> dtos = transactionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<TransactionDTO>builder()
                .content(dtos)
                .page(page)
                .size(size)
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();
    }

    /**
     * 获取交易详情
     */
    public TransactionDTO getTransactionById(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // 验证权限
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        return convertToDTO(transaction);
    }

    /**
     * 更新交易
     */
    @Transactional
    public TransactionDTO updateTransaction(Long id, Long userId, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // 验证权限
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        // 更新字段
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getMerchant() != null) {
            transaction.setMerchant(request.getMerchant());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.setCategory(category);
        }
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            transaction.setAccount(account);
        }
        if (request.getTransactionTime() != null) {
            transaction.setTransactionTime(request.getTransactionTime());
        }
        if (request.getRemark() != null) {
            transaction.setRemark(request.getRemark());
        }
        if (request.getStatus() != null) {
            transaction.setStatus(request.getStatus());
        }
        
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        
        return convertToDTO(saved);
    }

    /**
     * 删除交易
     */
    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // 验证权限
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        transactionRepository.delete(transaction);
    }

    /**
     * 批量确认交易
     */
    @Transactional
    public void batchConfirmTransactions(List<Long> ids, Long userId) {
        List<Transaction> transactions = transactionRepository.findAllById(ids);
        
        for (Transaction transaction : transactions) {
            // 验证权限
            if (!transaction.getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized access to transaction: " + transaction.getId());
            }
            
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setUpdatedAt(LocalDateTime.now());
        }
        
        transactionRepository.saveAll(transactions);
    }

    /**
     * 转换为 DTO
     */
    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .merchant(transaction.getMerchant())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountName(transaction.getAccount() != null ? transaction.getAccount().getAccountName() : null)
                .transactionTime(transaction.getTransactionTime())
                .remark(transaction.getRemark())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
