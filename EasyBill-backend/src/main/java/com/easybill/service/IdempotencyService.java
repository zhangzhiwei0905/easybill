package com.easybill.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String IDEMPOTENCY_PREFIX = "easybill:idempotency:";
    private static final long EXPIRATION_DAYS = 7;
    
    /**
     * 生成幂等性键
     * MD5(手机尾号 + 交易金额 + 商户名 + 原始短信全文)
     */
    public String generateKey(String phoneLastFour, BigDecimal amount, 
                             String merchant, String rawContent) {
        String raw = (phoneLastFour != null ? phoneLastFour : "") +
                    amount.toString() +
                    (merchant != null ? merchant : "") +
                    rawContent;
        return DigestUtils.md5Hex(raw);
    }
    
    /**
     * 检查是否重复
     * @return true 如果是新记录，false 如果已存在
     */
    public boolean checkAndSet(String key) {
        String redisKey = IDEMPOTENCY_PREFIX + key;
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(redisKey, "1", EXPIRATION_DAYS, TimeUnit.DAYS);
        return Boolean.TRUE.equals(result);
    }
    
    /**
     * 检查是否存在
     */
    public boolean exists(String key) {
        String redisKey = IDEMPOTENCY_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }
}
