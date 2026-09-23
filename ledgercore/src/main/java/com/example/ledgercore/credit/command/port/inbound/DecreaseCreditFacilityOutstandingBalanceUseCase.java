package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto
        .DecreaseCreditFacilityOutstandingBalanceCommand;

public interface DecreaseCreditFacilityOutstandingBalanceUseCase {

    void execute(
            DecreaseCreditFacilityOutstandingBalanceCommand command
    );
}