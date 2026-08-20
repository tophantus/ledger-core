package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountTransferPort {

    TransferAccountInfo getTransferInfo(
            UUID userId,
            UUID sourceAccountId,
            String destinationAccountNo
    );

    void verifySourceAccountAccess(
            UUID userId,
            UUID sourceAccountId
    );

    void transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount
    );

    record TransferAccountInfo(
            UUID sourceAccountId,
            UUID destinationAccountId,
            String currency,
            BigDecimal sourceBalance
    ) {
    }
}