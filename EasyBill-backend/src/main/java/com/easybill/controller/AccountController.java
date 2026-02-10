package com.easybill.controller;

import com.easybill.dto.AccountCreateRequest;
import com.easybill.dto.AccountDTO;
import com.easybill.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取所有账户
     */
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        Long userId = getCurrentUserId();
        List<AccountDTO> accounts = accountService.getAllAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    /**
     * 获取账户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        AccountDTO account = accountService.getAccountById(id, userId);
        return ResponseEntity.ok(account);
    }

    /**
     * 创建账户
     */
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountCreateRequest request) {
        Long userId = getCurrentUserId();
        AccountDTO created = accountService.createAccount(userId, request);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新账户
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable Long id,
            @RequestBody AccountCreateRequest request
    ) {
        Long userId = getCurrentUserId();
        AccountDTO updated = accountService.updateAccount(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除账户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        accountService.deleteAccount(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "账户已删除"
        ));
    }

    /**
     * 启用/禁用账户
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<AccountDTO> toggleAccountStatus(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        AccountDTO account = accountService.toggleAccountStatus(id, userId);
        return ResponseEntity.ok(account);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return 1L;
    }
}
