package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityResult;

import java.util.Optional;

public interface GetUserCreditFacilityUseCase {

    Optional<GetUserCreditFacilityResult> execute(
            GetUserCreditFacilityQuery query
    );
}