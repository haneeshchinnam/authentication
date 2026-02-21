package com.example.auth.core;

import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucket {

    private final int capacity;
    private final int refillTokens;
    private final long refillIntervalMillis;

    private AtomicInteger tokens;
    private volatile long lastRefillTimestamp;

    public TokenBucket(int capacity, int refillTokens, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillIntervalMillis = refillIntervalMillis;
        this.tokens = new AtomicInteger(capacity);
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp;

        if (elapsed > refillIntervalMillis) {
            long intervals = elapsed / refillIntervalMillis;
            int tokensToAdd = (int) (intervals * refillTokens);

            int newTokenCount = Math.min(capacity, tokens.get() + tokensToAdd);
            tokens.set(newTokenCount);

            lastRefillTimestamp += intervals * refillIntervalMillis;
        }
    }
}
