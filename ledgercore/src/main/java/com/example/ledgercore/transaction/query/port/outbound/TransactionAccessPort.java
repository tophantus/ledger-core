package com.example.ledgercore.transaction.query.port.outbound;

import java.util.UUID;

public interface TransactionAccessPort {

    void verifyAccess(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    );

    void verifyAccess(
            UUID userId,
            UUID accountId
    );
}