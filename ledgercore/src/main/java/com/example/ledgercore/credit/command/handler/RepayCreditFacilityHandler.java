package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.RepayCreditFacilityUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditAccountBalancePort;
import com.example.ledgercore.credit.command.port.outbound.RepaymentTransactionPort;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditStatementCommandRepository;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import com.example.ledgercore.credit.enums.RepaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepayCreditFacilityHandler
        implements RepayCreditFacilityUseCase {

    private final CreditStatementCommandRepository
            creditStatementCommandRepository;

    private final CreditRepaymentMandateCommandRepository
            creditRepaymentMandateCommandRepository;

    private final CreditAccountBalancePort creditAccountBalancePort;

    private final RepaymentTransactionPort repaymentTransactionPort;

    @Override
    @Transactional
    public void execute(UUID statementId) {

        CreditStatement statement =
                creditStatementCommandRepository.findByIdForUpdate(statementId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Credit statement not found: "
                                                + statementId
                                ));

        validateStatement(statement);

        CreditRepaymentMandate mandate =
                creditRepaymentMandateCommandRepository
                        .findByCreditFacilityIdAndStatus(
                                statement.getCreditFacilityId(),
                                CreditRepaymentMandateStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Active repayment mandate not found"
                                ));

        BigDecimal repaymentAmount =
                calculateRepaymentAmount(
                        statement,
                        mandate.getRepaymentType()
                );

        if (repaymentAmount.signum() <= 0) {
            return;
        }

        CreditAccountBalancePort.AccountBalance account =
                creditAccountBalancePort.getActiveAccount(
                        mandate.getAccountId()
                );

        if (account.availableBalance()
                .compareTo(repaymentAmount) < 0) {

            scheduleNextAttempt(statement);

            return;
        }

        repaymentTransactionPort.repay(
                mandate.getAccountId(),
                statement.getCreditFacilityId(),
                repaymentAmount,
                account.currency(),
                buildReference(statement),
                buildDescription(statement)
        );

        statement.applyPayment(repaymentAmount);
        statement.clearNextRepaymentAttempt();
    }

    private void validateStatement(CreditStatement statement) {

        if (statement.getStatus() != CreditStatementStatus.ISSUED
                && statement.getStatus()
                != CreditStatementStatus.PARTIALLY_PAID) {

            throw new IllegalStateException(
                    "Credit statement is not eligible for repayment"
            );
        }

        if (statement.isPaid()) {
            return;
        }
    }

    private BigDecimal calculateRepaymentAmount(
            CreditStatement statement,
            RepaymentType repaymentType
    ) {
        BigDecimal remainingAmount =
                statement.getRemainingAmount();

        return switch (repaymentType) {
            case FULL_BALANCE ->
                    remainingAmount;

            case MINIMUM_PAYMENT -> {
                BigDecimal remainingMinimum =
                        statement.getMinimumPayment()
                                .subtract(statement.getPaidAmount());

                yield remainingMinimum.min(remainingAmount);
            }
        };
    }

    private void scheduleNextAttempt(CreditStatement statement) {
        statement.scheduleNextRepaymentAttempt(
                Instant.now().plusSeconds(5 * 60 * 60)
        );
    }

    private String buildReference(CreditStatement statement) {
        return "CREDIT_REPAYMENT-" + statement.getId();
    }

    private String buildDescription(CreditStatement statement) {
        return "Automatic repayment for credit statement "
                + statement.getId();
    }
}