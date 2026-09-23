package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferCreditCardToProviderCommand;
import com.example.ledgercore.transaction.command.port.outbound.CreditPaymentLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferCreditFacilityToProviderPort;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferCreditCardToProviderHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private TransferCreditFacilityToProviderPort
            transferCreditFacilityToProviderPort;

    @Mock
    private CreditPaymentLedgerPort creditPaymentLedgerPort;

    @Mock
    private TransactionBusinessDayPort transactionBusinessDayPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    private TransferCreditCardToProviderHandler handler;

    private UUID creditFacilityId;
    private UUID providerAccountId;
    private UUID transactionId;

    private BigDecimal amount;
    private Currency currency;
    private LocalDate businessDate;

    private TransferCreditCardToProviderCommand command;

    @BeforeEach
    void setUp() {
        handler = new TransferCreditCardToProviderHandler(
                transactionCommandRepository,
                transferCreditFacilityToProviderPort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );

        creditFacilityId = UUID.randomUUID();
        providerAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        amount = new BigDecimal("150000.00");
        currency = Currency.VND;
        businessDate = LocalDate.of(2026, 9, 24);

        command = new TransferCreditCardToProviderCommand(
                creditFacilityId,
                providerAccountId,
                amount,
                currency,
                "CC-PAY-001",
                "Credit card payment"
        );
    }

    @Test
    void shouldTransferCreditCardToProviderSuccessfully() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        TransactionResponse response =
                handler.execute(command);

        assertNotNull(response);

        assertEquals(transactionId, response.id());
        assertEquals(
                command.reference(),
                response.reference()
        );
        assertEquals(
                TransactionType.CARD_PAYMENT,
                response.type()
        );
        assertEquals(
                TransactionStatus.COMPLETED,
                response.status()
        );
        assertEquals(
                command.providerAccountId(),
                response.destinationAccountId()
        );
        assertEquals(
                command.amount(),
                response.amount()
        );
        assertEquals(
                command.currency(),
                response.currency()
        );
        assertEquals(
                command.description(),
                response.description()
        );
        assertNotNull(response.completedAt());

        verify(transferCreditFacilityToProviderPort)
                .decreaseCreditFacilityOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transferCreditFacilityToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(creditPaymentLedgerPort)
                .recordCreditPayment(
                        transactionId,
                        creditFacilityId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transactionEventPort)
                .publishCreditFacilityBalanceChanged(any());

        verify(transactionEventPort)
                .publishAccountBalanceChanged(any());
    }

    @Test
    void shouldCreateCorrectTransaction() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        handler.execute(command);

        verify(transactionCommandRepository)
                .save(captor.capture());

        MoneyTransaction transaction =
                captor.getValue();

        assertEquals(
                transactionId,
                transaction.getId()
        );
        assertEquals(
                command.reference(),
                transaction.getReference()
        );
        assertEquals(
                TransactionType.CARD_PAYMENT,
                transaction.getType()
        );
        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );
        assertEquals(
                businessDate,
                transaction.getBusinessDate()
        );
        assertEquals(
                creditFacilityId,
                transaction.getSourceCreditFacilityId()
        );
        assertEquals(
                providerAccountId,
                transaction.getDestinationAccountId()
        );
        assertEquals(
                amount,
                transaction.getAmount()
        );
        assertEquals(
                currency,
                transaction.getCurrency()
        );
        assertEquals(
                command.description(),
                transaction.getDescription()
        );
        assertNotNull(transaction.getCompletedAt());
    }

    @Test
    void shouldExecuteOperationsInCorrectOrder() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        InOrder inOrder = inOrder(
                transactionBusinessDayPort,
                transactionCommandRepository,
                transferCreditFacilityToProviderPort,
                creditPaymentLedgerPort,
                transactionEventPort
        );

        handler.execute(command);

        inOrder.verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        inOrder.verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        inOrder.verify(transferCreditFacilityToProviderPort)
                .decreaseCreditFacilityOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        inOrder.verify(transferCreditFacilityToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        inOrder.verify(creditPaymentLedgerPort)
                .recordCreditPayment(
                        transactionId,
                        creditFacilityId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        inOrder.verify(transactionEventPort)
                .publishCreditFacilityBalanceChanged(any());

        inOrder.verify(transactionEventPort)
                .publishAccountBalanceChanged(any());
    }

    @Test
    void shouldPublishNegativeCreditFacilityBalanceDelta() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        ArgumentCaptor<CreditFacilityBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        CreditFacilityBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(transactionEventPort)
                .publishCreditFacilityBalanceChanged(
                        captor.capture()
                );

        CreditFacilityBalanceChangedEvent event =
                captor.getValue();

        assertEquals(
                transactionId,
                event.transactionId()
        );
        assertEquals(
                creditFacilityId,
                event.creditFacilityId()
        );
        assertEquals(
                amount.negate(),
                event.balanceDelta()
        );
        assertEquals(
                currency,
                event.currency()
        );
        assertNotNull(event.changedAt());
    }

    @Test
    void shouldPublishPositiveProviderAccountBalanceDelta() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        AccountBalanceChangedEvent event =
                captor.getValue();

        assertEquals(
                transactionId,
                event.transactionId()
        );
        assertEquals(
                providerAccountId,
                event.accountId()
        );
        assertEquals(
                amount,
                event.balanceDelta()
        );
        assertEquals(
                currency,
                event.currency()
        );
        assertNotNull(event.changedAt());
    }

    @Test
    void shouldCompleteTransactionBeforePublishingEvents() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        ArgumentCaptor<CreditFacilityBalanceChangedEvent>
                creditEventCaptor =
                ArgumentCaptor.forClass(
                        CreditFacilityBalanceChangedEvent.class
                );

        ArgumentCaptor<AccountBalanceChangedEvent>
                accountEventCaptor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        verify(transactionEventPort)
                .publishCreditFacilityBalanceChanged(
                        creditEventCaptor.capture()
                );

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        accountEventCaptor.capture()
                );

        MoneyTransaction transaction =
                transactionCaptor.getValue();

        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        assertNotNull(
                transaction.getCompletedAt()
        );

        assertEquals(
                transaction.getCompletedAt(),
                creditEventCaptor.getValue().changedAt()
        );

        assertEquals(
                transaction.getCompletedAt(),
                accountEventCaptor.getValue().changedAt()
        );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        assertInvalidRequest(null);
    }

    @Test
    void shouldThrowWhenCreditFacilityIdIsNull() {
        TransferCreditCardToProviderCommand invalidCommand =
                new TransferCreditCardToProviderCommand(
                        null,
                        command.providerAccountId(),
                        command.amount(),
                        command.currency(),
                        command.reference(),
                        command.description()
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenProviderAccountIdIsNull() {
        TransferCreditCardToProviderCommand invalidCommand =
                new TransferCreditCardToProviderCommand(
                        command.creditFacilityId(),
                        null,
                        command.amount(),
                        command.currency(),
                        command.reference(),
                        command.description()
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        TransferCreditCardToProviderCommand invalidCommand =
                new TransferCreditCardToProviderCommand(
                        command.creditFacilityId(),
                        command.providerAccountId(),
                        null,
                        command.currency(),
                        command.reference(),
                        command.description()
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        TransferCreditCardToProviderCommand invalidCommand =
                new TransferCreditCardToProviderCommand(

                        command.creditFacilityId(),
                        command.providerAccountId(),
                        command.amount(),
                        null,
                        command.reference(),
                        command.description()
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        TransferCreditCardToProviderCommand invalidCommand =
                new TransferCreditCardToProviderCommand(
                        command.creditFacilityId(),
                        command.providerAccountId(),
                        BigDecimal.ZERO,
                        command.currency(),
                        command.reference(),
                        command.description()
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                transferCreditFacilityToProviderPort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        TransferCreditCardToProviderCommand invalidCommand =
                new TransferCreditCardToProviderCommand(
                        command.creditFacilityId(),
                        command.providerAccountId(),
                        new BigDecimal("-1.00"),
                        command.currency(),
                        command.reference(),
                        command.description()
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                transferCreditFacilityToProviderPort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldThrowWhenAmountHasInvalidScaleForCurrency() {
        TransferCreditCardToProviderCommand invalidCommand =
                new TransferCreditCardToProviderCommand(
                        command.creditFacilityId(),
                        command.providerAccountId(),
                        new BigDecimal("150000.001"),
                        command.currency(),
                        command.reference(),
                        command.description()
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        verifyNoInteractions(
                transactionCommandRepository,
                transferCreditFacilityToProviderPort,
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

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, thrown);

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        verifyNoInteractions(
                transactionCommandRepository,
                transferCreditFacilityToProviderPort,
                creditPaymentLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateSaveFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        RuntimeException exception =
                new RuntimeException("Save failed");

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenThrow(exception);

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, thrown);

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verifyNoInteractions(
                transferCreditFacilityToProviderPort,
                creditPaymentLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateDecreaseCreditFacilityFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        RuntimeException exception =
                new RuntimeException("Decrease facility failed");

        doThrow(exception)
                .when(transferCreditFacilityToProviderPort)
                .decreaseCreditFacilityOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, thrown);

        verify(transferCreditFacilityToProviderPort)
                .decreaseCreditFacilityOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        verify(
                transferCreditFacilityToProviderPort,
                never()
        ).increaseProviderAccount(
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                creditPaymentLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateIncreaseProviderAccountFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        RuntimeException exception =
                new RuntimeException("Increase provider failed");

        doThrow(exception)
                .when(transferCreditFacilityToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, thrown);

        verify(transferCreditFacilityToProviderPort)
                .decreaseCreditFacilityOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transferCreditFacilityToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
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
    void shouldPropagateLedgerFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        RuntimeException exception =
                new RuntimeException("Ledger failed");

        doThrow(exception)
                .when(creditPaymentLedgerPort)
                .recordCreditPayment(
                        transactionId,
                        creditFacilityId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, thrown);

        verify(transferCreditFacilityToProviderPort)
                .decreaseCreditFacilityOutstandingBalance(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transferCreditFacilityToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(creditPaymentLedgerPort)
                .recordCreditPayment(
                        transactionId,
                        creditFacilityId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldPropagateCreditFacilityEventFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        RuntimeException exception =
                new RuntimeException("Credit facility event failed");

        doThrow(exception)
                .when(transactionEventPort)
                .publishCreditFacilityBalanceChanged(any());

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, thrown);

        verify(transactionEventPort)
                .publishCreditFacilityBalanceChanged(any());

        verify(
                transactionEventPort,
                never()
        ).publishAccountBalanceChanged(any());
    }

    @Test
    void shouldPropagateProviderAccountEventFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        stubSaveTransaction();

        RuntimeException exception =
                new RuntimeException("Account event failed");

        doThrow(exception)
                .when(transactionEventPort)
                .publishAccountBalanceChanged(any());

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, thrown);

        verify(transactionEventPort)
                .publishCreditFacilityBalanceChanged(any());

        verify(transactionEventPort)
                .publishAccountBalanceChanged(any());
    }

    private void stubSaveTransaction() {
        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> {
                    MoneyTransaction transaction =
                            invocation.getArgument(0);

                    transaction.setId(transactionId);

                    return transaction;
                });
    }

    private void assertInvalidRequest(
            TransferCreditCardToProviderCommand invalidCommand
    ) {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                transferCreditFacilityToProviderPort,
                creditPaymentLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }
}