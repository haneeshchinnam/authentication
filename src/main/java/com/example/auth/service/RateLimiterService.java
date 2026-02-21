package com.example.auth.service;

import com.example.auth.core.TokenBucket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private static final int CAPACITY = 10;
    private static final int REFILL_TOKENS = 10;
    private static final long REFILL_INTERVAL_MS = 1000;

    public boolean allowRequest(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(
                key,
                k -> new TokenBucket(CAPACITY, REFILL_TOKENS, REFILL_INTERVAL_MS)
        );

        return bucket.tryConsume();
    }
}
