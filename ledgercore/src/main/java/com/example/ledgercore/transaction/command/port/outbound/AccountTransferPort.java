package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface AccountTransferPort {

    TransferAccountInfo getTransferInfo(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    );

    UUID getAccountIdByAccountNo(
            String accountNo
    );

    void verifySourceAccountAccess(
            UUID userId,
            UUID sourceAccountId
    );

    void transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            LocalDate businessDate
    );

    record TransferAccountInfo(
            UUID sourceAccountId,
            UUID destinationAccountId,
            String currency,
            BigDecimal sourceBalance
    ) {
    }
}