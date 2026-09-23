package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.TransferCreditCardToProviderCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface TransferCreditCardToProviderUseCase {

    TransactionResponse execute(
            TransferCreditCardToProviderCommand command
    );
}