package com.example.ledgercore.withdrawal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "withdrawal.request")
public class WithdrawalRequestProperties {

    private Duration expiration;
}