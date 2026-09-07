package com.example.ledgercore.interest.command.port.inbound;

import java.time.Instant;
import java.util.UUID;

public interface CompleteInterestRunUseCase {

    void execute(
            UUID runId,
            Instant completedAt
    );
}