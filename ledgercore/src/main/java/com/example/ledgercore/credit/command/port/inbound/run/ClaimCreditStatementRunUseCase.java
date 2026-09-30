package com.example.ledgercore.credit.command.port.inbound.run;

import com.example.ledgercore.credit.command.dto.run.ClaimedCreditStatementRun;

import java.time.Instant;
import java.util.Optional;

public interface ClaimCreditStatementRunUseCase {

    Optional<ClaimedCreditStatementRun> execute(
            Instant claimAt
    );
}