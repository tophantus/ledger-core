package com.example.ledgercore.withdrawal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "withdrawal.intent")
public class WithdrawalIntentProperties {

    private Duration expiration;
}