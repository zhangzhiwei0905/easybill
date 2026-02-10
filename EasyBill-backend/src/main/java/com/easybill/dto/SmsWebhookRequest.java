package com.easybill.dto;

import lombok.Data;

@Data
public class SmsWebhookRequest {
    
    private String rawContent;
    
    private String sender;
    
    private String timestamp;
    
    private String deviceId;
}
