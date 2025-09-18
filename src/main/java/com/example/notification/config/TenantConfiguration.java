package com.example.notification.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfiguration {

    private String tenantId;
    private long rateLimitCapacity;
    private long rateLimitRefillRate;
    private Map<String, String> smsConfig;
    private Map<String, String> pushConfig;
    private Map<String, String> ivrsConfig;
    private Map<String, String> emailConfig;

    public TenantConfiguration(String tenantId, long rateLimitCapacity, long rateLimitRefillRate) {
        this.tenantId = tenantId;
        this.rateLimitCapacity = rateLimitCapacity;
        this.rateLimitRefillRate = rateLimitRefillRate;
    }
}