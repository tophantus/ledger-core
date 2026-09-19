package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.ClaimedCreditOfferRun;

import java.time.Instant;
import java.util.Optional;

public interface ClaimCreditOfferRunUseCase {

    Optional<ClaimedCreditOfferRun> execute(Instant claimAt);
}