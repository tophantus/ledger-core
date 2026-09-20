package com.example.ledgercore.credit.command.port.inbound;

import java.time.Instant;
import java.util.UUID;

public interface CompleteCreditOfferRunUseCase {

    void execute(
            UUID runId,
            Instant completedAt
    );
}