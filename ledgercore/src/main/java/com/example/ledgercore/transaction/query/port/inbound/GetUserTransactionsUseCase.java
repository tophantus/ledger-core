package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.transaction.query.dto.GetUserTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface GetUserTransactionsUseCase {

    PageResponse<TransactionResponse> execute(
            GetUserTransactionsQuery query
    );
}