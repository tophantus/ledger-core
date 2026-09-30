
package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.CreditAccountBalancePort;
import com.example.ledgercore.credit.command.port.outbound.RepaymentTransactionPort;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditStatementCommandRepository;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import com.example.ledgercore.credit.enums.RepaymentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepayCreditFacilityHandlerTest {

    @Mock
    private CreditStatementCommandRepository creditStatementCommandRepository;

    @Mock
    private CreditRepaymentMandateCommandRepository creditRepaymentMandateCommandRepository;

    @Mock
    private CreditAccountBalancePort creditAccountBalancePort;

    @Mock
    private RepaymentTransactionPort repaymentTransactionPort;

    @Mock
    private CreditStatement statement;

    @Mock
    private CreditRepaymentMandate mandate;

    @InjectMocks
    private RepayCreditFacilityHandler handler;

    private UUID statementId;
    private UUID facilityId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        facilityId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldRepayFullBalanceSuccessfully() {
        BigDecimal remaining = new BigDecimal("500.00");
        BigDecimal availableBalance = new BigDecimal("1000.00");

        CreditAccountBalancePort.AccountBalance account =
                new CreditAccountBalancePort.AccountBalance(
                        accountId,
                        availableBalance,
                        Currency.VND
                );

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(remaining);
        when(statement.getId())
                .thenReturn(statementId);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.FULL_BALANCE);
        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(creditAccountBalancePort.getActiveAccount(accountId))
                .thenReturn(account);

        handler.execute(statementId);

        verify(repaymentTransactionPort).repay(
                accountId,
                facilityId,
                remaining,
                Currency.VND,
                "CREDIT_REPAYMENT-" + statementId,
                "Automatic repayment for credit statement " + statementId
        );
        verify(statement).applyPayment(remaining);
        verify(statement).clearNextRepaymentAttempt();
    }

    @Test
    void shouldRepayMinimumPaymentSuccessfully() {
        BigDecimal remaining = new BigDecimal("500.00");
        BigDecimal minimumPayment = new BigDecimal("100.00");
        BigDecimal paidAmount = new BigDecimal("20.00");
        BigDecimal expectedRepayment = new BigDecimal("80.00");

        CreditAccountBalancePort.AccountBalance account =
                new CreditAccountBalancePort.AccountBalance(
                        accountId,
                        new BigDecimal("1000.00"),
                        Currency.VND
                );

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(remaining);
        when(statement.getMinimumPayment())
                .thenReturn(minimumPayment);
        when(statement.getPaidAmount())
                .thenReturn(paidAmount);
        when(statement.getId())
                .thenReturn(statementId);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.MINIMUM_PAYMENT);
        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(creditAccountBalancePort.getActiveAccount(accountId))
                .thenReturn(account);

        handler.execute(statementId);

        verify(repaymentTransactionPort).repay(
                accountId,
                facilityId,
                expectedRepayment,
                Currency.VND,
                "CREDIT_REPAYMENT-" + statementId,
                "Automatic repayment for credit statement " + statementId
        );
        verify(statement).applyPayment(expectedRepayment);
        verify(statement).clearNextRepaymentAttempt();
    }

    @Test
    void shouldCapMinimumPaymentAtRemainingAmount() {
        BigDecimal remaining = new BigDecimal("60.00");
        BigDecimal minimumPayment = new BigDecimal("200.00");
        BigDecimal paidAmount = new BigDecimal("50.00");

        CreditAccountBalancePort.AccountBalance account =
                new CreditAccountBalancePort.AccountBalance(
                        accountId,
                        new BigDecimal("1000.00"),
                        Currency.VND
                );

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.PARTIALLY_PAID);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(remaining);
        when(statement.getMinimumPayment())
                .thenReturn(minimumPayment);
        when(statement.getPaidAmount())
                .thenReturn(paidAmount);
        when(statement.getId())
                .thenReturn(statementId);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.MINIMUM_PAYMENT);
        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(creditAccountBalancePort.getActiveAccount(accountId))
                .thenReturn(account);

        handler.execute(statementId);

        verify(repaymentTransactionPort).repay(
                accountId,
                facilityId,
                remaining,
                Currency.VND,
                "CREDIT_REPAYMENT-" + statementId,
                "Automatic repayment for credit statement " + statementId
        );
        verify(statement).applyPayment(remaining);
        verify(statement).clearNextRepaymentAttempt();
    }

    @Test
    void shouldScheduleNextAttemptWhenAccountHasInsufficientBalance() {
        BigDecimal remaining = new BigDecimal("500.00");

        CreditAccountBalancePort.AccountBalance account =
                new CreditAccountBalancePort.AccountBalance(
                        accountId,
                        new BigDecimal("100.00"),
                        Currency.VND
                );

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(remaining);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.FULL_BALANCE);
        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(creditAccountBalancePort.getActiveAccount(accountId))
                .thenReturn(account);

        Instant before = Instant.now();

        handler.execute(statementId);

        Instant after = Instant.now();

        verify(statement).scheduleNextRepaymentAttempt(
                argThat(actual ->
                        actual != null
                                && !actual.isBefore(
                                before.plusSeconds(5 * 60 * 60)
                        )
                                && !actual.isAfter(
                                after.plusSeconds(5 * 60 * 60)
                        )
                )
        );

        verify(repaymentTransactionPort, never()).repay(
                any(), any(), any(), any(), anyString(), anyString()
        );
        verify(statement, never()).applyPayment(any());
        verify(statement, never()).clearNextRepaymentAttempt();
    }

    @Test
    void shouldScheduleRetryWhenMinimumPaymentExceedsAvailableBalance() {
        BigDecimal remaining = new BigDecimal("500.00");
        BigDecimal minimumPayment = new BigDecimal("100.00");
        BigDecimal paidAmount = new BigDecimal("20.00");

        CreditAccountBalancePort.AccountBalance account =
                new CreditAccountBalancePort.AccountBalance(
                        accountId,
                        new BigDecimal("50.00"),
                        Currency.VND
                );

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(remaining);
        when(statement.getMinimumPayment())
                .thenReturn(minimumPayment);
        when(statement.getPaidAmount())
                .thenReturn(paidAmount);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.MINIMUM_PAYMENT);
        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(creditAccountBalancePort.getActiveAccount(accountId))
                .thenReturn(account);

        Instant before = Instant.now();

        handler.execute(statementId);

        Instant after = Instant.now();

        verify(statement).scheduleNextRepaymentAttempt(
                argThat(actual ->
                        actual != null
                                && !actual.isBefore(
                                before.plusSeconds(5 * 60 * 60)
                        )
                                && !actual.isAfter(
                                after.plusSeconds(5 * 60 * 60)
                        )
                )
        );

        verify(repaymentTransactionPort, never()).repay(
                any(), any(), any(), any(), anyString(), anyString()
        );
        verify(statement, never()).applyPayment(any());
        verify(statement, never()).clearNextRepaymentAttempt();
    }

    @Test
    void shouldSkipRepaymentWhenRemainingAmountIsZero() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(BigDecimal.ZERO);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.FULL_BALANCE);

        handler.execute(statementId);

        verify(creditRepaymentMandateCommandRepository)
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                );
        verifyNoInteractions(creditAccountBalancePort);
        verifyNoInteractions(repaymentTransactionPort);
        verify(statement, never()).applyPayment(any());
        verify(statement, never()).scheduleNextRepaymentAttempt(any());
        verify(statement, never()).clearNextRepaymentAttempt();
    }

    @Test
    void shouldSkipRepaymentWhenMinimumPaymentAlreadyPaid() {
        BigDecimal remaining = new BigDecimal("500.00");
        BigDecimal minimumPayment = new BigDecimal("100.00");

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(remaining);
        when(statement.getMinimumPayment())
                .thenReturn(minimumPayment);
        when(statement.getPaidAmount())
                .thenReturn(minimumPayment);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.MINIMUM_PAYMENT);

        handler.execute(statementId);

        verify(creditRepaymentMandateCommandRepository)
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                );
        verifyNoInteractions(creditAccountBalancePort);
        verifyNoInteractions(repaymentTransactionPort);
        verify(statement, never()).applyPayment(any());
        verify(statement, never()).scheduleNextRepaymentAttempt(any());
        verify(statement, never()).clearNextRepaymentAttempt();
    }

    @Test
    void shouldThrowWhenStatementDoesNotExist() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(statementId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Credit statement not found: " + statementId);

        verifyNoInteractions(creditRepaymentMandateCommandRepository);
        verifyNoInteractions(creditAccountBalancePort);
        verifyNoInteractions(repaymentTransactionPort);
    }

    @Test
    void shouldThrowWhenStatementStatusIsNotEligible() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.PAID);

        assertThatThrownBy(() -> handler.execute(statementId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Credit statement is not eligible for repayment");

        verifyNoInteractions(creditRepaymentMandateCommandRepository);
        verifyNoInteractions(creditAccountBalancePort);
        verifyNoInteractions(repaymentTransactionPort);
    }

    @Test
    void shouldThrowWhenActiveMandateDoesNotExist() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(statementId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Active repayment mandate not found");

        verifyNoInteractions(creditAccountBalancePort);
        verifyNoInteractions(repaymentTransactionPort);
    }

    @Test
    void shouldPropagateAccountLookupFailure() {
        RuntimeException exception =
                new RuntimeException("Account service unavailable");

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(new BigDecimal("500.00"));

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.FULL_BALANCE);
        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(creditAccountBalancePort.getActiveAccount(accountId))
                .thenThrow(exception);

        org.junit.jupiter.api.Assertions.assertSame(
                exception,
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(statementId)
                )
        );

        verifyNoInteractions(repaymentTransactionPort);
    }

    @Test
    void shouldPropagateRepaymentTransactionFailure() {
        RuntimeException exception =
                new RuntimeException("Repayment transaction failed");

        BigDecimal remaining = new BigDecimal("500.00");

        CreditAccountBalancePort.AccountBalance account =
                new CreditAccountBalancePort.AccountBalance(
                        accountId,
                        new BigDecimal("1000.00"),
                        Currency.VND
                );

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId())
                .thenReturn(facilityId);
        when(statement.getRemainingAmount())
                .thenReturn(remaining);
        when(statement.getId())
                .thenReturn(statementId);

        when(creditRepaymentMandateCommandRepository
                .findByCreditFacilityIdAndStatus(
                        facilityId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(Optional.of(mandate));
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.FULL_BALANCE);
        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(creditAccountBalancePort.getActiveAccount(accountId))
                .thenReturn(account);

        doThrow(exception).when(repaymentTransactionPort).repay(
                accountId,
                facilityId,
                remaining,
                Currency.VND,
                "CREDIT_REPAYMENT-" + statementId,
                "Automatic repayment for credit statement " + statementId
        );

        org.junit.jupiter.api.Assertions.assertSame(
                exception,
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(statementId)
                )
        );

        verify(statement, never()).applyPayment(any());
        verify(statement, never()).clearNextRepaymentAttempt();
    }
}