package com.example.ledgercore.reconciliation.command.port.inbound;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;

import java.time.Instant;
import java.util.Optional;

public interface ClaimReconciliationRunUseCase {

    Optional<ClaimedReconciliationRun> execute(
            Instant claimAt
    );
}