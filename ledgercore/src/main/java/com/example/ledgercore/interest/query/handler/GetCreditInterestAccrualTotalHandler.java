package com.example.ledgercore.interest.query.handler;

import com.example.ledgercore.interest.query.dto.GetCreditInterestAccrualTotalQuery;
import com.example.ledgercore.interest.query.dto.GetCreditInterestAccrualTotalResult;
import com.example.ledgercore.interest.query.port.inbound.GetCreditInterestAccrualTotalUseCase;
import com.example.ledgercore.interest.query.repository.CreditInterestAccrualQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetCreditInterestAccrualTotalHandler
        implements GetCreditInterestAccrualTotalUseCase {

    private final CreditInterestAccrualQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public GetCreditInterestAccrualTotalResult execute(
            GetCreditInterestAccrualTotalQuery query
    ) {
        validate(query);

        BigDecimal totalInterestAmount =
                repository
                        .sumInterestAmountByCreditFacilityIdAndBusinessDateBetween(
                                query.creditFacilityId(),
                                query.fromDate(),
                                query.toDate()
                        );

        return new GetCreditInterestAccrualTotalResult(
                query.creditFacilityId(),
                query.fromDate(),
                query.toDate(),
                totalInterestAmount != null
                        ? totalInterestAmount
                        : BigDecimal.ZERO
        );
    }

    private void validate(
            GetCreditInterestAccrualTotalQuery query
    ) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }

        if (query.creditFacilityId() == null) {
            throw new IllegalArgumentException(
                    "creditFacilityId must not be null"
            );
        }

        if (query.fromDate() == null) {
            throw new IllegalArgumentException(
                    "fromDate must not be null"
            );
        }

        if (query.toDate() == null) {
            throw new IllegalArgumentException(
                    "toDate must not be null"
            );
        }

        if (query.fromDate().isAfter(query.toDate())) {
            throw new IllegalArgumentException(
                    "fromDate must not be after toDate"
            );
        }
    }
}