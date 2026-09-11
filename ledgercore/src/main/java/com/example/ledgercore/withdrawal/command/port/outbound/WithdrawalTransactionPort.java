package com.example.ledgercore.withdrawal.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface WithdrawalTransactionPort {

    UUID withdraw(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    );
}