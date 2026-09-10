package com.example.ledgercore.withdrawal.query.port.outbound;

import java.util.UUID;

public interface AccountOwnershipPort {

    boolean checkOwnership(
            UUID userId,
            UUID accountId
    );
}