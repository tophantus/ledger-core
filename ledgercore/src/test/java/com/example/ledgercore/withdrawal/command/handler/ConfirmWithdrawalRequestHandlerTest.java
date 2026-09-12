package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestCommand;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestResponse;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalOtpPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalRequestCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmWithdrawalRequestHandlerTest {

    @Mock
    private WithdrawalRequestCommandRepository
            withdrawalRequestCommandRepository;

    @Mock
    private WithdrawalOtpPort
            withdrawalOtpPort;

    @Mock
    private ConfirmWithdrawalExecutionService
            confirmWithdrawalExecutionService;

    private Clock clock;

    private ConfirmWithdrawalRequestHandler handler;

    private UUID userId;
    private UUID anotherUserId;
    private UUID requestId;
    private UUID accountId;

    private static final Instant NOW =
            Instant.parse("2026-08-27T10:00:00Z");

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        handler = new ConfirmWithdrawalRequestHandler(
                withdrawalRequestCommandRepository,
                withdrawalOtpPort,
                confirmWithdrawalExecutionService,
                clock
        );

        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldConfirmWithdrawalRequestSuccessfully() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        ConfirmWithdrawalRequestResponse expected =
                new ConfirmWithdrawalRequestResponse(
                        requestId,
                        WithdrawalRequestStatus.CONFIRMED,
                        UUID.randomUUID(),
                        "WD-ABC123",
                        new BigDecimal("100000").toPlainString(),
                        Currency.VND,
                        NOW.plusSeconds(600)
                );

        when(
                confirmWithdrawalExecutionService.execute(
                        userId,
                        requestId,
                        accountId
                )
        ).thenReturn(expected);

        ConfirmWithdrawalRequestCommand command =
                new ConfirmWithdrawalRequestCommand(
                        userId,
                        requestId,
                        "123456"
                );

        ConfirmWithdrawalRequestResponse actual =
                handler.execute(command);

        assertSame(
                expected,
                actual
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verify(withdrawalOtpPort)
                .verifyConfirmationOtp(
                        userId,
                        requestId,
                        "123456"
                );

        verify(
                confirmWithdrawalExecutionService
        ).execute(
                userId,
                requestId,
                accountId
        );
    }

    @Test
    void shouldVerifyOtpBeforeDelegatingExecution() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        ConfirmWithdrawalRequestResponse response =
                mock(
                        ConfirmWithdrawalRequestResponse.class
                );

        when(
                confirmWithdrawalExecutionService.execute(
                        userId,
                        requestId,
                        accountId
                )
        ).thenReturn(response);

        var inOrder = inOrder(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );

        handler.execute(
                command()
        );

        inOrder.verify(withdrawalOtpPort)
                .verifyConfirmationOtp(
                        userId,
                        requestId,
                        "123456"
                );

        inOrder.verify(
                confirmWithdrawalExecutionService
        ).execute(
                userId,
                requestId,
                accountId
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
                withdrawalRequestCommandRepository,
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenRequestAmountScaleExceedsCurrencyScale() {
        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        request.setAmount(
                new BigDecimal("100.1")
        );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.INVALID_CURRENCY_AMOUNT,
                exception.getErrorCode()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenRequestAmountIsNotWithdrawalDenomination() {
        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        request.setAmount(
                new BigDecimal("100001")
        );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {

        ConfirmWithdrawalRequestCommand command =
                new ConfirmWithdrawalRequestCommand(
                        null,
                        requestId,
                        "123456"
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenRequestIdIsNull() {

        ConfirmWithdrawalRequestCommand command =
                new ConfirmWithdrawalRequestCommand(
                        userId,
                        null,
                        "123456"
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenOtpIsNull() {

        ConfirmWithdrawalRequestCommand command =
                new ConfirmWithdrawalRequestCommand(
                        userId,
                        requestId,
                        null
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenOtpIsBlank() {

        ConfirmWithdrawalRequestCommand command =
                new ConfirmWithdrawalRequestCommand(
                        userId,
                        requestId,
                        "   "
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenRequestDoesNotExist() {

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.empty()
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnRequest() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        request.setUserId(anotherUserId);

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenRequestIsNotPending() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        request.setStatus(
                WithdrawalRequestStatus.CONFIRMED
        );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_NOT_PENDING,
                exception.getErrorCode()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenRequestIsCancelled() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        request.setStatus(
                WithdrawalRequestStatus.CANCELLED
        );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_NOT_PENDING,
                exception.getErrorCode()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenRequestIsExpired() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.minusSeconds(1)
                );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_EXPIRED,
                exception.getErrorCode()
        );

        assertEquals(
                WithdrawalRequestStatus.EXPIRED,
                request.getStatus()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldThrowWhenRequestExpiresExactlyAtNow() {

        WithdrawalRequest request =
                pendingRequest(NOW);

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command())
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_EXPIRED,
                exception.getErrorCode()
        );

        assertEquals(
                WithdrawalRequestStatus.EXPIRED,
                request.getStatus()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verifyNoInteractions(
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldNotDelegateWhenOtpVerificationFails() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        doThrow(
                new BusinessException(
                        ErrorCode.INVALID_REQUEST
                )
        ).when(withdrawalOtpPort)
                .verifyConfirmationOtp(
                        userId,
                        requestId,
                        "123456"
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command())
        );

        verify(withdrawalOtpPort)
                .verifyConfirmationOtp(
                        userId,
                        requestId,
                        "123456"
                );

        verifyNoInteractions(
                confirmWithdrawalExecutionService
        );
    }

    @Test
    void shouldDelegateCorrectRequestAndAccount() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );

        ConfirmWithdrawalRequestResponse response =
                mock(
                        ConfirmWithdrawalRequestResponse.class
                );

        when(
                confirmWithdrawalExecutionService.execute(
                        userId,
                        requestId,
                        accountId
                )
        ).thenReturn(response);

        ConfirmWithdrawalRequestResponse actual =
                handler.execute(command());

        assertSame(
                response,
                actual
        );

        verify(
                confirmWithdrawalExecutionService
        ).execute(
                userId,
                requestId,
                accountId
        );
    }

    private ConfirmWithdrawalRequestCommand command() {

        return new ConfirmWithdrawalRequestCommand(
                userId,
                requestId,
                "123456"
        );
    }

    private WithdrawalRequest pendingRequest(
            Instant expiresAt
    ) {

        return WithdrawalRequest.builder()
                .id(requestId)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("100000"))
                .currency(Currency.VND)
                .status(WithdrawalRequestStatus.PENDING)
                .expiresAt(expiresAt)
                .createdAt(NOW)
                .version(0L)
                .build();
    }

    private void assertInvalidRequest(
            ConfirmWithdrawalRequestCommand command
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

        verifyNoInteractions(
                withdrawalRequestCommandRepository,
                withdrawalOtpPort,
                confirmWithdrawalExecutionService
        );
    }
}
