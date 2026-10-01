package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.CreateCreditFacilityFeeCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface CreateCreditFacilityFeeUseCase {

    TransactionResponse execute(
            CreateCreditFacilityFeeCommand command
    );
}