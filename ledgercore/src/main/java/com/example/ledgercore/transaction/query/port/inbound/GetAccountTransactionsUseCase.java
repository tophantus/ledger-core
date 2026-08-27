package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.transaction.query.dto.GetAccountTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface GetAccountTransactionsUseCase {

    PageResponse<TransactionResponse> execute(
            GetAccountTransactionsQuery query
    );
}