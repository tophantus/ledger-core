package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.GetActiveOwnedCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetActiveOwnedCreditFacilityResult;

public interface GetActiveOwnedCreditFacilityUseCase {

    GetActiveOwnedCreditFacilityResult execute(
            GetActiveOwnedCreditFacilityQuery query
    );
}