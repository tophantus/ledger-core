package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.GetCreditPurchasesAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPurchasesAmountResult;

public interface GetCreditPurchasesAmountUseCase {

    GetCreditPurchasesAmountResult execute(
            GetCreditPurchasesAmountQuery query
    );
}