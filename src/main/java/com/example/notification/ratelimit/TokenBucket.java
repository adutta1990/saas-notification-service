package com.example.notification.ratelimit;

import lombok.Getter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucket {
    private final long capacity;
    @Getter
    private final long refillRate;
    private final AtomicLong tokens;
    private volatile long lastRefillTimestamp;

    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = Instant.now().toEpochMilli();
    }

    public synchronized boolean tryConsume(long tokensToConsume) {
        refill();
        
        long currentTokens = tokens.get();
        if (currentTokens >= tokensToConsume) {
            tokens.addAndGet(-tokensToConsume);
            return true;
        }
        return false;
    }

    private void refill() {
        long currentTime = Instant.now().toEpochMilli();
        long timeDiff = currentTime - lastRefillTimestamp;
        
        if (timeDiff > 0) {
            long tokensToAdd = (timeDiff / 1000) * refillRate;
            if (tokensToAdd > 0) {
                long newTokenCount = Math.min(capacity, tokens.get() + tokensToAdd);
                tokens.set(newTokenCount);
                lastRefillTimestamp = currentTime;
            }
        }
    }

    public long getAvailableTokens() {
        refill();
        return tokens.get();
    }

}