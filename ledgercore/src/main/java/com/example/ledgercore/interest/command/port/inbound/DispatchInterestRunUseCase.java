package com.example.ledgercore.interest.command.port.inbound;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;

public interface DispatchInterestRunUseCase {

    void execute(ClaimedInterestRun run);
}