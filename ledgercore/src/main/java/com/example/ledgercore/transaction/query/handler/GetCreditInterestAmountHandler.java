package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetCreditInterestAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditInterestAmountResult;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditInterestAmountUseCase;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetCreditInterestAmountHandler
        implements GetCreditInterestAmountUseCase {

    private final TransactionQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public GetCreditInterestAmountResult execute(
            GetCreditInterestAmountQuery query
    ) {
        validate(query);

        BigDecimal totalAmount =
                repository.sumCreditInterestAmount(
                        query.creditFacilityId(),
                        TransactionType.INTEREST,
                        TransactionStatus.COMPLETED,
                        query.fromDate(),
                        query.toDate()
                );

        return new GetCreditInterestAmountResult(
                query.creditFacilityId(),
                query.fromDate(),
                query.toDate(),
                totalAmount
        );
    }

    private void validate(
            GetCreditInterestAmountQuery query
    ) {
        if (query == null) {
            throw new IllegalArgumentException(
                    "query must not be null"
            );
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
