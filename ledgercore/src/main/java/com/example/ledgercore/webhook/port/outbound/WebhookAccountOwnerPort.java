package com.example.ledgercore.webhook.port.outbound;

import java.util.UUID;

public interface WebhookAccountOwnerPort {

    void verifyOwnership(
            UUID userId,
            UUID accountId
    );
}