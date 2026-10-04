package com.example.ledgercore.interest.adapter.outbound.credit;

import com.example.ledgercore.credit.query.dto.CreditFacilityInterestEligibility;
import com.example.ledgercore.credit.query.port.inbound.GetCreditFacilityEligibleForInterestUseCase;
import com.example.ledgercore.interest.command.port.outbound.credit.InterestEligibleCreditFacility;
import com.example.ledgercore.interest.command.port.outbound.credit.InterestEligibleCreditFacilityQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InterestEligibleCreditFacilityQueryAdapter
        implements InterestEligibleCreditFacilityQueryPort {

    private final GetCreditFacilityEligibleForInterestUseCase
            getCreditFacilityEligibleForInterestUseCase;

    @Override
    public List<InterestEligibleCreditFacility> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    ) {
        return getCreditFacilityEligibleForInterestUseCase
                .getEligibleCreditFacilitiesForInterest(
                        businessDate,
                        lastProcessedId,
                        batchSize
                )
                .stream()
                .map(this::toInterestEligibleCreditFacility)
                .toList();
    }

    private InterestEligibleCreditFacility toInterestEligibleCreditFacility(
            CreditFacilityInterestEligibility eligibility
    ) {
        return new InterestEligibleCreditFacility(
                eligibility.creditFacilityId(),
                eligibility.productId(),
                eligibility.currency(),
                eligibility.closingBalance()
        );
    }
}