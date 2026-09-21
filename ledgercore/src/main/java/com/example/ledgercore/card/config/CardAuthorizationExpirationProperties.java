package com.example.ledgercore.card.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "card.authorization.expiration")
public class CardAuthorizationExpirationProperties {

    private int batchSize = 100;
}