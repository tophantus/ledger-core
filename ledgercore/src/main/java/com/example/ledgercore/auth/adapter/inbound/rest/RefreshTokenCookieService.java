package com.example.ledgercore.auth.adapter.inbound.rest;

import com.example.ledgercore.auth.config.RefreshTokenCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private final RefreshTokenCookieProperties properties;

    public void set(
            HttpServletResponse response,
            String refreshToken
    ) {
        ResponseCookie cookie =
                ResponseCookie.from(
                                properties.getName(),
                                refreshToken
                        )
                        .httpOnly(properties.isHttpOnly())
                        .secure(properties.isSecure())
                        .sameSite(properties.getSameSite())
                        .path(properties.getPath())
                        .maxAge(properties.getMaxAge())
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    public Optional<String> get(
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (properties.getName().equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }

        return Optional.empty();
    }

    public void delete(
            HttpServletResponse response
    ) {
        ResponseCookie cookie =
                ResponseCookie.from(
                                properties.getName(),
                                ""
                        )
                        .httpOnly(properties.isHttpOnly())
                        .secure(properties.isSecure())
                        .sameSite(properties.getSameSite())
                        .path(properties.getPath())
                        .maxAge(0)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}