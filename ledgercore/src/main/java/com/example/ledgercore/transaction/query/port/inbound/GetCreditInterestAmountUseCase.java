package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.GetCreditInterestAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditInterestAmountResult;

public interface GetCreditInterestAmountUseCase {

    GetCreditInterestAmountResult execute(
            GetCreditInterestAmountQuery query
    );
}
