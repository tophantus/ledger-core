package com.example.ledgercore.credit.command.port.outbound;

import java.util.UUID;

public interface VerifyRepaymentAccountOwnershipPort {

    boolean verify(
            UUID userId,
            UUID accountId
    );
}