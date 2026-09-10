package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelWithdrawalIntentServiceTest {

    @Mock
    private WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    @Mock
    private WithdrawalHoldPort withdrawalHoldPort;

    @InjectMocks
    private CancelWithdrawalIntentService
            cancelWithdrawalIntentService;

    private UUID intentId;
    private UUID accountId;
    private UUID holdId;
    private UUID userId;

    private WithdrawalIntent intent;

    @BeforeEach
    void setUp() {
        intentId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        holdId = UUID.randomUUID();
        userId = UUID.randomUUID();

        intent = WithdrawalIntent.builder()
                .id(intentId)
                .userId(userId)
                .accountId(accountId)
                .holdId(holdId)
                .status(WithdrawalIntentStatus.READY)
                .build();
    }

    @Test
    void shouldCancelWithdrawalIntentSuccessfully() {
        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(java.util.Optional.of(intent));

        cancelWithdrawalIntentService.cancel(
                intentId,
                accountId
        );

        assertEquals(
                WithdrawalIntentStatus.CANCELLED,
                intent.getStatus()
        );

        verify(withdrawalHoldPort)
                .releaseHold(holdId);

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verifyNoMoreInteractions(
                withdrawalIntentCommandRepository,
                withdrawalHoldPort
        );
    }

    @Test
    void shouldThrowWhenIntentNotFound() {
        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(java.util.Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentService.cancel(
                                intentId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verifyNoInteractions(withdrawalHoldPort);
    }

    @Test
    void shouldThrowWhenAccountDoesNotMatch() {
        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(java.util.Optional.of(intent));

        UUID anotherAccountId = UUID.randomUUID();

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentService.cancel(
                                intentId,
                                anotherAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verifyNoInteractions(withdrawalHoldPort);
    }

    @Test
    void shouldThrowWhenIntentIsNotReady() {
        intent.setStatus(
                WithdrawalIntentStatus.COMPLETED
        );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(java.util.Optional.of(intent));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentService.cancel(
                                intentId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_READY,
                exception.getErrorCode()
        );

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verifyNoInteractions(withdrawalHoldPort);
    }

    @Test
    void shouldNotReleaseHoldWhenIntentIsAlreadyCancelled() {
        intent.setStatus(
                WithdrawalIntentStatus.CANCELLED
        );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(java.util.Optional.of(intent));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentService.cancel(
                                intentId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_READY,
                exception.getErrorCode()
        );

        verifyNoInteractions(withdrawalHoldPort);
    }

    @Test
    void shouldNotReleaseHoldWhenIntentIsExpired() {
        intent.setStatus(
                WithdrawalIntentStatus.EXPIRED
        );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(java.util.Optional.of(intent));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> cancelWithdrawalIntentService.cancel(
                                intentId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_INTENT_NOT_READY,
                exception.getErrorCode()
        );

        verifyNoInteractions(withdrawalHoldPort);
    }

    @Test
    void shouldNotReleaseHoldWhenIntentIsCompleted() {
        intent.setStatus(
                WithdrawalIntentStatus.COMPLETED
        );

        when(withdrawalIntentCommandRepository.findById(intentId))
                .thenReturn(java.util.Optional.of(intent));

        assertThrows(
                BusinessException.class,
                () -> cancelWithdrawalIntentService.cancel(
                        intentId,
                        accountId
                )
        );

        verify(withdrawalIntentCommandRepository)
                .findById(intentId);

        verifyNoInteractions(withdrawalHoldPort);
    }
}