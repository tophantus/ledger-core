package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.GetCreditFeeAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditFeeAmountResult;

public interface GetCreditFeeAmountUseCase {

    GetCreditFeeAmountResult execute(
            GetCreditFeeAmountQuery query
    );
}