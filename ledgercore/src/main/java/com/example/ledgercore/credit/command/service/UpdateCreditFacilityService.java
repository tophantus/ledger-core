package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityResult;

public interface UpdateCreditFacilityService {

    UpdateCreditFacilityResult update(
            UpdateCreditFacilityCommand command
    );
}