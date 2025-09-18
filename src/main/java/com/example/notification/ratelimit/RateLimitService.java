package com.example.notification.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimitService {
    
    @Value("${notification.rate-limit.default-capacity:100}")
    private long defaultCapacity;
    
    @Value("${notification.rate-limit.default-refill-rate:10}")
    private long defaultRefillRate;
    
    private final ConcurrentMap<String, TokenBucket> tenantBuckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String tenantId, long tokensRequired) {
        TokenBucket bucket = tenantBuckets.computeIfAbsent(tenantId, 
            k -> new TokenBucket(defaultCapacity, defaultRefillRate));
        
        return bucket.tryConsume(tokensRequired);
    }

    public boolean isAllowed(String tenantId, long tokensRequired, long capacity, long refillRate) {
        String bucketKey = tenantId + ":" + capacity + ":" + refillRate;
        TokenBucket bucket = tenantBuckets.computeIfAbsent(bucketKey, 
            k -> new TokenBucket(capacity, refillRate));
        
        return bucket.tryConsume(tokensRequired);
    }

    public long getAvailableTokens(String tenantId) {
        TokenBucket bucket = tenantBuckets.get(tenantId);
        return bucket != null ? bucket.getAvailableTokens() : defaultCapacity;
    }

    public void configureTenantRateLimit(String tenantId, long capacity, long refillRate) {
        String bucketKey = tenantId + ":" + capacity + ":" + refillRate;
        tenantBuckets.put(bucketKey, new TokenBucket(capacity, refillRate));
    }
}