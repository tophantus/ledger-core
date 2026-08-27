package com.example.ledgercore.account.query.port.inbound;

import java.util.UUID;

public interface CheckAccountOwnershipUseCase {

    boolean execute(
            UUID userId,
            UUID accountId
    );
}