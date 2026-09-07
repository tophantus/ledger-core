package com.example.ledgercore.interest.service;

import com.example.ledgercore.interest.enums.DayCountConvention;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterestCalculationServiceTest {

    private final InterestCalculationService service =
            new InterestCalculationService();

    @Test
    void shouldCalculateDailyInterestUsingActual365() {
        BigDecimal result = service.calculateDailyInterest(
                new BigDecimal("100000000"),
                new BigDecimal("0.030000"),
                DayCountConvention.ACTUAL_365
        );

        assertThat(result)
                .isEqualByComparingTo("8219.1781");
    }

    @Test
    void shouldCalculateDailyInterestUsingActual360() {
        BigDecimal result = service.calculateDailyInterest(
                new BigDecimal("100000000"),
                new BigDecimal("0.030000"),
                DayCountConvention.ACTUAL_360
        );

        assertThat(result)
                .isEqualByComparingTo("8333.3333");
    }

    @Test
    void shouldReturnZeroWhenPrincipalIsZero() {
        BigDecimal result = service.calculateDailyInterest(
                BigDecimal.ZERO,
                new BigDecimal("0.030000"),
                DayCountConvention.ACTUAL_365
        );

        assertThat(result)
                .isEqualByComparingTo("0.0000");
    }

    @Test
    void shouldReturnZeroWhenRateIsZero() {
        BigDecimal result = service.calculateDailyInterest(
                new BigDecimal("100000000"),
                BigDecimal.ZERO,
                DayCountConvention.ACTUAL_365
        );

        assertThat(result)
                .isEqualByComparingTo("0.0000");
    }

    @Test
    void shouldRejectNegativePrincipal() {
        assertThatThrownBy(() ->
                service.calculateDailyInterest(
                        new BigDecimal("-1"),
                        new BigDecimal("0.03"),
                        DayCountConvention.ACTUAL_365
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("principal must not be negative");
    }

    @Test
    void shouldRejectNegativeRate() {
        assertThatThrownBy(() ->
                service.calculateDailyInterest(
                        new BigDecimal("100000000"),
                        new BigDecimal("-0.03"),
                        DayCountConvention.ACTUAL_365
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("annualRate must not be negative");
    }
}