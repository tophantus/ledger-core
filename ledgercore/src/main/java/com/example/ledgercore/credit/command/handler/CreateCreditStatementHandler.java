package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.CreateCreditStatementCommand;
import com.example.ledgercore.credit.command.port.inbound.CreateCreditStatementUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditStatementAmountsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditStatementAmounts;
import com.example.ledgercore.credit.command.repository.CreditDailyBalanceCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditStatementCommandRepository;
import com.example.ledgercore.credit.command.service.CreditMinimumPaymentService;
import com.example.ledgercore.credit.entity.CreditDailyBalance;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCreditStatementHandler
        implements CreateCreditStatementUseCase {

    private final CreditFacilityCommandRepository facilityRepository;
    private final CreditStatementCommandRepository statementRepository;
    private final CreditDailyBalanceCommandRepository dailyBalanceRepository;
    private final CreditStatementAmountsPort amountsPort;
    private final CreditMinimumPaymentService minimumPaymentService;

    @Override
    @Transactional
    public void execute(CreateCreditStatementCommand command) {

        validateCommand(command);

        CreditFacility facility = facilityRepository
                .findById(command.creditFacilityId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                ));

        if (facility.getStatus() != CreditFacilityStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_ACTIVE
            );
        }

        if (statementRepository
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        command.creditFacilityId(),
                        command.periodStart(),
                        command.periodEnd()
                )) {
            throw new BusinessException(
                    ErrorCode.CREDIT_STATEMENT_ALREADY_EXISTS
            );
        }

        BigDecimal openingBalance = statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        command.creditFacilityId(),
                        command.periodStart()
                )
                .map(CreditStatement::getClosingBalance)
                .orElse(BigDecimal.ZERO);

        CreditDailyBalance dailyBalance = dailyBalanceRepository
                .findFirstByCreditFacilityIdAndBusinessDateLessThanEqualOrderByBusinessDateDesc(
                        command.creditFacilityId(),
                        command.periodEnd()
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CREDIT_DAILY_BALANCE_NOT_FOUND
                ));

        CreditStatementAmounts amounts = amountsPort.getAmounts(
                command.creditFacilityId(),
                command.periodStart(),
                command.periodEnd()
        );

        BigDecimal closingBalance = dailyBalance.getClosingBalance();

        BigDecimal expectedClosingBalance = openingBalance
                .add(amounts.purchasesAmount())
                .add(amounts.feesAmount())
                .add(amounts.interestAmount())
                .subtract(amounts.paymentsAmount());

        if (expectedClosingBalance.compareTo(closingBalance) != 0) {
            throw new IllegalStateException(
                    "Expected closing balance does not match daily balance"
            );
        }

        BigDecimal minimumPayment = minimumPaymentService.calculate(
                closingBalance,
                facility.getCurrency()
        );

        CreditStatementStatus status =
                closingBalance.signum() == 0
                        ? CreditStatementStatus.CLOSED
                        : CreditStatementStatus.ISSUED;

        Instant now = Instant.now();

        statementRepository.save(
                CreditStatement.builder()
                        .id(UUID.randomUUID())
                        .runId(command.runId())
                        .creditFacilityId(command.creditFacilityId())
                        .periodStart(command.periodStart())
                        .periodEnd(command.periodEnd())
                        .statementDate(command.statementDate())
                        .dueDate(command.dueDate())
                        .openingBalance(openingBalance)
                        .purchasesAmount(amounts.purchasesAmount())
                        .paymentsAmount(amounts.paymentsAmount())
                        .feesAmount(amounts.feesAmount())
                        .interestAmount(amounts.interestAmount())
                        .closingBalance(closingBalance)
                        .minimumPayment(minimumPayment)
                        .paidAmount(BigDecimal.ZERO)
                        .status(status)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
    }

    private void validateCommand(CreateCreditStatementCommand command) {
        if (command == null
                || command.runId() == null
                || command.creditFacilityId() == null
                || command.periodStart() == null
                || command.periodEnd() == null
                || command.statementDate() == null
                || command.dueDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (command.periodStart().isAfter(command.periodEnd())
                || command.periodEnd().isAfter(command.statementDate())
                || command.statementDate().isAfter(command.dueDate())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}