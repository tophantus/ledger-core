package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityResult;

public interface CreateCreditFacilityService {

    CreateCreditFacilityResult create(
            CreateCreditFacilityCommand command
    );
}