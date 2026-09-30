package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.query.dto.CreditFacilityInterestEligibility;
import com.example.ledgercore.credit.query.port.inbound.GetCreditFacilityEligibleForInterestUseCase;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreditFacilityEligibleForInterestHandler
        implements GetCreditFacilityEligibleForInterestUseCase {

    private final CreditFacilityQueryRepository
            creditFacilityQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CreditFacilityInterestEligibility>
    getEligibleCreditFacilitiesForInterest(
            LocalDate businessDate,
            String lastProcessedId,
            int batchSize
    ) {
        validate(
                businessDate,
                lastProcessedId,
                batchSize
        );

        UUID cursor = lastProcessedId == null
                ? null
                : UUID.fromString(lastProcessedId);

        return creditFacilityQueryRepository
                .findBatch(
                        cursor,
                        CreditFacilityStatus.ACTIVE,
                        PageRequest.of(0, batchSize)
                )
                .stream()
                .map(this::toEligibility)
                .toList();
    }

    private CreditFacilityInterestEligibility toEligibility(
            com.example.ledgercore.credit.entity.CreditFacility facility
    ) {
        return new CreditFacilityInterestEligibility(
                facility.getId(),
                facility.getProductId(),
                facility.getCurrency()
        );
    }

    private void validate(
            LocalDate businessDate,
            String lastProcessedId,
            int batchSize
    ) {
        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        if (lastProcessedId != null) {
            try {
                UUID.fromString(lastProcessedId);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "lastProcessedId must be a valid UUID",
                        exception
                );
            }
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than zero"
            );
        }
    }
}