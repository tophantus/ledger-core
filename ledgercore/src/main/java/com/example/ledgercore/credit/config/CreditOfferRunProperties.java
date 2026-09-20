package com.example.ledgercore.credit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "credit.offer.run")
public class CreditOfferRunProperties {

    private int batchSize = 100;
}