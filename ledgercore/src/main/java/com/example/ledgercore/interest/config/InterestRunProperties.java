package com.example.ledgercore.interest.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "interest.run")
public class InterestRunProperties {

    @Min(1)
    private int batchSize = 500;

    @NotNull
    private Duration leaseDuration = Duration.ofMinutes(5);
}