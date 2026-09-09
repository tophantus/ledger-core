package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalCommand;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalResponse;
import com.example.ledgercore.withdrawal.command.port.outbound.AtmAuthenticationPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteWithdrawalHandlerTest {

    @Mock
    private AtmAuthenticationPort
            atmAuthenticationPort;

    @Mock
    private WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    @Mock
    private ExecuteWithdrawalExecutionService
            executeWithdrawalExecutionService;

    private ExecuteWithdrawalHandler handler;

    private UUID atmId;
    private UUID intentId;
    private UUID accountId;

    private ExecuteWithdrawalCommand command;

    @BeforeEach
    void setUp() {

        handler = new ExecuteWithdrawalHandler(
                atmAuthenticationPort,
                withdrawalIntentCommandRepository,
                executeWithdrawalExecutionService
        );

        atmId = UUID.randomUUID();
        intentId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        command = new ExecuteWithdrawalCommand(
                "ATM-HN-001",
                "atm-secret",
                "WD-ABC123",
                "123456",
                new BigDecimal("100000")
        );
    }

    @Test
    void shouldExecuteWithdrawalSuccessfully() {

        WithdrawalIntent intent =
                readyIntent();

        ExecuteWithdrawalResponse expectedResponse =
                new ExecuteWithdrawalResponse(
                        UUID.randomUUID(),
                        intentId,
                        UUID.randomUUID(),
                        atmId,
                        "WD-ABC123",
                        new BigDecimal("100000"),
                        "VND",
                        Instant.parse(
                                "2026-08-27T10:00:00Z"
                        )
                );

        when(
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                )
        ).thenReturn(atmId);

        when(
                withdrawalIntentCommandRepository
                        .findByWithdrawalReference(
                                command.withdrawalReference()
                        )
        ).thenReturn(
                Optional.of(intent)
        );

        when(
                executeWithdrawalExecutionService.execute(
                        command,
                        intentId,
                        accountId,
                        atmId
                )
        ).thenReturn(expectedResponse);

        ExecuteWithdrawalResponse response =
                handler.execute(command);

        assertSame(
                expectedResponse,
                response
        );

        verify(
                atmAuthenticationPort
        ).authenticate(
                command.terminalCode(),
                command.credential()
        );

        verify(
                withdrawalIntentCommandRepository
        ).findByWithdrawalReference(
                command.withdrawalReference()
        );

        verify(
                executeWithdrawalExecutionService
        ).execute(
                command,
                intentId,
                accountId,
                atmId
        );
    }

    @Test
    void shouldAuthenticateAtmBeforeFindingIntent() {

        WithdrawalIntent intent =
                readyIntent();

        when(
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                )
        ).thenReturn(atmId);

        when(
                withdrawalIntentCommandRepository
                        .findByWithdrawalReference(
                                command.withdrawalReference()
                        )
        ).thenReturn(
                Optional.of(intent)
        );

        when(
                executeWithdrawalExecutionService.execute(
                        any(),
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(null);

        InOrder inOrder = inOrder(
                atmAuthenticationPort,
                withdrawalIntentCommandRepository
        );

        handler.execute(command);

        inOrder.verify(
                atmAuthenticationPort
        ).authenticate(
                command.terminalCode(),
                command.credential()
        );

        inOrder.verify(
                withdrawalIntentCommandRepository
        ).findByWithdrawalReference(
                command.withdrawalReference()
        );
    }

    @Test
    void shouldPassCorrectDataToExecutionService() {

        WithdrawalIntent intent =
                readyIntent();

        when(
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                )
        ).thenReturn(atmId);

        when(
                withdrawalIntentCommandRepository
                        .findByWithdrawalReference(
                                command.withdrawalReference()
                        )
        ).thenReturn(
                Optional.of(intent)
        );

        when(
                executeWithdrawalExecutionService.execute(
                        command,
                        intentId,
                        accountId,
                        atmId
                )
        ).thenReturn(null);

        handler.execute(command);

        ArgumentCaptor<ExecuteWithdrawalCommand>
                commandCaptor =
                ArgumentCaptor.forClass(
                        ExecuteWithdrawalCommand.class
                );

        ArgumentCaptor<UUID> intentIdCaptor =
                ArgumentCaptor.forClass(UUID.class);

        ArgumentCaptor<UUID> accountIdCaptor =
                ArgumentCaptor.forClass(UUID.class);

        ArgumentCaptor<UUID> atmIdCaptor =
                ArgumentCaptor.forClass(UUID.class);

        verify(
                executeWithdrawalExecutionService
        ).execute(
                commandCaptor.capture(),
                intentIdCaptor.capture(),
                accountIdCaptor.capture(),
                atmIdCaptor.capture()
        );

        assertSame(
                command,
                commandCaptor.getValue()
        );

        assertEquals(
                intentId,
                intentIdCaptor.getValue()
        );

        assertEquals(
                accountId,
                accountIdCaptor.getValue()
        );

        assertEquals(
                atmId,
                atmIdCaptor.getValue()
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
                atmAuthenticationPort,
                withdrawalIntentCommandRepository,
                executeWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenTerminalCodeIsNull() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        null,
                        "atm-secret",
                        "WD-ABC123",
                        "123456",
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenTerminalCodeIsBlank() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        " ",
                        "atm-secret",
                        "WD-ABC123",
                        "123456",
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenCredentialIsNull() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        null,
                        "WD-ABC123",
                        "123456",
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenCredentialIsBlank() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        " ",
                        "WD-ABC123",
                        "123456",
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenWithdrawalReferenceIsNull() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        null,
                        "123456",
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenWithdrawalReferenceIsBlank() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        " ",
                        "123456",
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenWithdrawalCodeIsNull() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        "WD-ABC123",
                        null,
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenWithdrawalCodeIsBlank() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        "WD-ABC123",
                        " ",
                        new BigDecimal("100000")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        "WD-ABC123",
                        "123456",
                        null
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        "WD-ABC123",
                        "123456",
                        BigDecimal.ZERO
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {

        ExecuteWithdrawalCommand invalidCommand =
                new ExecuteWithdrawalCommand(
                        "ATM-HN-001",
                        "atm-secret",
                        "WD-ABC123",
                        "123456",
                        new BigDecimal("-1")
                );

        assertInvalidCommand(invalidCommand);
    }

    @Test
    void shouldThrowWhenIntentDoesNotExist() {

        when(
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                )
        ).thenReturn(atmId);

        when(
                withdrawalIntentCommandRepository
                        .findByWithdrawalReference(
                                command.withdrawalReference()
                        )
        ).thenReturn(
                Optional.empty()
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(
                atmAuthenticationPort
        ).authenticate(
                command.terminalCode(),
                command.credential()
        );

        verify(
                withdrawalIntentCommandRepository
        ).findByWithdrawalReference(
                command.withdrawalReference()
        );

        verifyNoInteractions(
                executeWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenIntentIsNotReady() {

        WithdrawalIntent intent =
                readyIntent();

        intent.setStatus(
                WithdrawalIntentStatus.COMPLETED
        );

        when(
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                )
        ).thenReturn(atmId);

        when(
                withdrawalIntentCommandRepository
                        .findByWithdrawalReference(
                                command.withdrawalReference()
                        )
        ).thenReturn(
                Optional.of(intent)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_READY,
                exception.getErrorCode()
        );

        verify(
                atmAuthenticationPort
        ).authenticate(
                command.terminalCode(),
                command.credential()
        );

        verify(
                withdrawalIntentCommandRepository
        ).findByWithdrawalReference(
                command.withdrawalReference()
        );

        verifyNoInteractions(
                executeWithdrawalExecutionService
        );
    }

    @Test
    void shouldNotFindIntentWhenAtmAuthenticationFails() {

        BusinessException authenticationException =
                new BusinessException(
                        ErrorCode.ATM_AUTHENTICATION_FAILED
                );

        when(
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                )
        ).thenThrow(
                authenticationException
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ATM_AUTHENTICATION_FAILED,
                exception.getErrorCode()
        );

        verify(
                atmAuthenticationPort
        ).authenticate(
                command.terminalCode(),
                command.credential()
        );

        verifyNoInteractions(
                withdrawalIntentCommandRepository,
                executeWithdrawalExecutionService
        );
    }

    @Test
    void shouldNotExecuteWhenIntentValidationFails() {

        WithdrawalIntent intent =
                readyIntent();

        intent.setStatus(
                WithdrawalIntentStatus.CANCELLED
        );

        when(
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                )
        ).thenReturn(atmId);

        when(
                withdrawalIntentCommandRepository
                        .findByWithdrawalReference(
                                command.withdrawalReference()
                        )
        ).thenReturn(
                Optional.of(intent)
        );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                executeWithdrawalExecutionService
        );
    }

    private void assertInvalidCommand(
            ExecuteWithdrawalCommand invalidCommand
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
                atmAuthenticationPort,
                withdrawalIntentCommandRepository,
                executeWithdrawalExecutionService
        );
    }

    private WithdrawalIntent readyIntent() {

        return WithdrawalIntent.builder()
                .id(intentId)
                .withdrawalRequestId(UUID.randomUUID())
                .withdrawalReference("WD-ABC123")
                .userId(UUID.randomUUID())
                .accountId(accountId)
                .holdId(UUID.randomUUID())
                .amount(new BigDecimal("100000"))
                .currency("VND")
                .withdrawalCodeHash("HASHED-CODE")
                .status(WithdrawalIntentStatus.READY)
                .expiresAt(
                        Instant.parse(
                                "2026-08-27T10:10:00Z"
                        )
                )
                .createdAt(
                        Instant.parse(
                                "2026-08-27T10:00:00Z"
                        )
                )
                .version(0L)
                .build();
    }
}
