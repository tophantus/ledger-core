package com.example.ledgercore.reconciliation.command.port.inbound;

import java.util.UUID;

public interface UpdateReconciliationRunProgressUseCase {

    void execute(
            UUID runId,
            UUID lastProcessedId,
            long processedCount
    );
}