package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.PostInterestTransactionCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface PostInterestTransactionUseCase {

    TransactionResponse execute(
            PostInterestTransactionCommand command
    );
}