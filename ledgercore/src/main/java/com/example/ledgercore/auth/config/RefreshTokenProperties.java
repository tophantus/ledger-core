package com.example.ledgercore.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt.refresh-token")
public class RefreshTokenProperties {

    private Duration expiration = Duration.ofDays(30);
}