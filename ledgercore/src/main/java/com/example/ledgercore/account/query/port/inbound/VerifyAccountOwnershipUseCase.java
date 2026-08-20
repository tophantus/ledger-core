package com.example.ledgercore.account.query.port.inbound;

import java.util.UUID;

public interface VerifyAccountOwnershipUseCase {

    void execute(
            UUID userId,
            UUID accountId
    );
}