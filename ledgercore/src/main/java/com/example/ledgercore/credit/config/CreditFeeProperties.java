package com.example.ledgercore.credit.config;

import com.example.ledgercore.common.currency.Currency;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "credit.fee")
public class CreditFeeProperties {

    private Map<Currency, BigDecimal> overdueFees =
            new EnumMap<>(Currency.class);
}