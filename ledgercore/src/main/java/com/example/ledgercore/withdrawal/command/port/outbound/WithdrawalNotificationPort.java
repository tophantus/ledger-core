package com.example.ledgercore.withdrawal.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface WithdrawalNotificationPort {

    void sendWithdrawalCode(
            UUID withdrawalIntentId,
            UUID userId,
            String withdrawalLookupCode,
            String withdrawalCode,
            BigDecimal amount,
            Currency currency,
            Instant expiresAt
    );
}