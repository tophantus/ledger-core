package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.CreateCreditFacilityCommand;
import com.example.ledgercore.credit.command.dto.CreateCreditFacilityResult;

public interface CreateCreditFacilityUseCase {

    CreateCreditFacilityResult execute(
            CreateCreditFacilityCommand command
    );
}