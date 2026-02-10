package com.easybill.controller;

import com.easybill.dto.SmsWebhookRequest;
import com.easybill.entity.Transaction;
import com.easybill.entity.User;
import com.easybill.repository.UserRepository;
import com.easybill.service.SmsProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
public class WebhookController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SmsProcessingService smsProcessingService;
    
    @PostMapping("/sms")
    public ResponseEntity<Map<String, Object>> handleSms(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SmsWebhookRequest request) {
        
        log.info("收到 Webhook 请求，发件人: {}", request.getSender());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 验证 API Key
            String apiKey = extractApiKey(authHeader);
            if (apiKey == null) {
                response.put("success", false);
                response.put("message", "缺少 Authorization Header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userRepository.findByApiKey(apiKey).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "无效的 API Key");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 2. 处理短信
            Transaction transaction = smsProcessingService.processSms(user, request);
            
            // 3. 返回结果
            response.put("success", true);
            response.put("message", "账单已记录");
            response.put("transaction_id", transaction.getId());
            response.put("status", transaction.getStatus());
            
            Map<String, Object> parsedData = new HashMap<>();
            parsedData.put("amount", transaction.getAmount());
            parsedData.put("merchant", transaction.getMerchant());
            parsedData.put("type", transaction.getType());
            response.put("parsed_data", parsedData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理 Webhook 失败: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 从 Authorization Header 提取 API Key
     * 支持格式: "Bearer {api_key}"
     */
    private String extractApiKey(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
