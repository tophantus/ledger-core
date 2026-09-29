package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetCreditPaymentAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPaymentAmountResult;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditPaymentAmountUseCase;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetCreditPaymentAmountHandler
        implements GetCreditPaymentAmountUseCase {

    private final TransactionQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public GetCreditPaymentAmountResult execute(
            GetCreditPaymentAmountQuery query
    ) {
        validate(query);

        BigDecimal totalAmount =
                repository.sumCreditPaymentAmount(
                        query.creditFacilityId(),
                        TransactionType.CREDIT_PAYMENT,
                        TransactionStatus.COMPLETED,
                        query.fromDate(),
                        query.toDate()
                );

        return new GetCreditPaymentAmountResult(
                query.creditFacilityId(),
                query.fromDate(),
                query.toDate(),
                totalAmount
        );
    }

    private void validate(
            GetCreditPaymentAmountQuery query
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