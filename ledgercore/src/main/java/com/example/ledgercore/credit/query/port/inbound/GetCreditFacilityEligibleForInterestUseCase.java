package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.CreditFacilityInterestEligibility;

import java.time.LocalDate;
import java.util.List;

public interface GetCreditFacilityEligibleForInterestUseCase {
    List<CreditFacilityInterestEligibility> getEligibleCreditFacilitiesForInterest(
            LocalDate businessDate,
            String lastProcessedId,
            int batchSize
    );
}
