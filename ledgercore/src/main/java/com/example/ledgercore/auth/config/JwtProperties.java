package com.example.ledgercore.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String app;

    private String secret;

    private Duration accessTokenExpiration = Duration.ofMinutes(15);
}
