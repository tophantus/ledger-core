package com.example.ledgercore.interest.service;

import com.example.ledgercore.interest.enums.DayCountConvention;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
public class InterestCalculationService {

    private static final BigDecimal DAYS_365 =
            BigDecimal.valueOf(365);

    private static final BigDecimal DAYS_360 =
            BigDecimal.valueOf(360);

    private static final int SCALE = 4;

    public BigDecimal calculateDailyInterest(
            BigDecimal principal,
            BigDecimal annualRate,
            DayCountConvention convention
    ) {
        Objects.requireNonNull(
                principal,
                "principal must not be null"
        );

        Objects.requireNonNull(
                annualRate,
                "annualRate must not be null"
        );

        Objects.requireNonNull(
                convention,
                "convention must not be null"
        );

        if (principal.signum() < 0) {
            throw new IllegalArgumentException(
                    "principal must not be negative"
            );
        }

        if (annualRate.signum() < 0) {
            throw new IllegalArgumentException(
                    "annualRate must not be negative"
            );
        }

        BigDecimal denominator = switch (convention) {
            case ACTUAL_365 -> DAYS_365;
            case ACTUAL_360 -> DAYS_360;
        };

        return principal
                .multiply(annualRate)
                .divide(
                        denominator,
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }
}