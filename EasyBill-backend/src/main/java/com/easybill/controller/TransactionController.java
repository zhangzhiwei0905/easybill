package com.easybill.controller;

import com.easybill.dto.PageResponse;
import com.easybill.dto.TransactionDTO;
import com.easybill.dto.TransactionUpdateRequest;
import com.easybill.entity.TransactionStatus;
import com.easybill.entity.TransactionType;
import com.easybill.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 获取交易列表
     */
    @GetMapping
    public ResponseEntity<PageResponse<TransactionDTO>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        Long userId = getCurrentUserId();
        
        PageResponse<TransactionDTO> response = transactionService.getTransactions(
                userId, type, status, categoryId, accountId,
                startDate, endDate, page, size, sortBy, sortDirection
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取交易详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        TransactionDTO transaction = transactionService.getTransactionById(id, userId);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 更新交易
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionUpdateRequest request
    ) {
        Long userId = getCurrentUserId();
        TransactionDTO updated = transactionService.updateTransaction(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除交易
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "交易已删除"
        ));
    }

    /**
     * 批量确认交易
     */
    @PostMapping("/batch/confirm")
    public ResponseEntity<Map<String, Object>> batchConfirmTransactions(
            @RequestBody List<Long> ids
    ) {
        Long userId = getCurrentUserId();
        transactionService.batchConfirmTransactions(ids, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量确认成功",
                "count", ids.size()
        ));
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 这里简化处理，实际应该从 UserDetails 中获取
        // 暂时返回固定值，后续可以优化
        return 1L;
    }
}
