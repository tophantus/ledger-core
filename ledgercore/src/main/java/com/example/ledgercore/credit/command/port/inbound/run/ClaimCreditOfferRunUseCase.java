package com.example.ledgercore.credit.command.port.inbound.run;

import com.example.ledgercore.credit.command.dto.run.ClaimedCreditOfferRun;

import java.time.Instant;
import java.util.Optional;

public interface ClaimCreditOfferRunUseCase {

    Optional<ClaimedCreditOfferRun> execute(Instant claimAt);
}