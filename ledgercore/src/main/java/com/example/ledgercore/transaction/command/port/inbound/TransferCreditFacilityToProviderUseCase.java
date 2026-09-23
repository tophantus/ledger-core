package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto
        .TransferCreditFacilityToProviderCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface TransferCreditFacilityToProviderUseCase {

    TransactionResponse execute(
            TransferCreditFacilityToProviderCommand command
    );
}