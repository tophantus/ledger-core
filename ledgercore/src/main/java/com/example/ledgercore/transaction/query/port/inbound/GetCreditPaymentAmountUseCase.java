package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.GetCreditPaymentAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPaymentAmountResult;

public interface GetCreditPaymentAmountUseCase {

    GetCreditPaymentAmountResult execute(
            GetCreditPaymentAmountQuery query
    );
}