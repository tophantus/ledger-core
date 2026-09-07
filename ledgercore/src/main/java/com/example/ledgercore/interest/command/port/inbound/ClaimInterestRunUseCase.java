package com.example.ledgercore.interest.command.port.inbound;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;

import java.time.Instant;
import java.util.Optional;

public interface ClaimInterestRunUseCase {

    Optional<ClaimedInterestRun> execute(Instant claimAt);
}