package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.CreditFacilityInterestEligibility;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetCreditFacilityEligibleForInterestUseCase {
    List<CreditFacilityInterestEligibility> getEligibleCreditFacilitiesForInterest(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    );
}
