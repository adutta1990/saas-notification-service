package com.example.notification.service;

import com.example.notification.config.TenantConfiguration;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TenantConfigurationService {
    
    private final ConcurrentMap<String, TenantConfiguration> tenantConfigs = new ConcurrentHashMap<>();

    public TenantConfiguration getTenantConfiguration(String tenantId) {
        return tenantConfigs.computeIfAbsent(tenantId,
            k -> new TenantConfiguration(tenantId, 100L, 10L));
    }

    public void updateTenantConfiguration(TenantConfiguration config) {
        tenantConfigs.put(config.getTenantId(), config);
    }

    public void removeTenantConfiguration(String tenantId) {
        tenantConfigs.remove(tenantId);
    }

    public boolean hasTenantConfiguration(String tenantId) {
        return tenantConfigs.containsKey(tenantId);
    }
}