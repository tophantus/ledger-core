package com.example.ledgercore.reconciliation.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "reconciliation.run")
public class ReconciliationRunProperties {

    @Min(1)
    private int batchSize = 500;

    @NotNull
    private Duration leaseDuration = Duration.ofMinutes(5);
}