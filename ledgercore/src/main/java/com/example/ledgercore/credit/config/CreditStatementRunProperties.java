package com.example.ledgercore.credit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "credit.statement.run")
public class CreditStatementRunProperties {

    private int batchSize = 100;
}