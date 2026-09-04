package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.AccountWithdrawPort;
import com.example.ledgercore.transaction.command.port.outbound.BusinessDayPort;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private BusinessDayPort businessDayPort;

    private WithdrawMoneyHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID transactionId;

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 8, 27);

    @BeforeEach
    void setUp() {
        handler = new WithdrawMoneyHandler(
                transactionCommandRepository,
                accountWithdrawPort,
                ledgerWithdrawPort,
                transactionEventPort,
                businessDayPort
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {

        WithdrawMoneyCommand command =
                command(
                        "100",
                        "VND",
                        "WD-001",
                        "Cash withdrawal"
                );

        mockNewWithdraw(
                "WD-001",
                new BigDecimal("1000"),
                "VND"
        );

        when(businessDayPort.getCurrentBusinessDate())
                .thenReturn(BUSINESS_DATE);

        mockSaveTransaction();

        TransactionResponse response =
                handler.execute(userId, command);

        assertNotNull(response);
        assertEquals(transactionId, response.id());
        assertEquals("WD-001", response.reference());
        assertEquals(
                TransactionType.WITHDRAW,
                response.type()
        );
        assertEquals(
                TransactionStatus.COMPLETED,
                response.status()
        );
        assertEquals(
                accountId,
                response.sourceAccountId()
        );
        assertEquals(
                new BigDecimal("100"),
                response.amount()
        );
        assertEquals("VND", response.currency());
        assertEquals(
                "Cash withdrawal",
                response.description()
        );
        assertNotNull(response.completedAt());

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        verify(transactionCommandRepository)
                .save(captor.capture());

        MoneyTransaction transaction =
                captor.getValue();

        assertEquals(
                BUSINESS_DATE,
                transaction.getBusinessDate()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        verify(accountWithdrawPort)
                .withdraw(
                        accountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        verify(ledgerWithdrawPort)
                .recordWithdraw(
                        transactionId,
                        accountId,
                        new BigDecimal("100"),
                        "VND",
                        BUSINESS_DATE
                );

        verify(transactionEventPort)
                .publishWithdrawCompleted(
                        any(WithdrawCompletedEvent.class)
                );

        verify(businessDayPort)
                .getCurrentBusinessDate();
    }

    @Test
    void shouldCreatePendingWithdrawTransactionBeforeCompletion() {

        WithdrawMoneyCommand command =
                command(
                        "100",
                        "VND",
                        "WD-002",
                        null
                );

        mockNewWithdraw(
                "WD-002",
                new BigDecimal("1000"),
                "VND"
        );

        when(businessDayPort.getCurrentBusinessDate())
                .thenReturn(BUSINESS_DATE);

        doAnswer(invocation -> {

            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(
                    TransactionStatus.PENDING,
                    transaction.getStatus()
            );

            assertEquals(
                    TransactionType.WITHDRAW,
                    transaction.getType()
            );

            assertEquals(
                    "WD-002",
                    transaction.getReference()
            );

            assertEquals(
                    accountId,
                    transaction.getSourceAccountId()
            );

            assertEquals(
                    new BigDecimal("100"),
                    transaction.getAmount()
            );

            assertEquals(
                    "VND",
                    transaction.getCurrency()
            );

            assertEquals(
                    BUSINESS_DATE,
                    transaction.getBusinessDate()
            );

            transaction.setId(transactionId);

            return transaction;

        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        handler.execute(userId, command);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    @Test
    void shouldReturnExistingWithdrawTransaction() {

        MoneyTransaction existing =
                MoneyTransaction.builder()
                        .id(transactionId)
                        .reference("WD-003")
                        .type(TransactionType.WITHDRAW)
                        .status(TransactionStatus.COMPLETED)
                        .sourceAccountId(accountId)
                        .amount(new BigDecimal("100"))
                        .currency("VND")
                        .build();

        when(transactionCommandRepository.findByReference(
                "WD-003"
        )).thenReturn(Optional.of(existing));

        WithdrawMoneyCommand command =
                command(
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
                handler.execute(
                        userId,
                        command
                );

        assertEquals(
                transactionId,
                response.id()
        );

        assertEquals(
                "WD-003",
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

        verify(accountWithdrawPort)
                .verifySourceAccountAccess(
                        userId,
                        accountId
                );

        verify(accountWithdrawPort, never())
                .getWithdrawInfo(any(), any());

        verify(accountWithdrawPort, never())
                .withdraw(any(), any(), any());

        verify(ledgerWithdrawPort, never())
                .recordWithdraw(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );

        verify(transactionCommandRepository, never())
                .save(any());

        verifyNoInteractions(
                businessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldThrowWhenExistingReferenceBelongsToAnotherTransactionType() {

        MoneyTransaction existing =
                MoneyTransaction.builder()
                        .id(transactionId)
                        .reference("REF-001")
                        .type(TransactionType.DEPOSIT)
                        .status(TransactionStatus.COMPLETED)
                        .build();

        when(transactionCommandRepository.findByReference(
                "REF-001"
        )).thenReturn(Optional.of(existing));

        WithdrawMoneyCommand command =
                command(
                        "100",
                        "VND",
                        "REF-001",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSACTION_REFERENCE_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountWithdrawPort,
                ledgerWithdrawPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {

        WithdrawMoneyCommand command =
                command(
                        "0",
                        "VND",
                        "WD-004",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                accountWithdrawPort,
                ledgerWithdrawPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {

        WithdrawMoneyCommand command =
                command(
                        "-100",
                        "VND",
                        "WD-005",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                accountWithdrawPort,
                ledgerWithdrawPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {

        WithdrawMoneyCommand command =
                command(
                        "100",
                        "USD",
                        "WD-006",
                        null
                );

        mockNewWithdraw(
                "WD-006",
                new BigDecimal("1000"),
                "VND"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSACTION_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountWithdrawPort, never())
                .withdraw(any(), any(), any());

        verifyNoInteractions(
                ledgerWithdrawPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {

        WithdrawMoneyCommand command =
                command(
                        "1000",
                        "VND",
                        "WD-007",
                        null
                );

        mockNewWithdraw(
                "WD-007",
                new BigDecimal("500"),
                "VND"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountWithdrawPort, never())
                .withdraw(any(), any(), any());

        verifyNoInteractions(
                ledgerWithdrawPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldUseCurrentBusinessDate() {

        WithdrawMoneyCommand command =
                command(
                        "100",
                        "VND",
                        "WD-008",
                        "withdraw"
                );

        mockNewWithdraw(
                "WD-008",
                new BigDecimal("1000"),
                "VND"
        );

        when(businessDayPort.getCurrentBusinessDate())
                .thenReturn(BUSINESS_DATE);

        mockSaveTransaction();

        handler.execute(
                userId,
                command
        );

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        verify(transactionCommandRepository)
                .save(captor.capture());

        MoneyTransaction transaction =
                captor.getValue();

        assertEquals(
                BUSINESS_DATE,
                transaction.getBusinessDate()
        );

        verify(ledgerWithdrawPort)
                .recordWithdraw(
                        transactionId,
                        accountId,
                        new BigDecimal("100"),
                        "VND",
                        BUSINESS_DATE
                );
    }

    @Test
    void shouldPublishCompletedEvent() {

        WithdrawMoneyCommand command =
                command(
                        "100",
                        "VND",
                        "WD-009",
                        "withdraw"
                );

        mockNewWithdraw(
                "WD-009",
                new BigDecimal("1000"),
                "VND"
        );

        when(businessDayPort.getCurrentBusinessDate())
                .thenReturn(BUSINESS_DATE);

        mockSaveTransaction();

        handler.execute(
                userId,
                command
        );

        ArgumentCaptor<WithdrawCompletedEvent> captor =
                ArgumentCaptor.forClass(
                        WithdrawCompletedEvent.class
                );

        verify(transactionEventPort)
                .publishWithdrawCompleted(
                        captor.capture()
                );

        WithdrawCompletedEvent event =
                captor.getValue();

        assertEquals(
                transactionId,
                event.transactionId()
        );

        assertEquals(
                "WD-009",
                event.reference()
        );

        assertEquals(
                accountId,
                event.accountId()
        );

        assertEquals(
                new BigDecimal("100"),
                event.amount()
        );

        assertEquals(
                "VND",
                event.currency()
        );

        assertNotNull(
                event.completedAt()
        );
    }

    private void mockNewWithdraw(
            String reference,
            BigDecimal balance,
            String currency
    ) {
        when(transactionCommandRepository.findByReference(
                reference
        )).thenReturn(Optional.empty());

        when(accountWithdrawPort.getWithdrawInfo(
                userId,
                accountId
        )).thenReturn(
                new AccountWithdrawPort.WithdrawAccountInfo(
                        accountId,
                        currency,
                        balance
                )
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