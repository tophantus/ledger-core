package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.PostCreditInterestTransactionCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface PostCreditInterestTransactionUseCase {

    TransactionResponse execute(
            PostCreditInterestTransactionCommand command
    );
}