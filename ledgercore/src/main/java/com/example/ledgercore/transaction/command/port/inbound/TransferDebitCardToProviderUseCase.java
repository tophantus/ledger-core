package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.TransferDebitCardToProviderCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface TransferDebitCardToProviderUseCase {

    TransactionResponse execute(
            TransferDebitCardToProviderCommand command
    );
}