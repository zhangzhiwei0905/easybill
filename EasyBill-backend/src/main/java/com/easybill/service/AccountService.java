package com.easybill.service;

import com.easybill.dto.AccountCreateRequest;
import com.easybill.dto.AccountDTO;
import com.easybill.entity.Account;
import com.easybill.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * 获取用户的所有账户
     */
    public List<AccountDTO> getAllAccounts(Long userId) {
        List<Account> accounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return accounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取账户详情
     */
    public AccountDTO getAccountById(Long id, Long userId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        return convertToDTO(account);
    }

    /**
     * 创建账户
     */
    @Transactional
    public AccountDTO createAccount(Long userId, AccountCreateRequest request) {
        Account account = new Account();
        account.setUserId(userId);
        account.setAccountName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setLastFourDigits(request.getLastFourDigits());
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setIsActive(true);
        
        Account saved = accountRepository.save(account);
        return convertToDTO(saved);
    }

    /**
     * 更新账户
     */
    @Transactional
    public AccountDTO updateAccount(Long id, Long userId, AccountCreateRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        if (request.getAccountName() != null) {
            account.setAccountName(request.getAccountName());
        }
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        if (request.getLastFourDigits() != null) {
            account.setLastFourDigits(request.getLastFourDigits());
        }
        if (request.getBalance() != null) {
            account.setBalance(request.getBalance());
        }
        
        Account saved = accountRepository.save(account);
        return convertToDTO(saved);
    }

    /**
     * 删除账户
     */
    @Transactional
    public void deleteAccount(Long id, Long userId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        accountRepository.delete(account);
    }

    /**
     * 启用/禁用账户
     */
    @Transactional
    public AccountDTO toggleAccountStatus(Long id, Long userId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        account.setIsActive(!account.getIsActive());
        Account saved = accountRepository.save(account);
        return convertToDTO(saved);
    }

    /**
     * 转换为 DTO
     */
    private AccountDTO convertToDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .lastFourDigits(account.getLastFourDigits())
                .balance(account.getBalance())
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
