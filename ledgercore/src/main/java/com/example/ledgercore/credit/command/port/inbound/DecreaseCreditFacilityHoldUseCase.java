package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.DecreaseCreditFacilityHoldCommand;

public interface DecreaseCreditFacilityHoldUseCase {

    void execute(
            DecreaseCreditFacilityHoldCommand command
    );
}