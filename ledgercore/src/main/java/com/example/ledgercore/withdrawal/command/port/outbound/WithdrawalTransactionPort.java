package com.example.ledgercore.withdrawal.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface WithdrawalTransactionPort {

    UUID withdraw(
            UUID accountId,
            BigDecimal amount,
            String currency,
            String reference,
            String description
    );
}