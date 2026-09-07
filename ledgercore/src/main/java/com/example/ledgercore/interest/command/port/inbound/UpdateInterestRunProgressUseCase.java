package com.example.ledgercore.interest.command.port.inbound;

import java.time.Instant;
import java.util.UUID;

public interface UpdateInterestRunProgressUseCase {

    void execute(
            UUID runId,
            UUID lastProcessedId,
            long processedCount
    );
}