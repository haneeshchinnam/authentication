package com.example.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThrottlingInterceptor implements HandlerInterceptor {

    private Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private static final int LIMIT = 5;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        int count = requestCounts.getOrDefault(clientIp, 0);
        if (count >= LIMIT) {
            response.setStatus(429); // Too Many Requests
            return false;
        }
        requestCounts.put(clientIp, count + 1);
        return true;
    }

}
