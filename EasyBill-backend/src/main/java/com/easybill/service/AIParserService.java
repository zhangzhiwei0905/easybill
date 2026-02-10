package com.easybill.service;

import com.easybill.dto.ParsedTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AIParserService {
    
    @Autowired
    private ChatClient.Builder chatClientBuilder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 默认 Prompt 模板
    private static final String DEFAULT_PROMPT = """
        你是一个专业的银行短信解析助手。请从以下短信中提取交易信息，返回 JSON 格式：
        {
          "type": "INCOME 或 EXPENSE",
          "amount": "金额（数字）",
          "merchant": "商户名称",
          "cardLastFour": "卡号尾号（如有）",
          "transactionTime": "交易时间（ISO 8601 格式）",
          "categoryHint": "分类提示（如：餐饮、交通）"
        }
        
        注意：
        1. 金额必须是正数
        2. 如果短信中提到"消费"、"支出"，type 为 EXPENSE
        3. 如果短信中提到"存入"、"到账"，type 为 INCOME
        4. 商户名称尽量完整提取
        5. 如果无法提取某个字段，返回 null
        6. 只返回 JSON，不要有其他文字
        
        短信内容：
        """;
    
    /**
     * 解析短信内容
     */
    public ParsedTransaction parse(String rawContent, String sender) {
        try {
            log.info("开始解析短信，发件人: {}", sender);
            
            // 构建 Prompt（后续可以根据 sender 选择不同模板）
            String prompt = DEFAULT_PROMPT + rawContent;
            
            // 调用 DeepSeek API
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
            
            log.info("AI 解析结果: {}", response);
            
            // 清理响应（移除可能的 markdown 代码块标记）
            String cleanedResponse = response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
            
            // 解析 JSON
            ParsedTransaction result = objectMapper.readValue(
                cleanedResponse, 
                ParsedTransaction.class
            );
            
            return result;
            
        } catch (Exception e) {
            log.error("AI 解析失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 使用自定义 Prompt 模板解析
     */
    public ParsedTransaction parseWithTemplate(String rawContent, String promptTemplate) {
        try {
            String prompt = promptTemplate + "\n\n短信内容：" + rawContent;
            
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
            
            String cleanedResponse = response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
            
            return objectMapper.readValue(cleanedResponse, ParsedTransaction.class);
            
        } catch (Exception e) {
            log.error("AI 解析失败（自定义模板）: {}", e.getMessage(), e);
            return null;
        }
    }
}
