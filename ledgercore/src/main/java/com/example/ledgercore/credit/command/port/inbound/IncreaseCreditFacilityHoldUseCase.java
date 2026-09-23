package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.IncreaseCreditFacilityHoldCommand;

public interface IncreaseCreditFacilityHoldUseCase {

    void execute(
            IncreaseCreditFacilityHoldCommand command
    );
}