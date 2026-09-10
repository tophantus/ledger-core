package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.CancelWithdrawalIntentCommand;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelWithdrawalIntentHandlerTest {

    @Mock
    private WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    @Mock
    private CancelWithdrawalIntentService
            cancelWithdrawalIntentService;

    @InjectMocks
    private CancelWithdrawalIntentHandler
            cancelWithdrawalIntentHandler;

    private UUID userId;
    private UUID intentId;
    private UUID accountId;

    private WithdrawalIntent intent;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        intentId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        intent = WithdrawalIntent.builder()
                .id(intentId)
                .userId(userId)
                .accountId(accountId)
                .status(WithdrawalIntentStatus.READY)
                .build();
    }

    @Test
    void shouldCancelWithdrawalIntentSuccessfully() {
        CancelWithdrawalIntentCommand command =
                new CancelWithdrawalIntentCommand(
                        userId,
                        intentId
                );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(Optional.of(intent));

        cancelWithdrawalIntentHandler.execute(command);

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verify(cancelWithdrawalIntentService)
                .cancel(
                        intentId,
                        accountId
                );

        verifyNoMoreInteractions(
                withdrawalIntentCommandRepository,
                cancelWithdrawalIntentService
        );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentHandler.execute(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalIntentCommandRepository,
                cancelWithdrawalIntentService
        );
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        CancelWithdrawalIntentCommand command =
                new CancelWithdrawalIntentCommand(
                        null,
                        intentId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentHandler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalIntentCommandRepository,
                cancelWithdrawalIntentService
        );
    }

    @Test
    void shouldThrowWhenIntentIdIsNull() {
        CancelWithdrawalIntentCommand command =
                new CancelWithdrawalIntentCommand(
                        userId,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentHandler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalIntentCommandRepository,
                cancelWithdrawalIntentService
        );
    }

    @Test
    void shouldThrowWhenIntentNotFound() {
        CancelWithdrawalIntentCommand command =
                new CancelWithdrawalIntentCommand(
                        userId,
                        intentId
                );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentHandler.execute(command)
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verifyNoInteractions(cancelWithdrawalIntentService);
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnIntent() {
        UUID anotherUserId = UUID.randomUUID();

        CancelWithdrawalIntentCommand command =
                new CancelWithdrawalIntentCommand(
                        anotherUserId,
                        intentId
                );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(Optional.of(intent));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentHandler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verifyNoInteractions(cancelWithdrawalIntentService);
    }

    @Test
    void shouldPassIntentAccountIdToCancellationService() {
        CancelWithdrawalIntentCommand command =
                new CancelWithdrawalIntentCommand(
                        userId,
                        intentId
                );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(Optional.of(intent));

        cancelWithdrawalIntentHandler.execute(command);

        verify(cancelWithdrawalIntentService)
                .cancel(
                        intent.getId(),
                        intent.getAccountId()
                );
    }
}