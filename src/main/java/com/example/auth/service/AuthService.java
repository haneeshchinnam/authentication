package com.example.auth.service;

import com.example.auth.dto.AuthRequest;
import com.example.auth.dto.AuthResponse;
import com.example.auth.model.User;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest authRequest) {
        if(userRepository.existsByEmail(authRequest.getEmail()) || userRepository.existsByUsername(authRequest.getUsername())) {
            throw new RuntimeException("Username or email already exists");
        }

        User user = new User(authRequest.getUsername(), authRequest.getEmail(), passwordEncoder.encode(authRequest.getPassword()));
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);

        refreshTokenRepository.save(refreshToken, userDetails);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);

        refreshTokenRepository.save(refreshToken, userDetails);

        return new AuthResponse(accessToken, refreshToken);
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
