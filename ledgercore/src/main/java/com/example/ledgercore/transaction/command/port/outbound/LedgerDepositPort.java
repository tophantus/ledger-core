package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface LedgerDepositPort {

    void recordDeposit(
            UUID transactionId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            LocalDate businessDate
    );
}