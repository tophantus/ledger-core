package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerWithdrawPort {

    void recordWithdraw(
            UUID transactionId,
            UUID sourceAccountId,
            BigDecimal amount,
            String currency
    );
}