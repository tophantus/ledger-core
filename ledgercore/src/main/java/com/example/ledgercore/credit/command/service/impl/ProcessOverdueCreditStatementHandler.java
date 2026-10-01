package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.service.ProcessOverdueCreditStatementUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditBusinessDatePort;
import com.example.ledgercore.credit.command.port.outbound.CreditFacilityFeePort;
import com.example.ledgercore.credit.command.port.outbound.CreditInterestTransactionPort;
import com.example.ledgercore.credit.command.service.CreditStatementFeeCalculationService;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditStatementCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessOverdueCreditStatementHandler
        implements ProcessOverdueCreditStatementUseCase {

    private final CreditStatementCommandRepository
            creditStatementCommandRepository;

    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    private final CreditBusinessDatePort
            creditBusinessDatePort;

    private final CreditStatementFeeCalculationService
            feeCalculationService;

    private final CreditFacilityFeePort
            creditFacilityFeePort;

    private final CreditInterestTransactionPort
            creditInterestTransactionPort;

    @Override
    @Transactional
    public void execute(UUID statementId) {
        CreditStatement statement =
                creditStatementCommandRepository
                        .findByIdForUpdate(statementId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credit statement not found: "
                                                + statementId
                                )
                        );

        if (!isOverdueCandidate(statement)) {
            return;
        }

        CreditFacility facility =
                creditFacilityCommandRepository
                        .findById(statement.getCreditFacilityId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credit facility not found: "
                                                + statement
                                                .getCreditFacilityId()
                                )
                        );

        LocalDate businessDate =
                creditBusinessDatePort.getCurrentBusinessDate();

        Currency currency = facility.getCurrency();

        if (!statement.isMinimumPaymentSatisfied()) {
            applyOverdueFee(
                    statement,
                    currency
            );
        }

        if (!statement.isPaid()) {
            postInterest(
                    statement,
                    currency,
                    businessDate
            );
        }

        statement.markOverdue();
    }

    private void applyOverdueFee(
            CreditStatement statement,
            Currency currency
    ) {
        BigDecimal fee =
                feeCalculationService.calculateOverdueFee(
                        statement,
                        currency
                );

        if (fee.signum() <= 0) {
            return;
        }

        creditFacilityFeePort.createFee(
                statement.getCreditFacilityId(),
                fee,
                currency,
                buildFeeReference(statement),
                "Credit statement overdue fee"
        );
    }

    private void postInterest(
            CreditStatement statement,
            Currency currency,
            LocalDate businessDate
    ) {
        BigDecimal interestAmount =
                statement.getInterestAmount();

        if (interestAmount.signum() <= 0) {
            return;
        }

        creditInterestTransactionPort.postInterest(
                statement.getCreditFacilityId(),
                interestAmount,
                currency,
                businessDate
        );
    }

    private boolean isOverdueCandidate(
            CreditStatement statement
    ) {
        return statement.getStatus()
                == CreditStatementStatus.ISSUED
                || statement.getStatus()
                == CreditStatementStatus.PARTIALLY_PAID;
    }

    private String buildFeeReference(
            CreditStatement statement
    ) {
        return "OVERDUE-FEE-" + statement.getId();
    }
}