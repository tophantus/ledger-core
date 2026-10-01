package com.example.ledgercore.credit.command.port.inbound.run;

import java.time.Instant;
import java.util.UUID;

public interface UpdateCreditStatementRunProgressUseCase {

    void execute(
            UUID runId,
            UUID lastProcessedFacilityId,
            Instant heartbeatAt
    );
}