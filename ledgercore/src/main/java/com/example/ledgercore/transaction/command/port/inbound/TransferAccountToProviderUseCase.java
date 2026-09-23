package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto
        .TransferAccountToProviderCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface TransferAccountToProviderUseCase {

    TransactionResponse execute(
            TransferAccountToProviderCommand command
    );
}