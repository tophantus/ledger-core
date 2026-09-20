package com.example.ledgercore.credit.command.port.inbound;

import java.time.Instant;
import java.util.UUID;

public interface UpdateCreditOfferRunProgressUseCase {

    void execute(
            UUID runId,
            UUID lastProcessedId,
            Instant heartbeatAt
    );
}