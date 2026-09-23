package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.port.outbound.WithdrawLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.WithdrawUserAccountPort;
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
class WithdrawMoneyHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private WithdrawUserAccountPort withdrawUserAccountPort;

    @Mock
    private WithdrawLedgerPort withdrawLedgerPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    @Mock
    private TransactionBusinessDayPort transactionBusinessDayPort;

    private WithdrawMoneyHandler handler;

    private UUID sourceAccountId;
    private UUID transactionId;

    private BigDecimal amount;
    private Currency currency;
    private LocalDate businessDate;

    private WithdrawMoneyCommand command;

    @BeforeEach
    void setUp() {
        handler = new WithdrawMoneyHandler(
                transactionCommandRepository,
                withdrawUserAccountPort,
                withdrawLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        sourceAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        amount = new BigDecimal("150000.00");
        currency = Currency.VND;
        businessDate = LocalDate.of(2026, 9, 24);

        command = new WithdrawMoneyCommand(
                sourceAccountId,
                amount,
                currency,
                "WD-001",
                "Cash withdrawal"
        );
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {
        mockSuccessfulWithdrawal();

        TransactionResponse response =
                handler.execute(command);

        assertNotNull(response);

        assertEquals(transactionId, response.id());
        assertEquals(
                command.reference(),
                response.reference()
        );
        assertEquals(
                TransactionType.WITHDRAW,
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
        assertNull(response.destinationAccountId());
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

        verify(withdrawUserAccountPort)
                .withdraw(
                        sourceAccountId,
                        amount,
                        businessDate
                );

        verify(withdrawLedgerPort)
                .recordWithdraw(
                        transactionId,
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verify(transactionEventPort)
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
                TransactionType.WITHDRAW,
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
        assertNull(
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
        mockSuccessfulWithdrawal();

        InOrder inOrder = inOrder(
                transactionBusinessDayPort,
                transactionCommandRepository,
                withdrawUserAccountPort,
                withdrawLedgerPort,
                transactionEventPort
        );

        handler.execute(command);

        inOrder.verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        inOrder.verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        inOrder.verify(withdrawUserAccountPort)
                .withdraw(
                        sourceAccountId,
                        amount,
                        businessDate
                );

        inOrder.verify(withdrawLedgerPort)
                .recordWithdraw(
                        transactionId,
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        inOrder.verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );
    }

    @Test
    void shouldCompleteTransactionBeforePublishingBalanceEvent() {
        mockSuccessfulWithdrawal();

        ArgumentCaptor<AccountBalanceChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        handler.execute(command);

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        eventCaptor.capture()
                );

        AccountBalanceChangedEvent event =
                eventCaptor.getValue();

        assertEquals(
                TransactionStatus.COMPLETED,
                getSavedTransaction().getStatus()
        );

        assertNotNull(
                getSavedTransaction().getCompletedAt()
        );

        assertEquals(
                transactionId,
                event.transactionId()
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        WithdrawMoneyCommand invalidCommand =
                new WithdrawMoneyCommand(
                        sourceAccountId,
                        BigDecimal.ZERO,
                        currency,
                        "WD-001",
                        "Cash withdrawal"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                withdrawUserAccountPort,
                withdrawLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        WithdrawMoneyCommand invalidCommand =
                new WithdrawMoneyCommand(
                        sourceAccountId,
                        new BigDecimal("-1.00"),
                        currency,
                        "WD-001",
                        "Cash withdrawal"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                withdrawUserAccountPort,
                withdrawLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldPropagateBusinessDateFailure() {
        RuntimeException exception =
                new RuntimeException(
                        "Business date unavailable"
                );

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
                withdrawUserAccountPort,
                withdrawLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateTransactionSaveFailure() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        RuntimeException exception =
                new RuntimeException(
                        "Transaction save failed"
                );

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
                withdrawUserAccountPort,
                withdrawLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateAccountWithdrawFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException(
                        "Account withdrawal failed"
                );

        doThrow(exception)
                .when(withdrawUserAccountPort)
                .withdraw(
                        sourceAccountId,
                        amount,
                        businessDate
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(command)
                );

        assertSame(exception, actual);

        verify(withdrawUserAccountPort)
                .withdraw(
                        sourceAccountId,
                        amount,
                        businessDate
                );

        verifyNoInteractions(
                withdrawLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateLedgerFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException("Ledger failed");

        doThrow(exception)
                .when(withdrawLedgerPort)
                .recordWithdraw(
                        transactionId,
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

        verify(withdrawLedgerPort)
                .recordWithdraw(
                        transactionId,
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                );

        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldPropagateBalanceEventFailure() {
        mockTransactionSave();

        RuntimeException exception =
                new RuntimeException(
                        "Balance event publishing failed"
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
    void shouldNotCallDependenciesWhenValidationFails() {
        WithdrawMoneyCommand invalidCommand =
                new WithdrawMoneyCommand(
                        sourceAccountId,
                        BigDecimal.ZERO,
                        currency,
                        "WD-001",
                        "Cash withdrawal"
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        verifyNoInteractions(
                transactionCommandRepository,
                withdrawUserAccountPort,
                withdrawLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    private void mockSuccessfulWithdrawal() {
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
}