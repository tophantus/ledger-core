package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface LedgerTransferPort {

    void recordTransfer(
            UUID transactionId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            LocalDate businessDate
    );
}