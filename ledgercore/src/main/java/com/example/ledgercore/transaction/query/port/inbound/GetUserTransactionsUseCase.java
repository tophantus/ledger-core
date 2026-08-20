package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.GetUserTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

import java.util.List;

public interface GetUserTransactionsUseCase {

    List<TransactionResponse> execute(
            GetUserTransactionsQuery query
    );
}