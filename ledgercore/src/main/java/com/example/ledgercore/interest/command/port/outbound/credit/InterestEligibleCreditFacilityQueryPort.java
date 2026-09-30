package com.example.ledgercore.interest.command.port.outbound.credit;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InterestEligibleCreditFacilityQueryPort {

    List<InterestEligibleCreditFacility> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    );
}