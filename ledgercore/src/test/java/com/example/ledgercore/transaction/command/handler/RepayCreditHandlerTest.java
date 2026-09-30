
package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.RepayCreditCommand;
import com.example.ledgercore.transaction.command.port.outbound.CreditFacilityBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.CreditPaymentLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.RepayCreditFacilityAccountBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;
import com.example.ledgercore.transaction.event.CreditFacilityBalanceChangedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepayCreditHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private RepayCreditFacilityAccountBalancePort
            repayCreditFacilityAccountBalancePort;

    @Mock
    private CreditFacilityBalancePort creditFacilityBalancePort;

    @Mock
    private CreditPaymentLedgerPort creditPaymentLedgerPort;

    @Mock
    private TransactionBusinessDayPort transactionBusinessDayPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    private RepayCreditHandler handler;

    private UUID accountId;
    private UUID creditFacilityId;
    private String reference;
    private String description;
    private LocalDate businessDate;
    private BigDecimal amount;
    private Currency currency;
    private RepayCreditCommand command;

    @BeforeEach
    void setUp() {
        handler = new RepayCreditHandler(
                transactionCommandRepository,
                repayCreditFacilityAccountBalancePort,
                creditFacilityBalancePort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );

        accountId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        reference = "REPAY-" + UUID.randomUUID();
        description = "Credit facility repayment";
        businessDate = LocalDate.of(2026, 9, 28);
        amount = new BigDecimal("100000");
        currency = Currency.VND;

        command = validCommand();
    }

    @Test
    void shouldRepayCreditFacilitySuccessfully() {
        givenBusinessDate();

        TransactionResponse response = handler.execute(command);

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        MoneyTransaction savedTransaction =
                transactionCaptor.getValue();

        assertThat(savedTransaction.getReference())
                .isEqualTo(reference);
        assertThat(savedTransaction.getType())
                .isEqualTo(TransactionType.CREDIT_PAYMENT);
        assertThat(savedTransaction.getStatus())
                .isEqualTo(TransactionStatus.COMPLETED);
        assertThat(savedTransaction.getBusinessDate())
                .isEqualTo(businessDate);
        assertThat(savedTransaction.getSourceAccountId())
                .isEqualTo(accountId);
        assertThat(savedTransaction.getDestinationCreditFacilityId())
                .isEqualTo(creditFacilityId);
        assertThat(savedTransaction.getAmount())
                .isEqualByComparingTo(amount);
        assertThat(savedTransaction.getCurrency())
                .isEqualTo(currency);
        assertThat(savedTransaction.getDescription())
                .isEqualTo(description);
        assertThat(savedTransaction.getCompletedAt())
                .isNotNull();

        verify(repayCreditFacilityAccountBalancePort)
                .decreaseBalance(
                        accountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(creditFacilityBalancePort)
                .decreaseOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        verify(creditPaymentLedgerPort)
                .recordCreditPayment(
                        savedTransaction.getId(),
                        creditFacilityId,
                        accountId,
                        amount,
                        currency,
                        businessDate
                );

        ArgumentCaptor<AccountBalanceChangedEvent> accountEventCaptor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        ArgumentCaptor<CreditFacilityBalanceChangedEvent>
                facilityEventCaptor =
                ArgumentCaptor.forClass(
                        CreditFacilityBalanceChangedEvent.class
                );

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        accountEventCaptor.capture()
                );

        verify(transactionEventPort)
                .publishCreditFacilityBalanceChanged(
                        facilityEventCaptor.capture()
                );

        AccountBalanceChangedEvent accountEvent =
                accountEventCaptor.getValue();

        assertThat(accountEvent.transactionId())
                .isEqualTo(savedTransaction.getId());
        assertThat(accountEvent.accountId())
                .isEqualTo(accountId);
        assertThat(accountEvent.currency())
                .isEqualTo(currency);

        CreditFacilityBalanceChangedEvent facilityEvent =
                facilityEventCaptor.getValue();

        assertThat(facilityEvent.transactionId())
                .isEqualTo(savedTransaction.getId());
        assertThat(facilityEvent.creditFacilityId())
                .isEqualTo(creditFacilityId);
        assertThat(facilityEvent.currency())
                .isEqualTo(currency);

        assertThat(response).isNotNull();
        assertThat(response.id())
                .isEqualTo(savedTransaction.getId());
        assertThat(response.reference())
                .isEqualTo(reference);
        assertThat(response.type())
                .isEqualTo(TransactionType.CREDIT_PAYMENT);
        assertThat(response.status())
                .isEqualTo(TransactionStatus.COMPLETED);
        assertThat(response.sourceAccountId())
                .isEqualTo(accountId);
        assertThat(response.amount())
                .isEqualByComparingTo(amount);
        assertThat(response.currency())
                .isEqualTo(currency);
        assertThat(response.description())
                .isEqualTo(description);
        assertThat(response.completedAt())
                .isEqualTo(savedTransaction.getCompletedAt());

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        verifyNoMoreInteractions(
                transactionCommandRepository,
                repayCreditFacilityAccountBalancePort,
                creditFacilityBalancePort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldRejectNullCommand() {
        assertInvalidCommand(
                null,
                ErrorCode.INVALID_REQUEST
        );
    }

    @Test
    void shouldRejectNullAccountId() {
        assertInvalidCommand(
                new RepayCreditCommand(
                        null,
                        creditFacilityId,
                        amount,
                        currency,
                        reference,
                        description
                ),
                ErrorCode.INVALID_REQUEST
        );
    }

    @Test
    void shouldRejectNullCreditFacilityId() {
        assertInvalidCommand(
                new RepayCreditCommand(
                        accountId,
                        null,
                        amount,
                        currency,
                        reference,
                        description
                ),
                ErrorCode.INVALID_REQUEST
        );
    }

    @Test
    void shouldRejectNullAmount() {
        assertInvalidCommand(
                new RepayCreditCommand(
                        accountId,
                        creditFacilityId,
                        null,
                        currency,
                        reference,
                        description
                ),
                ErrorCode.INVALID_REQUEST
        );
    }

    @Test
    void shouldRejectNullCurrency() {
        assertInvalidCommand(
                new RepayCreditCommand(
                        accountId,
                        creditFacilityId,
                        amount,
                        null,
                        reference,
                        description
                ),
                ErrorCode.INVALID_REQUEST
        );
    }

    @Test
    void shouldRejectNullReference() {
        assertInvalidCommand(
                new RepayCreditCommand(
                        accountId,
                        creditFacilityId,
                        amount,
                        currency,
                        null,
                        description
                ),
                ErrorCode.INVALID_REQUEST
        );
    }

    @Test
    void shouldRejectBlankReference() {
        assertInvalidCommand(
                new RepayCreditCommand(
                        accountId,
                        creditFacilityId,
                        amount,
                        currency,
                        "   ",
                        description
                ),
                ErrorCode.INVALID_REQUEST
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        assertInvalidCommand(
                commandWithAmount(BigDecimal.ZERO),
                ErrorCode.INVALID_AMOUNT
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertInvalidCommand(
                commandWithAmount(new BigDecimal("-100")),
                ErrorCode.INVALID_AMOUNT
        );
    }

    @Test
    void shouldRejectAmountWithInvalidCurrencyPrecision() {
        RepayCreditCommand invalidCommand =
                commandWithAmount(new BigDecimal("100.50"));

        assertThatThrownBy(() -> handler.execute(invalidCommand))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(
                transactionCommandRepository,
                repayCreditFacilityAccountBalancePort,
                creditFacilityBalancePort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateBusinessDateFailure() {
        RuntimeException exception =
                new RuntimeException("Business date unavailable");

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenThrow(exception);

        assertThatThrownBy(() -> handler.execute(command))
                .isSameAs(exception);

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        verifyNoInteractions(
                transactionCommandRepository,
                repayCreditFacilityAccountBalancePort,
                creditFacilityBalancePort,
                creditPaymentLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldStopWhenSavingTransactionFails() {
        givenBusinessDate();

        RuntimeException exception =
                new RuntimeException("Transaction save failed");

        doThrow(exception)
                .when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        assertThatThrownBy(() -> handler.execute(command))
                .isSameAs(exception);

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();
        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verifyNoInteractions(
                repayCreditFacilityAccountBalancePort,
                creditFacilityBalancePort,
                creditPaymentLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldStopWhenAccountBalanceUpdateFails() {
        givenBusinessDate();

        RuntimeException exception =
                new RuntimeException("Account balance update failed");

        doThrow(exception)
                .when(repayCreditFacilityAccountBalancePort)
                .decreaseBalance(
                        accountId,
                        amount,
                        currency,
                        businessDate
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isSameAs(exception);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(repayCreditFacilityAccountBalancePort)
                .decreaseBalance(
                        accountId,
                        amount,
                        currency,
                        businessDate
                );

        verifyNoInteractions(
                creditFacilityBalancePort,
                creditPaymentLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldStopWhenCreditFacilityBalanceUpdateFails() {
        givenBusinessDate();

        RuntimeException exception =
                new RuntimeException("Credit facility update failed");

        doThrow(exception)
                .when(creditFacilityBalancePort)
                .decreaseOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isSameAs(exception);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(repayCreditFacilityAccountBalancePort)
                .decreaseBalance(
                        accountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(creditFacilityBalancePort)
                .decreaseOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        verifyNoInteractions(
                creditPaymentLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldStopWhenCreditPaymentLedgerFails() {
        givenBusinessDate();

        RuntimeException exception =
                new RuntimeException("Ledger posting failed");

        doThrow(exception)
                .when(creditPaymentLedgerPort)
                .recordCreditPayment(
                        any(),
                        eq(creditFacilityId),
                        eq(accountId),
                        eq(amount),
                        eq(currency),
                        eq(businessDate)
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isSameAs(exception);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(repayCreditFacilityAccountBalancePort)
                .decreaseBalance(
                        accountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(creditFacilityBalancePort)
                .decreaseOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        verify(creditPaymentLedgerPort)
                .recordCreditPayment(
                        any(),
                        eq(creditFacilityId),
                        eq(accountId),
                        eq(amount),
                        eq(currency),
                        eq(businessDate)
                );

        verifyNoInteractions(transactionEventPort);
    }

    private void givenBusinessDate() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);
    }

    private RepayCreditCommand validCommand() {
        return new RepayCreditCommand(
                accountId,
                creditFacilityId,
                amount,
                currency,
                reference,
                description
        );
    }

    private RepayCreditCommand commandWithAmount(
            BigDecimal newAmount
    ) {
        return new RepayCreditCommand(
                accountId,
                creditFacilityId,
                newAmount,
                currency,
                reference,
                description
        );
    }

    private void assertInvalidCommand(
            RepayCreditCommand invalidCommand,
            ErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(() -> handler.execute(invalidCommand))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(expectedErrorCode);

        verifyNoInteractions(
                transactionCommandRepository,
                repayCreditFacilityAccountBalancePort,
                creditFacilityBalancePort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }
}