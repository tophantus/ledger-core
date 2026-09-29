package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetCreditPurchasesAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPurchasesAmountResult;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditPurchasesAmountUseCase;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCreditPurchasesAmountHandler
        implements GetCreditPurchasesAmountUseCase {

    private static final List<TransactionType> PURCHASE_TYPES = List.of(
            TransactionType.CARD_PAYMENT,
            TransactionType.CASH_ADVANCE
    );

    private final TransactionQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public GetCreditPurchasesAmountResult execute(
            GetCreditPurchasesAmountQuery query
    ) {
        validate(query);

        BigDecimal totalAmount =
                repository.sumCreditFacilityPurchasesAmount(
                        query.creditFacilityId(),
                        PURCHASE_TYPES,
                        TransactionStatus.COMPLETED,
                        query.fromDate(),
                        query.toDate()
                );

        return new GetCreditPurchasesAmountResult(
                query.creditFacilityId(),
                query.fromDate(),
                query.toDate(),
                totalAmount
        );
    }

    private void validate(
            GetCreditPurchasesAmountQuery query
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