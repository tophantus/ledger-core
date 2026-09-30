package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.RepayCreditFacilityCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface RepayCreditFacilityUseCase {

    TransactionResponse execute(
            RepayCreditFacilityCommand command
    );
}