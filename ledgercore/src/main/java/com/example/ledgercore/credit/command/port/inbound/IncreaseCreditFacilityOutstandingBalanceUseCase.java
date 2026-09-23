package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto
        .IncreaseCreditFacilityOutstandingBalanceCommand;

public interface IncreaseCreditFacilityOutstandingBalanceUseCase {

    void execute(
            IncreaseCreditFacilityOutstandingBalanceCommand command
    );
}