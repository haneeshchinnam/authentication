package com.example.auth.service;

import com.example.auth.dto.AuthRequest;
import com.example.auth.dto.AuthResponse;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(AuthRequest authRequest) {
        if(userRepository.existsByEmail(authRequest.email()) || userRepository.existsByUsername(authRequest.username())) {
            throw new RuntimeException("Username or email already exists");
        }

        User user = new User(authRequest.username(), authRequest.email(), passwordEncoder.encode(authRequest.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);

        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);

        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);

        Optional<User> userOpt = userRepository.findByUsername(request.username());

        if (userOpt.isPresent()) {
            userOpt.get().setAccessToken(accessToken);
            userOpt.get().setRefreshToken(refreshToken);
            userRepository.save(userOpt.get());
        } else {
            throw new UserNotFoundException("User not found");
        }

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        System.out.println("valid token "+ tokenService.isValid(refreshToken));
        if (!tokenService.isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = tokenService.extractUsername(refreshToken);

        UserDetails storedUserDetails = userDetailsService.loadUserByUsername(username);
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (storedUserDetails == null) {
            throw new RuntimeException("Refresh Token not found");
        }

        if (!storedUserDetails.getUsername().equals(storedUserDetails.getUsername()) ||
                (userOpt.isPresent() && !refreshToken.equals(userOpt.get().getRefreshToken()))) {
            throw new RuntimeException("Invalid refresh token");
        }

//        refreshTokenRepository.deleteToken(refreshToken);

        String accessToken = tokenService.generateAccessToken(storedUserDetails);

        if (userOpt.isPresent()) {
            userOpt.get().setAccessToken(accessToken);
            userRepository.save(userOpt.get());
        } else {
            throw new UserNotFoundException("User not found");
        }
        return new AuthResponse(accessToken, refreshToken);
    }
}
