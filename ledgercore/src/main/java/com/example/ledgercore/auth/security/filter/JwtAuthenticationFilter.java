package com.example.ledgercore.auth.security.filter;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null
                && SecurityContextHolder.getContext()
                .getAuthentication() == null
                && jwtService.validateAccessToken(token)) {

            authenticate(token);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {

        UUID userId = jwtService.extractUserId(token);
        Set<String> roles = jwtService.extractRoles(token);

        if (userId == null || roles == null) {
            return;
        }

        Set<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(
                        role.startsWith("ROLE_")
                                ? role
                                : "ROLE_" + role
                ))
                .collect(Collectors.toSet());

        AuthPrincipal principal = AuthPrincipal.builder()
                .userId(userId)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private String extractBearerToken(
            HttpServletRequest request
    ) {
        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }
}