package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferUserAccountPort;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferMoneyHandlerTest {

    @Mock
    private TransactionCommandRepository
            transactionCommandRepository;

    @Mock
    private TransferUserAccountPort
            transferUserAccountPort;

    @Mock
    private TransferLedgerPort
            transferLedgerPort;

    @Mock
    private TransactionEventPort
            transactionEventPort;

    @Mock
    private TransactionBusinessDayPort
            transactionBusinessDayPort;

    private TransferMoneyHandler handler;

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 9, 4);

    private static final BigDecimal TRANSFER_AMOUNT =
            new BigDecimal("100.00");

    private static final String REFERENCE =
            "REF-001";

    private static final String DESCRIPTION =
            "Test transfer";

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        handler =
                new TransferMoneyHandler(
                        transactionCommandRepository,
                        transferUserAccountPort,
                        transferLedgerPort,
                        transactionEventPort,
                        transactionBusinessDayPort
                );
    }

    @Test
    void shouldTransferMoneySuccessfully() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        TransactionResponse response =
                handler.execute(command);

        assertEquals(
                transactionId,
                response.id()
        );

        assertEquals(
                REFERENCE,
                response.reference()
        );

        assertEquals(
                TransactionType.TRANSFER,
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
                destinationAccountId,
                response.destinationAccountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                response.amount()
        );

        assertEquals(
                Currency.VND,
                response.currency()
        );

        assertEquals(
                DESCRIPTION,
                response.description()
        );

        assertNull(
                response.incoming()
        );

        assertNotNull(
                response.completedAt()
        );

        verify(
                transactionCommandRepository
        ).save(any(MoneyTransaction.class));

        verify(
                transferUserAccountPort
        ).transfer(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                BUSINESS_DATE
        );

        verify(
                transferLedgerPort
        ).recordTransfer(
                transactionId,
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                Currency.VND,
                BUSINESS_DATE
        );

        verify(
                transactionEventPort,
                times(2)
        ).publishAccountBalanceChanged(
                any(AccountBalanceChangedEvent.class)
        );
    }

    @Test
    void shouldCreateTransactionCorrectly() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        handler.execute(command);

        verify(
                transactionCommandRepository
        ).save(
                captor.capture()
        );

        MoneyTransaction transaction =
                captor.getValue();

        assertEquals(
                transactionId,
                transaction.getId()
        );

        assertEquals(
                REFERENCE,
                transaction.getReference()
        );

        assertEquals(
                TransactionType.TRANSFER,
                transaction.getType()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        assertEquals(
                BUSINESS_DATE,
                transaction.getBusinessDate()
        );

        assertEquals(
                sourceAccountId,
                transaction.getSourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                transaction.getDestinationAccountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                transaction.getAmount()
        );

        assertEquals(
                Currency.VND,
                transaction.getCurrency()
        );

        assertEquals(
                DESCRIPTION,
                transaction.getDescription()
        );

        assertNotNull(
                transaction.getCompletedAt()
        );
    }

    @Test
    void shouldGetBusinessDateBeforeSavingTransaction() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        InOrder inOrder =
                inOrder(
                        transactionBusinessDayPort,
                        transactionCommandRepository
                );

        handler.execute(command);

        inOrder.verify(
                transactionBusinessDayPort
        ).getCurrentBusinessDate();

        inOrder.verify(
                transactionCommandRepository
        ).save(
                any(MoneyTransaction.class)
        );
    }

    @Test
    void shouldTransferAccountAfterSavingTransaction() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        InOrder inOrder =
                inOrder(
                        transactionCommandRepository,
                        transferUserAccountPort
                );

        handler.execute(command);

        inOrder.verify(
                transactionCommandRepository
        ).save(
                any(MoneyTransaction.class)
        );

        inOrder.verify(
                transferUserAccountPort
        ).transfer(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                BUSINESS_DATE
        );
    }

    @Test
    void shouldRecordLedgerAfterAccountTransfer() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        InOrder inOrder =
                inOrder(
                        transferUserAccountPort,
                        transferLedgerPort
                );

        handler.execute(command);

        inOrder.verify(
                transferUserAccountPort
        ).transfer(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                BUSINESS_DATE
        );

        inOrder.verify(
                transferLedgerPort
        ).recordTransfer(
                transactionId,
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                Currency.VND,
                BUSINESS_DATE
        );
    }

    @Test
    void shouldPublishSourceBalanceDecreaseEvent() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(
                transactionEventPort,
                times(2)
        ).publishAccountBalanceChanged(
                captor.capture()
        );

        AccountBalanceChangedEvent sourceEvent =
                captor.getAllValues().getFirst();

        assertEquals(
                transactionId,
                sourceEvent.transactionId()
        );

        assertEquals(
                sourceAccountId,
                sourceEvent.accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT.negate(),
                sourceEvent.balanceDelta()
        );

        assertEquals(
                Currency.VND,
                sourceEvent.currency()
        );

        assertNotNull(
                sourceEvent.changedAt()
        );
    }

    @Test
    void shouldPublishDestinationBalanceIncreaseEvent() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(
                transactionEventPort,
                times(2)
        ).publishAccountBalanceChanged(
                captor.capture()
        );

        AccountBalanceChangedEvent destinationEvent =
                captor.getAllValues().get(1);

        assertEquals(
                transactionId,
                destinationEvent.transactionId()
        );

        assertEquals(
                destinationAccountId,
                destinationEvent.accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                destinationEvent.balanceDelta()
        );

        assertEquals(
                Currency.VND,
                destinationEvent.currency()
        );

        assertNotNull(
                destinationEvent.changedAt()
        );
    }

    @Test
    void shouldPublishSourceEventBeforeDestinationEvent() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(
                transactionEventPort,
                times(2)
        ).publishAccountBalanceChanged(
                captor.capture()
        );

        var events =
                captor.getAllValues();

        assertEquals(
                sourceAccountId,
                events.get(0).accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT.negate(),
                events.get(0).balanceDelta()
        );

        assertEquals(
                destinationAccountId,
                events.get(1).accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                events.get(1).balanceDelta()
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

        verifyNoInteractionsWithDependencies();
    }

    @Test
    void shouldThrowWhenSourceAccountIdIsNull() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        null,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenDestinationAccountIdIsNull() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        null,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        destinationAccountId,
                        null,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        null,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenReferenceIsNull() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        null,
                        DESCRIPTION
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenReferenceIsBlank() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        "   ",
                        DESCRIPTION
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenSourceAndDestinationAreSame() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        sourceAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                exception.getErrorCode()
        );

        verifyNoInteractionsWithDependencies();
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        destinationAccountId,
                        BigDecimal.ZERO,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractionsWithDependencies();
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("-1.00"),
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractionsWithDependencies();
    }

    @Test
    void shouldThrowWhenAmountScaleExceedsCurrencyScale() {
        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.001"),
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_CURRENCY_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractionsWithDependencies();
    }

    @Test
    void shouldThrowWhenBusinessDateCannotBeRetrieved() {
        TransferMoneyCommand command =
                createValidCommand();

        RuntimeException exception =
                new RuntimeException(
                        "Business date unavailable"
                );

        when(
                transactionBusinessDayPort
                        .getCurrentBusinessDate()
        ).thenThrow(exception);

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transactionBusinessDayPort
        ).getCurrentBusinessDate();

        verify(
                transactionCommandRepository,
                never()
        ).save(any(MoneyTransaction.class));

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateTransactionSaveFailure() {
        TransferMoneyCommand command =
                createValidCommand();

        mockBusinessDate();

        RuntimeException exception =
                new RuntimeException(
                        "Transaction save failed"
                );

        doThrow(exception)
                .when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transactionCommandRepository
        ).save(any(MoneyTransaction.class));

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateAccountTransferFailure() {
        TransferMoneyCommand command =
                createValidCommand();

        mockBusinessDate();
        mockSaveTransaction();

        RuntimeException exception =
                new RuntimeException(
                        "Account transfer failed"
                );

        doThrow(exception)
                .when(transferUserAccountPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        BUSINESS_DATE
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transferUserAccountPort
        ).transfer(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                BUSINESS_DATE
        );

        verify(
                transferLedgerPort,
                never()
        ).recordTransfer(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateLedgerFailure() {
        TransferMoneyCommand command =
                createValidCommand();

        mockBusinessDate();
        mockSaveTransaction();

        RuntimeException exception =
                new RuntimeException(
                        "Ledger recording failed"
                );

        doThrow(exception)
                .when(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        BUSINESS_DATE
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transferUserAccountPort
        ).transfer(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                BUSINESS_DATE
        );

        verify(
                transferLedgerPort
        ).recordTransfer(
                transactionId,
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                Currency.VND,
                BUSINESS_DATE
        );

        verify(
                transactionEventPort,
                never()
        ).publishAccountBalanceChanged(
                any(AccountBalanceChangedEvent.class)
        );
    }

    @Test
    void shouldPropagateSourceBalanceEventFailure() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        RuntimeException exception =
                new RuntimeException(
                        "Source balance event failed"
                );

        doAnswer(invocation -> {
            AccountBalanceChangedEvent event =
                    invocation.getArgument(0);

            if (sourceAccountId.equals(event.accountId())) {
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

        assertSame(
                exception,
                actual
        );

        verify(
                transactionEventPort
        ).publishAccountBalanceChanged(
                any(AccountBalanceChangedEvent.class)
        );
    }

    @Test
    void shouldPropagateDestinationBalanceEventFailure() {
        TransferMoneyCommand command =
                createValidCommand();

        mockSuccessfulTransfer();

        RuntimeException exception =
                new RuntimeException(
                        "Destination balance event failed"
                );

        doAnswer(invocation -> {
            AccountBalanceChangedEvent event =
                    invocation.getArgument(0);

            if (destinationAccountId.equals(event.accountId())) {
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

        assertSame(
                exception,
                actual
        );

        verify(
                transactionEventPort,
                times(2)
        ).publishAccountBalanceChanged(
                any(AccountBalanceChangedEvent.class)
        );
    }

    private TransferMoneyCommand createValidCommand() {
        return new TransferMoneyCommand(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                Currency.VND,
                REFERENCE,
                DESCRIPTION
        );
    }

    private void assertInvalidRequest(
            TransferMoneyCommand command
    ) {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractionsWithDependencies();
    }

    private void mockSuccessfulTransfer() {
        mockBusinessDate();
        mockSaveTransaction();
    }

    private void mockBusinessDate() {
        when(
                transactionBusinessDayPort
                        .getCurrentBusinessDate()
        ).thenReturn(
                BUSINESS_DATE
        );
    }

    private void mockSaveTransaction() {
        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    private void verifyNoInteractionsWithDependencies() {
        verifyNoInteractions(
                transactionCommandRepository,
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }
}