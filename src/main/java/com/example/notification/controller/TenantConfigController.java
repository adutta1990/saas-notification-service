package com.example.notification.controller;

import com.example.notification.config.TenantConfiguration;
import com.example.notification.ratelimit.RateLimitService;
import com.example.notification.service.TenantConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantConfigController {
    
    @Autowired
    private TenantConfigurationService tenantConfigService;
    
    @Autowired
    private RateLimitService rateLimitService;

    @GetMapping("/{tenantId}/config")
    public ResponseEntity<TenantConfiguration> getTenantConfiguration(@PathVariable String tenantId) {
        TenantConfiguration config = tenantConfigService.getTenantConfiguration(tenantId);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/{tenantId}/config")
    public ResponseEntity<TenantConfiguration> updateTenantConfiguration(
            @PathVariable String tenantId,
            @RequestBody TenantConfiguration config) {
        config.setTenantId(tenantId);
        tenantConfigService.updateTenantConfiguration(config);
        
        rateLimitService.configureTenantRateLimit(tenantId, 
                                                config.getRateLimitCapacity(), 
                                                config.getRateLimitRefillRate());
        
        return ResponseEntity.ok(config);
    }

    @GetMapping("/{tenantId}/rate-limit/tokens")
    public ResponseEntity<Long> getAvailableTokens(@PathVariable String tenantId) {
        long tokens = rateLimitService.getAvailableTokens(tenantId);
        return ResponseEntity.ok(tokens);
    }
}