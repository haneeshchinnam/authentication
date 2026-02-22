package com.example.auth.security;

import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = parseJwt(request);

        System.out.println("is valid "+ tokenService.isValid(jwt));

        if (jwt != null && tokenService.isValid(jwt)) {
            String username = tokenService.extractUsername(jwt);
            Optional<User> user = userRepository.findByUsername(username);

            System.out.println("Username from JWT: " + username);
            System.out.println("User found in DB: " + user.isPresent());
            System.out.println("Current Authentication: " + SecurityContextHolder.getContext().getAuthentication());

            if (username != null && Objects.isNull(SecurityContextHolder.getContext().getAuthentication()) && user.isPresent()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                System.out.println("JWT "+ jwt);
                System.out.println("User Access Token "+ user.get().getAccessToken());

                if(tokenService.isValid(jwt, userDetails) && jwt.equals(user.get().getAccessToken())) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        System.out.println("header "+headerAuth);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
