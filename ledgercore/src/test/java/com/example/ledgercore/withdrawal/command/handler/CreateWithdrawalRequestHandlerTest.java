package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalRequestCommand;
import com.example.ledgercore.withdrawal.command.dto.WithdrawalRequestResponse;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountInfo;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalOtpPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalRequestCommandRepository;
import com.example.ledgercore.withdrawal.config.WithdrawalRequestProperties;
import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWithdrawalRequestHandlerTest {

    @Mock
    private WithdrawalRequestCommandRepository
            withdrawalRequestRepository;

    @Mock
    private WithdrawalAccountPort
            withdrawalAccountPort;

    @Mock
    private WithdrawalOtpPort
            withdrawalOtpPort;

    @Mock
    private WithdrawalRequestProperties
            withdrawalRequestProperties;

    private Clock clock;

    private CreateWithdrawalRequestHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID requestId;

    private static final Instant NOW =
            Instant.parse("2026-08-27T10:00:00Z");

    private static final Duration EXPIRATION =
            Duration.ofMinutes(5);

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        handler = new CreateWithdrawalRequestHandler(
                withdrawalRequestRepository,
                withdrawalAccountPort,
                withdrawalOtpPort,
                clock,
                withdrawalRequestProperties
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        requestId = UUID.randomUUID();
    }

    @Test
    void shouldCreateWithdrawalRequestSuccessfully() {

        when(withdrawalRequestProperties.getExpiration())
                .thenReturn(EXPIRATION);

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );

        mockSaveRequest();

        WithdrawalRequestResponse response =
                handler.execute(command);

        assertNotNull(response);

        assertEquals(
                requestId,
                response.requestId()
        );

        assertEquals(
                accountId,
                response.accountId()
        );

        assertEquals(
                new BigDecimal("100000"),
                response.amount()
        );

        assertEquals(
                "VND",
                response.currency()
        );

        assertEquals(
                WithdrawalRequestStatus.PENDING,
                response.status()
        );

        assertEquals(
                NOW.plus(EXPIRATION),
                response.expiresAt()
        );

        verify(withdrawalAccountPort)
                .getWithdrawalInfo(accountId);

        verify(withdrawalRequestProperties)
                .getExpiration();

        verify(withdrawalRequestRepository)
                .save(any(WithdrawalRequest.class));

        verify(withdrawalOtpPort)
                .sendConfirmationOtp(
                        userId,
                        requestId
                );
    }

    @Test
    void shouldCreatePendingWithdrawalRequest() {

        when(withdrawalRequestProperties.getExpiration())
                .thenReturn(EXPIRATION);

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );

        mockSaveRequest();

        handler.execute(command);

        ArgumentCaptor<WithdrawalRequest> captor =
                ArgumentCaptor.forClass(
                        WithdrawalRequest.class
                );

        verify(withdrawalRequestRepository)
                .save(captor.capture());

        WithdrawalRequest request =
                captor.getValue();

        assertEquals(
                userId,
                request.getUserId()
        );

        assertEquals(
                accountId,
                request.getAccountId()
        );

        assertEquals(
                new BigDecimal("100000"),
                request.getAmount()
        );

        assertEquals(
                "VND",
                request.getCurrency()
        );

        assertEquals(
                WithdrawalRequestStatus.PENDING,
                request.getStatus()
        );

        assertEquals(
                NOW.plus(EXPIRATION),
                request.getExpiresAt()
        );

        assertEquals(
                NOW,
                request.getCreatedAt()
        );
    }

    @Test
    void shouldCalculateExpirationFromClock() {

        when(withdrawalRequestProperties.getExpiration())
                .thenReturn(EXPIRATION);

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );

        mockSaveRequest();

        handler.execute(command);

        ArgumentCaptor<WithdrawalRequest> captor =
                ArgumentCaptor.forClass(
                        WithdrawalRequest.class
                );

        verify(withdrawalRequestRepository)
                .save(captor.capture());

        WithdrawalRequest request =
                captor.getValue();

        assertEquals(
                NOW.plus(EXPIRATION),
                request.getExpiresAt()
        );

        verify(withdrawalRequestProperties)
                .getExpiration();
    }

    @Test
    void shouldSendOtpAfterSavingWithdrawalRequest() {

        when(withdrawalRequestProperties.getExpiration())
                .thenReturn(EXPIRATION);

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );

        mockSaveRequest();

        var inOrder = inOrder(
                withdrawalRequestRepository,
                withdrawalOtpPort
        );

        handler.execute(command);

        inOrder.verify(
                withdrawalRequestRepository
        ).save(any(WithdrawalRequest.class));

        inOrder.verify(
                withdrawalOtpPort
        ).sendConfirmationOtp(
                userId,
                requestId
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {

        CreateWithdrawalRequestCommand command =
                new CreateWithdrawalRequestCommand(
                        userId,
                        accountId,
                        null,
                        "VND"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalRequestRepository,
                withdrawalOtpPort,
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {

        CreateWithdrawalRequestCommand command =
                command(
                        "0",
                        "VND"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalRequestRepository,
                withdrawalOtpPort,
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {

        CreateWithdrawalRequestCommand command =
                command(
                        "-100",
                        "VND"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalRequestRepository,
                withdrawalOtpPort,
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnAccount() {

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "VND"
                );

        UUID anotherUserId =
                UUID.randomUUID();

        mockWithdrawalAccount(
                anotherUserId,
                "VND",
                "1000000"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(withdrawalAccountPort)
                .getWithdrawalInfo(accountId);

        verifyNoInteractions(
                withdrawalRequestRepository,
                withdrawalOtpPort,
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "USD"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(withdrawalAccountPort)
                .getWithdrawalInfo(accountId);

        verifyNoInteractions(
                withdrawalRequestRepository,
                withdrawalOtpPort,
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldThrowWhenAvailableBalanceIsInsufficient() {

        CreateWithdrawalRequestCommand command =
                command(
                        "1000000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "999999"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(withdrawalAccountPort)
                .getWithdrawalInfo(accountId);

        verifyNoInteractions(
                withdrawalRequestRepository,
                withdrawalOtpPort,
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldAllowWithdrawalWhenAvailableBalanceEqualsAmount() {

        when(withdrawalRequestProperties.getExpiration())
                .thenReturn(EXPIRATION);

        CreateWithdrawalRequestCommand command =
                command(
                        "1000000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );

        mockSaveRequest();

        WithdrawalRequestResponse response =
                handler.execute(command);

        assertNotNull(response);

        assertEquals(
                new BigDecimal("1000000"),
                response.amount()
        );

        assertEquals(
                NOW.plus(EXPIRATION),
                response.expiresAt()
        );

        verify(withdrawalRequestRepository)
                .save(any(WithdrawalRequest.class));

        verify(withdrawalOtpPort)
                .sendConfirmationOtp(
                        userId,
                        requestId
                );
    }

    @Test
    void shouldUseWithdrawalAccountInfoForValidation() {

        CreateWithdrawalRequestCommand command =
                command(
                        "500000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "100000"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(withdrawalAccountPort)
                .getWithdrawalInfo(accountId);

        verify(withdrawalRequestRepository, never())
                .save(any());

        verifyNoInteractions(
                withdrawalOtpPort,
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldNotSendOtpWhenRequestCannotBeCreated() {

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "50000"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(withdrawalAccountPort)
                .getWithdrawalInfo(accountId);

        verify(withdrawalRequestRepository, never())
                .save(any());

        verify(withdrawalOtpPort, never())
                .sendConfirmationOtp(
                        any(),
                        any()
                );

        verifyNoInteractions(
                withdrawalRequestProperties
        );
    }

    @Test
    void shouldUseConfiguredExpiration() {

        Duration customExpiration =
                Duration.ofMinutes(10);

        when(withdrawalRequestProperties.getExpiration())
                .thenReturn(customExpiration);

        CreateWithdrawalRequestCommand command =
                command(
                        "100000",
                        "VND"
                );

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );

        mockSaveRequest();

        WithdrawalRequestResponse response =
                handler.execute(command);

        assertEquals(
                NOW.plus(customExpiration),
                response.expiresAt()
        );

        verify(withdrawalRequestProperties)
                .getExpiration();
    }

    private void mockWithdrawalAccount(
            UUID accountUserId,
            String currency,
            String availableBalance
    ) {

        when(withdrawalAccountPort.getWithdrawalInfo(
                accountId
        )).thenReturn(
                new WithdrawalAccountInfo(
                        accountId,
                        accountUserId,
                        currency,
                        new BigDecimal(availableBalance)
                )
        );
    }

    private void mockSaveRequest() {

        doAnswer(invocation -> {

            WithdrawalRequest request =
                    invocation.getArgument(0);

            request.setId(requestId);

            return request;

        }).when(withdrawalRequestRepository)
                .save(any(WithdrawalRequest.class));
    }

    private CreateWithdrawalRequestCommand command(
            String amount,
            String currency
    ) {

        return new CreateWithdrawalRequestCommand(
                userId,
                accountId,
                new BigDecimal(amount),
                currency
        );
    }
}
