package com.example.auth.repository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RefreshTokenRepository {

    private final Map<String, UserDetails> tokenStore = new ConcurrentHashMap<>();

    public void save(String token, UserDetails userDetails) {
        tokenStore.put(token, userDetails);
    }

    public UserDetails findUserDetailsByToken(String token) {
        return tokenStore.get(token);
    }

    public void deleteToken(String token) {
        tokenStore.remove(token);
    }

}
