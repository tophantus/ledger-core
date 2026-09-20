package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.GetOwnedCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.OwnedCreditFacilityInfo;

public interface GetOwnedCreditFacilityUseCase {

    OwnedCreditFacilityInfo execute(
            GetOwnedCreditFacilityQuery query
    );
}