package com.example.ledgercore.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.refresh-token.cookie")
public class RefreshTokenCookieProperties {

    private String name = "refresh_token";

    private String path = "/api/v1/auth";

    private Duration maxAge = Duration.ofDays(30);

    private boolean httpOnly = true;

    private boolean secure = true;

    private String sameSite = "Lax";
}