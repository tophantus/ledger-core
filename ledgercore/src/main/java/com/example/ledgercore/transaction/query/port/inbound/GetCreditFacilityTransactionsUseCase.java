package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.transaction.query.dto.GetCreditFacilityTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface GetCreditFacilityTransactionsUseCase {

    PageResponse<TransactionResponse> execute(
            GetCreditFacilityTransactionsQuery query
    );
}