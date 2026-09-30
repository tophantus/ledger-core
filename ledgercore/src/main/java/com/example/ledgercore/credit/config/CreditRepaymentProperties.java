package com.example.ledgercore.credit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "credit.repayment")
public class CreditRepaymentProperties {

    private Scheduler scheduler = new Scheduler();

    @Getter
    @Setter
    public static class Scheduler {

        private boolean enabled;

        private long fixedDelay = 300_000;

        private int batchSize = 100;
    }
}