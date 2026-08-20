package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.GetTransactionByReferenceQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface GetTransactionByReferenceUseCase {

    TransactionResponse execute(
            GetTransactionByReferenceQuery query
    );
}