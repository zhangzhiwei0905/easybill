package com.easybill.service;

import com.easybill.dto.ParsedTransaction;
import com.easybill.dto.SmsWebhookRequest;
import com.easybill.entity.*;
import com.easybill.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsProcessingService {
    
    @Autowired
    private AIParserService aiParserService;
    
    @Autowired
    private IdempotencyService idempotencyService;
    
    @Autowired
    private RawSmsLogRepository rawSmsLogRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Transactional
    public Transaction processSms(User user, SmsWebhookRequest request) {
        log.info("处理短信，用户: {}, 发件人: {}", user.getUsername(), request.getSender());
        
        // 1. 保存原始短信日志
        RawSmsLog smsLog = new RawSmsLog();
        smsLog.setUserId(user.getId());
        smsLog.setSender(request.getSender());
        smsLog.setFullContent(request.getRawContent());
        smsLog.setDeviceInfo(request.getDeviceId());
        smsLog.setParseStatus("PENDING");
        smsLog = rawSmsLogRepository.save(smsLog);
        
        try {
            // 2. AI 解析
            ParsedTransaction parsed = aiParserService.parse(
                request.getRawContent(), 
                request.getSender()
            );
            
            if (parsed == null || parsed.getAmount() == null) {
                log.warn("AI 解析失败，标记为待处理");
                smsLog.setParseStatus("FAILED");
                smsLog.setErrorMessage("AI 无法解析出有效数据");
                rawSmsLogRepository.save(smsLog);
                return createManualTransaction(user, smsLog);
            }
            
            // 保存 AI 响应
            smsLog.setAiResponse(objectMapper.writeValueAsString(parsed));
            
            // 3. 生成幂等性键
            String idempotencyKey = idempotencyService.generateKey(
                parsed.getCardLastFour(),
                parsed.getAmount(),
                parsed.getMerchant(),
                request.getRawContent()
            );
            
            // 4. 检查重复
            if (!idempotencyService.checkAndSet(idempotencyKey)) {
                log.warn("检测到重复交易，幂等性键: {}", idempotencyKey);
                smsLog.setParseStatus("DUPLICATE");
                rawSmsLogRepository.save(smsLog);
                return transactionRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            }
            
            // 5. 查找账户
            Account account = null;
            if (parsed.getCardLastFour() != null) {
                account = accountRepository.findByUserIdAndLastFourDigits(
                    user.getId(), 
                    parsed.getCardLastFour()
                ).orElse(null);
            }
            
            // 6. 创建交易记录
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAccount(account);
            transaction.setType(TransactionType.valueOf(parsed.getType()));
            transaction.setAmount(parsed.getAmount());
            transaction.setMerchant(parsed.getMerchant());
            transaction.setTransactionTime(parseTransactionTime(parsed.getTransactionTime()));
            transaction.setRawLogId(smsLog.getId());
            transaction.setStatus(TransactionStatus.PENDING); // 待用户确认
            transaction.setIdempotencyKey(idempotencyKey);
            
            transaction = transactionRepository.save(transaction);
            
            // 7. 更新日志状态
            smsLog.setParseStatus("SUCCESS");
            rawSmsLogRepository.save(smsLog);
            
            log.info("交易记录创建成功，ID: {}", transaction.getId());
            return transaction;
            
        } catch (Exception e) {
            log.error("处理短信失败: {}", e.getMessage(), e);
            
            // 记录错误
            smsLog.setParseStatus("ERROR");
            smsLog.setErrorMessage(e.getMessage());
            rawSmsLogRepository.save(smsLog);
            
            // 创建一个待处理的交易记录（用户可以手动编辑）
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setType(TransactionType.EXPENSE); // 默认为支出
            transaction.setAmount(BigDecimal.ZERO);
            transaction.setMerchant("解析失败");
            transaction.setTransactionTime(LocalDateTime.now());
            transaction.setRawLogId(smsLog.getId());
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setRemark("AI 解析失败，请手动编辑");
            
            return transactionRepository.save(transaction);
        }
    }
    
    /**
     * 创建待手动补录的交易记录
     */
    private Transaction createManualTransaction(User user, RawSmsLog smsLog) {
        Transaction transaction = new Transaction();
        transaction.setUser(user); // Changed from setUserId to setUser
        transaction.setType(TransactionType.EXPENSE); // Default to EXPENSE
        transaction.setAmount(BigDecimal.ZERO);
        transaction.setMerchant("待补录");
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setRawLogId(smsLog.getId());
        transaction.setStatus(TransactionStatus.MANUAL); // Changed from "MANUAL" to TransactionStatus.MANUAL
        return transactionRepository.save(transaction);
    }
    
    /**
     * 解析交易时间
     */
    private LocalDateTime parseTransactionTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            return LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("解析交易时间失败，使用当前时间: {}", timeStr);
            return LocalDateTime.now();
        }
    }
}
