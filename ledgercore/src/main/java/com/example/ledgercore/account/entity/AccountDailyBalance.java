package com.example.ledgercore.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "account_daily_balances")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDailyBalance {

    @EmbeddedId
    private AccountDailyBalanceId id;

    @Column(
            name = "closing_balance",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal closingBalance;

    public UUID getAccountId() {
        return id.getAccountId();
    }

    public LocalDate getBusinessDate() {
        return id.getBusinessDate();
    }

    public void updateClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = Objects.requireNonNull(
                closingBalance,
                "closingBalance must not be null"
        );
    }

    @Embeddable
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDailyBalanceId
            implements Serializable {

        @Column(name = "account_id", nullable = false)
        private UUID accountId;

        @Column(name = "business_date", nullable = false)
        private LocalDate businessDate;
    }
}