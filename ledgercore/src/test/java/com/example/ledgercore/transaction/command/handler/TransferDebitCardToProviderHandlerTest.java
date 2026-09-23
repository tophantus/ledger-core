package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferDebitCardToProviderCommand;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferAccountToProviderPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferLedgerPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferDebitCardToProviderHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private TransferAccountToProviderPort
            transferAccountToProviderPort;

    @Mock
    private TransferLedgerPort transferLedgerPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    @Mock
    private TransactionBusinessDayPort transactionBusinessDayPort;

    private TransferDebitCardToProviderHandler handler;

    private UUID sourceAccountId;
    private UUID providerAccountId;
    private UUID transactionId;

    private BigDecimal amount;
    private Currency currency;
    private LocalDate businessDate;

    private TransferDebitCardToProviderCommand command;

    @BeforeEach
    void setUp() {
        handler = new TransferDebitCardToProviderHandler(
                transactionCommandRepository,
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        sourceAccountId = UUID.randomUUID();
        providerAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        amount = new BigDecimal("150000.00");
        currency = Currency.VND;
        businessDate = LocalDate.of(2026, 9, 24);

        command = new TransferDebitCardToProviderCommand(
                sourceAccountId,
                providerAccountId,
                amount,
                currency,
                "DC-PAY-001",
                "Debit card payment"
        );
    }

    @Test
    void shouldTransferDebitCardToProviderSuccessfully() {
        mockSuccessfulTransaction();

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
                sourceAccountId,
                response.sourceAccountId()
        );
        assertEquals(
                providerAccountId,
                response.destinationAccountId()
        );
        assertEquals(amount, response.amount());
        assertEquals(currency, response.currency());
        assertEquals(
                command.description(),
                response.description()
        );
        assertNull(response.incoming());
        assertNotNull(response.completedAt());

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(transferAccountToProviderPort)
                .decreaseSourceAccount(
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transferAccountToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );
    }

    @Test
    void shouldCreateCorrectTransaction() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> {
                    MoneyTransaction transaction =
                            invocation.getArgument(0);

                    transaction.setId(transactionId);

                    return transaction;
                });

        handler.execute(command);

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(captor.capture());

        MoneyTransaction transaction =
                captor.getValue();

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
                sourceAccountId,
                transaction.getSourceAccountId()
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
    }

    @Test
    void shouldExecuteOperationsInCorrectOrder() {
        mockSuccessfulTransaction();

        InOrder inOrder = inOrder(
                transactionBusinessDayPort,
                transactionCommandRepository,
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort
        );

        handler.execute(command);

        inOrder.verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        inOrder.verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        inOrder.verify(transferAccountToProviderPort)
                .decreaseSourceAccount(
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        inOrder.verify(transferAccountToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        inOrder.verify(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        inOrder.verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );
    }

    @Test
    void shouldPublishNegativeBalanceDeltaForSourceAccount() {
        mockSuccessfulTransaction();

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        AccountBalanceChangedEvent sourceEvent =
                captor.getAllValues()
                        .get(0);

        assertEquals(
                transactionId,
                sourceEvent.transactionId()
        );
        assertEquals(
                sourceAccountId,
                sourceEvent.accountId()
        );
        assertEquals(
                amount.negate(),
                sourceEvent.balanceDelta()
        );
        assertEquals(
                currency,
                sourceEvent.currency()
        );
        assertNotNull(sourceEvent.changedAt());
    }

    @Test
    void shouldPublishPositiveBalanceDeltaForProviderAccount() {
        mockSuccessfulTransaction();

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        AccountBalanceChangedEvent providerEvent =
                captor.getAllValues()
                        .get(1);

        assertEquals(
                transactionId,
                providerEvent.transactionId()
        );
        assertEquals(
                providerAccountId,
                providerEvent.accountId()
        );
        assertEquals(
                amount,
                providerEvent.balanceDelta()
        );
        assertEquals(
                currency,
                providerEvent.currency()
        );
        assertNotNull(providerEvent.changedAt());
    }

    @Test
    void shouldPublishSourceEventBeforeProviderEvent() {
        mockSuccessfulTransaction();

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        var events = captor.getAllValues();

        assertEquals(
                sourceAccountId,
                events.get(0).accountId()
        );
        assertEquals(
                amount.negate(),
                events.get(0).balanceDelta()
        );

        assertEquals(
                providerAccountId,
                events.get(1).accountId()
        );
        assertEquals(
                amount,
                events.get(1).balanceDelta()
        );
    }

    @Test
    void shouldCompleteTransactionBeforePublishingEvents() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> {
                    MoneyTransaction transaction =
                            invocation.getArgument(0);

                    transaction.setId(transactionId);

                    return transaction;
                });

        handler.execute(command);

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        assertEquals(
                TransactionStatus.COMPLETED,
                getSavedTransaction().getStatus()
        );

        assertNotNull(
                getSavedTransaction().getCompletedAt()
        );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldThrowWhenSourceAccountIdIsNull() {
        TransferDebitCardToProviderCommand invalidCommand =
                new TransferDebitCardToProviderCommand(
                        null,
                        providerAccountId,
                        amount,
                        currency,
                        "DC-PAY-001",
                        "Debit card payment"
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenProviderAccountIdIsNull() {
        TransferDebitCardToProviderCommand invalidCommand =
                new TransferDebitCardToProviderCommand(
                        sourceAccountId,
                        null,
                        amount,
                        currency,
                        "DC-PAY-001",
                        "Debit card payment"
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        TransferDebitCardToProviderCommand invalidCommand =
                new TransferDebitCardToProviderCommand(
                        sourceAccountId,
                        providerAccountId,
                        null,
                        currency,
                        "DC-PAY-001",
                        "Debit card payment"
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        TransferDebitCardToProviderCommand invalidCommand =
                new TransferDebitCardToProviderCommand(
                        sourceAccountId,
                        providerAccountId,
                        amount,
                        null,
                        "DC-PAY-001",
                        "Debit card payment"
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void shouldThrowWhenSourceAndProviderAccountsAreSame() {
        TransferDebitCardToProviderCommand invalidCommand =
                new TransferDebitCardToProviderCommand(
                        sourceAccountId,
                        sourceAccountId,
                        amount,
                        currency,
                        "DC-PAY-001",
                        "Debit card payment"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        TransferDebitCardToProviderCommand invalidCommand =
                new TransferDebitCardToProviderCommand(
                        sourceAccountId,
                        providerAccountId,
                        BigDecimal.ZERO,
                        currency,
                        "DC-PAY-001",
                        "Debit card payment"
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
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        TransferDebitCardToProviderCommand invalidCommand =
                new TransferDebitCardToProviderCommand(
                        sourceAccountId,
                        providerAccountId,
                        new BigDecimal("-1.00"),
                        currency,
                        "DC-PAY-001",
                        "Debit card payment"
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
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldPropagateBusinessDateFailure() {
        RuntimeException exception =
                new RuntimeException("Business date unavailable");

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenThrow(exception);

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        verifyNoInteractions(
                transactionCommandRepository,
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateTransactionSaveFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        RuntimeException exception =
                new RuntimeException("Transaction save failed");

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenThrow(exception);

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verifyNoInteractions(
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateSourceAccountDecreaseFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException(
                        "Source account decrease failed"
                );

        doThrow(exception)
                .when(transferAccountToProviderPort)
                .decreaseSourceAccount(
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(transferAccountToProviderPort)
                .decreaseSourceAccount(
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(
                transferAccountToProviderPort,
                never()
        ).increaseProviderAccount(
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateProviderAccountIncreaseFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException(
                        "Provider account increase failed"
                );

        doThrow(exception)
                .when(transferAccountToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(transferAccountToProviderPort)
                .decreaseSourceAccount(
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transferAccountToProviderPort)
                .increaseProviderAccount(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verifyNoInteractions(
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateLedgerFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException("Ledger failed");

        doThrow(exception)
                .when(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldPropagateSourceBalanceEventFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException(
                        "Source balance event failed"
                );

        doThrow(exception)
                .when(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );
    }

    @Test
    void shouldPropagateProviderBalanceEventFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException(
                        "Provider balance event failed"
                );

        doAnswer(invocation -> {
            AccountBalanceChangedEvent event =
                    invocation.getArgument(0);

            if (event.accountId().equals(providerAccountId)) {
                throw exception;
            }

            return null;
        }).when(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );
    }

    private void mockSuccessfulTransaction() {
        mockTransactionSave();
    }

    private void mockTransactionSave() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> {
                    MoneyTransaction transaction =
                            invocation.getArgument(0);

                    transaction.setId(transactionId);

                    return transaction;
                });
    }

    private MoneyTransaction getSavedTransaction() {
        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(captor.capture());

        return captor.getValue();
    }

    private void assertInvalidRequest(
            TransferDebitCardToProviderCommand invalidCommand
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
                transferAccountToProviderPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }
}