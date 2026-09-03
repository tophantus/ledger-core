package com.example.ledgercore.businessday.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalTime;

@Getter
@Setter
@ConfigurationProperties(prefix = "business-day")
public class BusinessDayProperties {

    private String timezone;

    private LocalTime closingStart;
}