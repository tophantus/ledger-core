package com.example.ledgercore.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class AuthConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}