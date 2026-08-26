package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.AccountWithdrawPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerWithdrawPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.WithdrawCompletedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawMoneyHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private AccountWithdrawPort accountWithdrawPort;

    @Mock
    private LedgerWithdrawPort ledgerWithdrawPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    private WithdrawMoneyHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        handler = new WithdrawMoneyHandler(
                transactionCommandRepository,
                accountWithdrawPort,
                ledgerWithdrawPort,
                transactionEventPort
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {
        WithdrawMoneyCommand command = command(
                "100",
                "VND",
                "WD-001",
                "Cash withdrawal"
        );

        when(transactionCommandRepository.findByReference("WD-001"))
                .thenReturn(Optional.empty());

        when(accountWithdrawPort.getWithdrawInfo(
                userId,
                accountId
        )).thenReturn(
                new AccountWithdrawPort.WithdrawAccountInfo(
                        accountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(TransactionStatus.PENDING,
                    transaction.getStatus());

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        TransactionResponse response =
                handler.execute(userId, command);

        assertNotNull(response);
        assertEquals(transactionId, response.id());
        assertEquals(TransactionType.WITHDRAW, response.type());
        assertEquals(TransactionStatus.COMPLETED,
                response.status());
        assertEquals(accountId,
                response.sourceAccountId());
        assertEquals(new BigDecimal("100"),
                response.amount());
        assertEquals("VND", response.currency());
        assertNotNull(response.completedAt());

        verify(accountWithdrawPort)
                .withdraw(
                        accountId,
                        new BigDecimal("100")
                );

        verify(ledgerWithdrawPort)
                .recordWithdraw(
                        transactionId,
                        accountId,
                        new BigDecimal("100"),
                        "VND"
                );

        verify(transactionEventPort)
                .publishWithdrawCompleted(
                        any(WithdrawCompletedEvent.class)
                );
    }

    @Test
    void shouldCreatePendingWithdrawTransactionBeforeCompletion() {
        WithdrawMoneyCommand command = command(
                "100",
                "VND",
                "WD-002",
                null
        );

        when(transactionCommandRepository.findByReference("WD-002"))
                .thenReturn(Optional.empty());

        when(accountWithdrawPort.getWithdrawInfo(
                userId,
                accountId
        )).thenReturn(
                new AccountWithdrawPort.WithdrawAccountInfo(
                        accountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(TransactionStatus.PENDING,
                    transaction.getStatus());

            assertEquals(TransactionType.WITHDRAW,
                    transaction.getType());

            assertEquals(accountId,
                    transaction.getSourceAccountId());

            assertEquals(new BigDecimal("100"),
                    transaction.getAmount());

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        handler.execute(userId, command);
    }

    @Test
    void shouldReturnExistingWithdrawTransaction() {
        MoneyTransaction existing = MoneyTransaction.builder()
                .id(transactionId)
                .reference("WD-003")
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(accountId)
                .amount(new BigDecimal("100"))
                .currency("VND")
                .build();

        when(transactionCommandRepository.findByReference("WD-003"))
                .thenReturn(Optional.of(existing));

        WithdrawMoneyCommand command = command(
                "100",
                "VND",
                "WD-003",
                null
        );

        doNothing().when(accountWithdrawPort)
                .verifySourceAccountAccess(
                        userId,
                        accountId
                );

        TransactionResponse response =
                handler.execute(userId, command);

        assertEquals(transactionId, response.id());
        assertEquals(TransactionType.WITHDRAW,
                response.type());

        verify(accountWithdrawPort)
                .verifySourceAccountAccess(
                        userId,
                        accountId
                );

        verify(accountWithdrawPort, never())
                .getWithdrawInfo(any(), any());

        verify(accountWithdrawPort, never())
                .withdraw(any(), any());

        verify(ledgerWithdrawPort, never())
                .recordWithdraw(any(), any(), any(), any());

        verify(transactionCommandRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenExistingReferenceBelongsToAnotherTransactionType() {
        MoneyTransaction existing = MoneyTransaction.builder()
                .id(transactionId)
                .reference("REF-001")
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .build();

        when(transactionCommandRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(existing));

        WithdrawMoneyCommand command = command(
                "100",
                "VND",
                "REF-001",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                "TRANSACTION_REFERENCE_ALREADY_EXISTS",
                exception.getErrorCode().name()
        );

        verifyNoInteractions(accountWithdrawPort);
        verifyNoInteractions(ledgerWithdrawPort);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        WithdrawMoneyCommand command = command(
                "0",
                "VND",
                "WD-004",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                "INVALID_WITHDRAW_AMOUNT",
                exception.getErrorCode().name()
        );

        verifyNoInteractions(transactionCommandRepository);
        verifyNoInteractions(accountWithdrawPort);
        verifyNoInteractions(ledgerWithdrawPort);
        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        WithdrawMoneyCommand command = command(
                "-100",
                "VND",
                "WD-005",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                "INVALID_WITHDRAW_AMOUNT",
                exception.getErrorCode().name()
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {
        WithdrawMoneyCommand command = command(
                "100",
                "USD",
                "WD-006",
                null
        );

        when(transactionCommandRepository.findByReference("WD-006"))
                .thenReturn(Optional.empty());

        when(accountWithdrawPort.getWithdrawInfo(
                userId,
                accountId
        )).thenReturn(
                new AccountWithdrawPort.WithdrawAccountInfo(
                        accountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                "TRANSACTION_CURRENCY_MISMATCH",
                exception.getErrorCode().name()
        );

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountWithdrawPort, never())
                .withdraw(any(), any());

        verifyNoInteractions(ledgerWithdrawPort);
        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        WithdrawMoneyCommand command = command(
                "1000",
                "VND",
                "WD-007",
                null
        );

        when(transactionCommandRepository.findByReference("WD-007"))
                .thenReturn(Optional.empty());

        when(accountWithdrawPort.getWithdrawInfo(
                userId,
                accountId
        )).thenReturn(
                new AccountWithdrawPort.WithdrawAccountInfo(
                        accountId,
                        "VND",
                        new BigDecimal("500")
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                "ACCOUNT_INSUFFICIENT_BALANCE",
                exception.getErrorCode().name()
        );

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountWithdrawPort, never())
                .withdraw(any(), any());

        verifyNoInteractions(ledgerWithdrawPort);
        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldPublishCompletedEvent() {
        WithdrawMoneyCommand command = command(
                "100",
                "VND",
                "WD-008",
                "withdraw"
        );

        when(transactionCommandRepository.findByReference("WD-008"))
                .thenReturn(Optional.empty());

        when(accountWithdrawPort.getWithdrawInfo(
                userId,
                accountId
        )).thenReturn(
                new AccountWithdrawPort.WithdrawAccountInfo(
                        accountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        handler.execute(userId, command);

        ArgumentCaptor<WithdrawCompletedEvent> captor =
                ArgumentCaptor.forClass(
                        WithdrawCompletedEvent.class
                );

        verify(transactionEventPort)
                .publishWithdrawCompleted(captor.capture());

        WithdrawCompletedEvent event =
                captor.getValue();

        assertEquals(transactionId, event.transactionId());
        assertEquals("WD-008", event.reference());
        assertEquals(accountId,
                event.accountId());
        assertEquals(new BigDecimal("100"),
                event.amount());
        assertEquals("VND", event.currency());
        assertNotNull(event.completedAt());
    }

    private WithdrawMoneyCommand command(
            String amount,
            String currency,
            String reference,
            String description
    ) {
        return new WithdrawMoneyCommand(
                accountId,
                new BigDecimal(amount),
                currency,
                reference,
                description
        );
    }
}