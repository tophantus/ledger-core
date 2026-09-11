package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalCommand;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalResponse;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalTransactionPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalExecutionCommandRepository;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeHasher;
import com.example.ledgercore.withdrawal.entity.WithdrawalExecution;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteWithdrawalExecutionServiceTest {

    @Mock
    private WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    @Mock
    private WithdrawalExecutionCommandRepository
            withdrawalExecutionCommandRepository;

    @Mock
    private WithdrawalCodeHasher
            withdrawalCodeHasher;

    @Mock
    private WithdrawalTransactionPort
            withdrawalTransactionPort;

    @Mock
    private WithdrawalHoldPort
            withdrawalHoldPort;

    private ExecuteWithdrawalExecutionService service;

    private Clock clock;

    private UUID intentId;
    private UUID accountId;
    private UUID atmId;
    private UUID transactionId;
    private UUID executionId;
    private UUID holdId;

    private static final Instant NOW =
            Instant.parse("2026-08-27T10:00:00Z");

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new ExecuteWithdrawalExecutionService(
                withdrawalIntentCommandRepository,
                withdrawalExecutionCommandRepository,
                withdrawalCodeHasher,
                withdrawalTransactionPort,
                withdrawalHoldPort,
                clock
        );

        intentId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        atmId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        executionId = UUID.randomUUID();
        holdId = UUID.randomUUID();
    }

    @Test
    void shouldExecuteWithdrawalSuccessfully() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        ExecuteWithdrawalCommand command =
                command();

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        ExecuteWithdrawalResponse response =
                service.execute(
                        command,
                        intentId,
                        accountId,
                        atmId
                );

        assertNotNull(response);

        assertEquals(
                executionId,
                response.executionId()
        );

        assertEquals(
                intentId,
                response.withdrawalIntentId()
        );

        assertEquals(
                transactionId,
                response.transactionId()
        );

        assertEquals(
                atmId,
                response.atmTerminalId()
        );

        assertEquals(
                "WD-ABC123",
                response.withdrawalReference()
        );

        assertEquals(
                new BigDecimal("100000"),
                response.amount()
        );

        assertEquals(
                Currency.VND,
                response.currency()
        );

        assertEquals(
                NOW,
                response.executedAt()
        );

        assertEquals(
                WithdrawalIntentStatus.COMPLETED,
                intent.getStatus()
        );

        verify(
                withdrawalTransactionPort
        ).withdraw(
                accountId,
                new BigDecimal("100000"),
                Currency.VND,
                "WD-ABC123",
                "ATM withdrawal"
        );

        verify(
                withdrawalHoldPort
        ).releaseHold(holdId);
    }

    @Test
    void shouldUseCurrentClockForExecutionTime() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        ExecuteWithdrawalResponse response =
                service.execute(
                        command(),
                        intentId,
                        accountId,
                        atmId
                );

        assertEquals(
                NOW,
                response.executedAt()
        );

        ArgumentCaptor<WithdrawalExecution> captor =
                ArgumentCaptor.forClass(
                        WithdrawalExecution.class
                );

        verify(
                withdrawalExecutionCommandRepository
        ).save(captor.capture());

        assertEquals(
                NOW,
                captor.getValue().getExecutedAt()
        );

        assertEquals(
                NOW,
                captor.getValue().getCreatedAt()
        );
    }

    @Test
    void shouldCreateExecutionWithCorrectData() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        ArgumentCaptor<WithdrawalExecution> captor =
                ArgumentCaptor.forClass(
                        WithdrawalExecution.class
                );

        service.execute(
                command(),
                intentId,
                accountId,
                atmId
        );

        verify(
                withdrawalExecutionCommandRepository
        ).save(captor.capture());

        WithdrawalExecution execution =
                captor.getValue();

        assertEquals(
                intentId,
                execution.getWithdrawalIntentId()
        );

        assertEquals(
                atmId,
                execution.getAtmTerminalId()
        );

        assertEquals(
                transactionId,
                execution.getTransactionId()
        );

        assertEquals(
                NOW,
                execution.getExecutedAt()
        );

        assertEquals(
                NOW,
                execution.getCreatedAt()
        );
    }

    @Test
    void shouldSaveExecutionBeforeReleasingHold() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        InOrder inOrder = inOrder(
                withdrawalExecutionCommandRepository,
                withdrawalHoldPort
        );

        service.execute(
                command(),
                intentId,
                accountId,
                atmId
        );

        inOrder.verify(
                withdrawalExecutionCommandRepository
        ).save(any(WithdrawalExecution.class));

        inOrder.verify(
                withdrawalHoldPort
        ).releaseHold(holdId);
    }

    @Test
    void shouldReleaseCorrectHold() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        service.execute(
                command(),
                intentId,
                accountId,
                atmId
        );

        verify(
                withdrawalHoldPort
        ).releaseHold(holdId);
    }

    @Test
    void shouldCompleteIntentAfterExecution() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        service.execute(
                command(),
                intentId,
                accountId,
                atmId
        );

        assertEquals(
                WithdrawalIntentStatus.COMPLETED,
                intent.getStatus()
        );

        assertEquals(
                NOW,
                intent.getCompletedAt()
        );
    }

    @Test
    void shouldThrowWhenIntentDoesNotExist() {

        when(
                withdrawalIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(
                Optional.empty()
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                command(),
                                intentId,
                                accountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalExecutionCommandRepository,
                withdrawalCodeHasher,
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenAccountDoesNotMatchIntent() {

        UUID anotherAccountId =
                UUID.randomUUID();

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                command(),
                                intentId,
                                anotherAccountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalExecutionCommandRepository,
                withdrawalCodeHasher,
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenIntentIsNotReady() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        intent.setStatus(
                WithdrawalIntentStatus.COMPLETED
        );

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                command(),
                                intentId,
                                accountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_READY,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalExecutionCommandRepository,
                withdrawalCodeHasher,
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenIntentIsExpired() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.minusSeconds(1)
                );

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                command(),
                                intentId,
                                accountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_EXPIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalExecutionCommandRepository,
                withdrawalCodeHasher,
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenIntentExpiresExactlyAtNow() {

        WithdrawalIntent intent =
                readyIntent(NOW);

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                command(),
                                intentId,
                                accountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_EXPIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalExecutionCommandRepository,
                withdrawalCodeHasher,
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenWithdrawalCodeIsInvalid() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);

        when(
                withdrawalCodeHasher.matches(
                        command().withdrawalCode(),
                        intent.getWithdrawalCodeHash()
                )
        ).thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                command(),
                                intentId,
                                accountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_CODE_INVALID,
                exception.getErrorCode()
        );

        verify(
                withdrawalCodeHasher
        ).matches(
                command().withdrawalCode(),
                intent.getWithdrawalCodeHash()
        );

        verifyNoInteractions(
                withdrawalExecutionCommandRepository,
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenAmountDoesNotMatchIntent() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        "WD-ABC123",
                        "123456",
                        new BigDecimal("200000")
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                invalidCommand,
                                intentId,
                                accountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_AMOUNT_MISMATCH,
                exception.getErrorCode()
        );

        verify(
                withdrawalCodeHasher
        ).matches(
                "123456",
                intent.getWithdrawalCodeHash()
        );

        verifyNoInteractions(
                withdrawalExecutionCommandRepository,
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenWithdrawalAlreadyExecuted() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();

        when(
                withdrawalExecutionCommandRepository
                        .existsByWithdrawalIntentId(intentId)
        ).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                command(),
                                intentId,
                                accountId,
                                atmId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_ALREADY_EXECUTED,
                exception.getErrorCode()
        );

        verify(
                withdrawalExecutionCommandRepository
        ).existsByWithdrawalIntentId(intentId);

        verifyNoInteractions(
                withdrawalTransactionPort,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldCheckDuplicateBeforeCreatingTransaction() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();

        when(
                withdrawalExecutionCommandRepository
                        .existsByWithdrawalIntentId(intentId)
        ).thenReturn(true);

        InOrder inOrder = inOrder(
                withdrawalExecutionCommandRepository,
                withdrawalTransactionPort
        );

        assertThrows(
                BusinessException.class,
                () -> service.execute(
                        command(),
                        intentId,
                        accountId,
                        atmId
                )
        );

        inOrder.verify(
                withdrawalExecutionCommandRepository
        ).existsByWithdrawalIntentId(intentId);

        verifyNoInteractions(
                withdrawalTransactionPort
        );
    }

    @Test
    void shouldWithdrawUsingIntentData() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        service.execute(
                command(),
                intentId,
                accountId,
                atmId
        );

        verify(
                withdrawalTransactionPort
        ).withdraw(
                intent.getAccountId(),
                intent.getAmount(),
                intent.getCurrency(),
                intent.getWithdrawalReference(),
                "ATM withdrawal"
        );
    }

    @Test
    void shouldNotTrustCommandAmountForTransaction() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        ExecuteWithdrawalCommand command =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        "WD-ABC123",
                        "123456",
                        new BigDecimal("100000")
                );

        service.execute(
                command,
                intentId,
                accountId,
                atmId
        );

        verify(
                withdrawalTransactionPort
        ).withdraw(
                eq(accountId),
                eq(intent.getAmount()),
                eq(intent.getCurrency()),
                eq(intent.getWithdrawalReference()),
                eq("ATM withdrawal")
        );
    }

    @Test
    void shouldUseAuthenticatedAtmIdForExecution() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        ArgumentCaptor<WithdrawalExecution> captor =
                ArgumentCaptor.forClass(
                        WithdrawalExecution.class
                );

        service.execute(
                command(),
                intentId,
                accountId,
                atmId
        );

        verify(
                withdrawalExecutionCommandRepository
        ).save(captor.capture());

        assertEquals(
                atmId,
                captor.getValue().getAtmTerminalId()
        );
    }

    @Test
    void shouldExecuteOperationsInCorrectOrder() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        InOrder inOrder = inOrder(
                withdrawalTransactionPort,
                withdrawalExecutionCommandRepository,
                withdrawalHoldPort
        );

        service.execute(
                command(),
                intentId,
                accountId,
                atmId
        );

        inOrder.verify(
                withdrawalTransactionPort
        ).withdraw(
                accountId,
                new BigDecimal("100000"),
                Currency.VND,
                "WD-ABC123",
                "ATM withdrawal"
        );

        inOrder.verify(
                withdrawalExecutionCommandRepository
        ).save(
                any(WithdrawalExecution.class)
        );

        inOrder.verify(
                withdrawalHoldPort
        ).releaseHold(holdId);
    }

    @Test
    void shouldNotCreateTransactionWhenCodeIsInvalid() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);

        when(
                withdrawalCodeHasher.matches(
                        "123456",
                        intent.getWithdrawalCodeHash()
                )
        ).thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> service.execute(
                        command(),
                        intentId,
                        accountId,
                        atmId
                )
        );

        verifyNoInteractions(
                withdrawalTransactionPort,
                withdrawalExecutionCommandRepository,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldNotReleaseHoldWhenTransactionFails() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();

        when(
                withdrawalTransactionPort.withdraw(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND,
                        "WD-ABC123",
                        "ATM withdrawal"
                )
        ).thenThrow(
                new BusinessException(
                        ErrorCode.INVALID_REQUEST
                )
        );

        assertThrows(
                BusinessException.class,
                () -> service.execute(
                        command(),
                        intentId,
                        accountId,
                        atmId
                )
        );

        verify(
                withdrawalExecutionCommandRepository
        ).existsByWithdrawalIntentId(intentId);

        verify(
                withdrawalTransactionPort
        ).withdraw(
                accountId,
                new BigDecimal("100000"),
                Currency.VND,
                "WD-ABC123",
                "ATM withdrawal"
        );

        verifyNoMoreInteractions(
                withdrawalExecutionCommandRepository
        );

        verifyNoInteractions(
                withdrawalHoldPort
        );

        assertEquals(
                WithdrawalIntentStatus.READY,
                intent.getStatus()
        );
    }

    @Test
    void shouldNotCompleteIntentWhenHoldReleaseFails() {

        WithdrawalIntent intent =
                readyIntent(
                        NOW.plusSeconds(600)
                );

        mockIntent(intent);
        mockValidCode();
        mockNotExecuted();
        mockTransaction();
        mockExecutionSave();

        doThrow(
                new BusinessException(
                        ErrorCode.INVALID_REQUEST
                )
        ).when(
                withdrawalHoldPort
        ).releaseHold(holdId);

        assertThrows(
                BusinessException.class,
                () -> service.execute(
                        command(),
                        intentId,
                        accountId,
                        atmId
                )
        );

        assertEquals(
                WithdrawalIntentStatus.READY,
                intent.getStatus()
        );
    }

    private ExecuteWithdrawalCommand command() {

        return new ExecuteWithdrawalCommand(
                "ATM-HN-001",
                "atm-secret",
                "WD-ABC123",
                "123456",
                new BigDecimal("100000")
        );
    }

    private WithdrawalIntent readyIntent(
            Instant expiresAt
    ) {

        return WithdrawalIntent.builder()
                .id(intentId)
                .withdrawalRequestId(UUID.randomUUID())
                .withdrawalReference("WD-ABC123")
                .userId(UUID.randomUUID())
                .accountId(accountId)
                .holdId(holdId)
                .amount(new BigDecimal("100000"))
                .currency(Currency.VND)
                .withdrawalCodeHash("HASHED-CODE")
                .status(WithdrawalIntentStatus.READY)
                .expiresAt(expiresAt)
                .createdAt(NOW)
                .version(0L)
                .build();
    }

    private void mockIntent(
            WithdrawalIntent intent
    ) {

        when(
                withdrawalIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(
                Optional.of(intent)
        );
    }

    private void mockValidCode() {

        when(
                withdrawalCodeHasher.matches(
                        "123456",
                        "HASHED-CODE"
                )
        ).thenReturn(true);
    }

    private void mockNotExecuted() {

        when(
                withdrawalExecutionCommandRepository
                        .existsByWithdrawalIntentId(intentId)
        ).thenReturn(false);
    }

    private void mockTransaction() {

        when(
                withdrawalTransactionPort.withdraw(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND,
                        "WD-ABC123",
                        "ATM withdrawal"
                )
        ).thenReturn(transactionId);
    }

    private void mockExecutionSave() {

        when(
                withdrawalExecutionCommandRepository
                        .save(any(WithdrawalExecution.class))
        ).thenAnswer(invocation -> {

            WithdrawalExecution execution =
                    invocation.getArgument(0);

            execution.setId(executionId);

            return execution;
        });
    }
}
