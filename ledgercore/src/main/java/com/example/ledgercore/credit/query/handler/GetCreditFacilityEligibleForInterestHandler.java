package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.query.dto.CreditFacilityInterestEligibility;
import com.example.ledgercore.credit.query.port.inbound.GetCreditFacilityEligibleForInterestUseCase;
import com.example.ledgercore.credit.query.projection.CreditFacilityInterestBalanceProjection;
import com.example.ledgercore.credit.query.repository.CreditDailyBalanceQueryRepository;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCreditFacilityEligibleForInterestHandler
        implements GetCreditFacilityEligibleForInterestUseCase {

    private final CreditFacilityQueryRepository creditFacilityQueryRepository;
    private final CreditDailyBalanceQueryRepository creditDailyBalanceQueryRepository;

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

        List<CreditFacility> facilities =
                creditFacilityQueryRepository.findBatch(
                        cursor,
                        CreditFacilityStatus.ACTIVE,
                        PageRequest.of(0, batchSize)
                );

        if (facilities.isEmpty()) {
            return List.of();
        }

        List<UUID> facilityIds =
                facilities.stream()
                        .map(CreditFacility::getId)
                        .toList();

        Map<UUID, BigDecimal> closingBalances =
                creditDailyBalanceQueryRepository
                        .findEffectiveClosingBalances(
                                facilityIds,
                                businessDate
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                CreditFacilityInterestBalanceProjection
                                        ::getCreditFacilityId,
                                CreditFacilityInterestBalanceProjection
                                        ::getClosingBalance
                        ));

        return facilities.stream()
                .map(facility -> toEligibility(
                        facility,
                        closingBalances.getOrDefault(
                                facility.getId(),
                                BigDecimal.ZERO
                        )
                ))
                .toList();
    }

    private CreditFacilityInterestEligibility toEligibility(
            CreditFacility facility,
            BigDecimal closingBalance
    ) {
        return new CreditFacilityInterestEligibility(
                facility.getId(),
                facility.getProductId(),
                facility.getCurrency(),
                closingBalance
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