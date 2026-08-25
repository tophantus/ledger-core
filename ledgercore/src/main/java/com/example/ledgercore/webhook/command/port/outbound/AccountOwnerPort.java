package com.example.ledgercore.webhook.command.port.outbound;

import java.util.UUID;

public interface AccountOwnerPort {

    boolean isOwner(
            UUID accountId,
            UUID userId
    );
}