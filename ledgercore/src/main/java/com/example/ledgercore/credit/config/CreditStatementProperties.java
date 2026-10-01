package com.example.ledgercore.credit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "credit.statement")
public class CreditStatementProperties {

    private MinimumPayment minimumPayment = new MinimumPayment();

    private Overdue overdue = new Overdue();

    @Getter
    @Setter
    public static class MinimumPayment {

        private BigDecimal rate;

        private BigDecimal fixedMinimum;
    }

    @Getter
    @Setter
    public static class Overdue {

        private int batchSize = 100;
    }
}