package com.example.auth.service;

import com.example.auth.dto.AuthRequest;
import com.example.auth.dto.AuthResponse;
import com.example.auth.model.User;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public AuthResponse register(AuthRequest authRequest) {
        if(userRepository.existsByEmail(authRequest.email()) || userRepository.existsByUsername(authRequest.username())) {
            throw new RuntimeException("Username or email already exists");
        }

        User user = new User(authRequest.username(), authRequest.email(), passwordEncoder.encode(authRequest.password()));
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);

        refreshTokenRepository.save(refreshToken, userDetails);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        log.info("Authentication successful: {}", authentication.isAuthenticated());
        try {
            log.info("Authorities: {}", objectMapper.writeValueAsString(authentication.getDetails()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

            String accessToken = tokenService.generateAccessToken(userDetails);
            String refreshToken = tokenService.generateRefreshToken(userDetails);

            refreshTokenRepository.save(refreshToken, userDetails);

            return new AuthResponse(accessToken, refreshToken);
        }
        throw new RuntimeException("Authentication Failed");
    }

    public String refreshToken(String refreshToken) {
        if (!tokenService.isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        UserDetails storedUserDetails = refreshTokenRepository.findUserDetailsByToken(refreshToken);

        if (storedUserDetails == null) {
            throw new RuntimeException("Refresh Token not found");
        }

        String username = tokenService.extractUsername(refreshToken);

        UserDetails currentUserDetails = userDetailsService.loadUserByUsername(username);

        if (!storedUserDetails.getUsername().equals(currentUserDetails.getUsername())) {
            throw new RuntimeException("Invalid refresh token");
        }
        return tokenService.generateRefreshToken(currentUserDetails);
    }
}
