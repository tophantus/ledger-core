package com.example.ledgercore.interest.query.port.inbound;

import com.example.ledgercore.interest.query.dto.GetCreditInterestAccrualTotalQuery;
import com.example.ledgercore.interest.query.dto.GetCreditInterestAccrualTotalResult;

public interface GetCreditInterestAccrualTotalUseCase {

    GetCreditInterestAccrualTotalResult execute(
            GetCreditInterestAccrualTotalQuery query
    );
}