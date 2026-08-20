package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.GetTransactionQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface GetTransactionUseCase {

    TransactionResponse execute(
            GetTransactionQuery query
    );
}