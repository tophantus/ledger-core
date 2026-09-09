package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestResponse;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountInfo;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalNotificationPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalRequestCommandRepository;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeGenerator;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeHasher;
import com.example.ledgercore.withdrawal.command.service.WithdrawalReferenceGenerator;
import com.example.ledgercore.withdrawal.config.WithdrawalIntentProperties;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmWithdrawalExecutionServiceTest {

    @Mock
    private WithdrawalRequestCommandRepository
            withdrawalRequestCommandRepository;

    @Mock
    private WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    @Mock
    private WithdrawalAccountPort
            withdrawalAccountPort;

    @Mock
    private WithdrawalHoldPort
            withdrawalHoldPort;

    @Mock
    private WithdrawalNotificationPort
            withdrawalNotificationPort;

    @Mock
    private WithdrawalReferenceGenerator
            withdrawalReferenceGenerator;

    @Mock
    private WithdrawalCodeGenerator
            withdrawalCodeGenerator;

    @Mock
    private WithdrawalCodeHasher
            withdrawalCodeHasher;

    @Mock
    private WithdrawalIntentProperties
            withdrawalIntentProperties;

    private Clock clock;

    private ConfirmWithdrawalExecutionService service;

    private UUID userId;
    private UUID anotherUserId;
    private UUID requestId;
    private UUID accountId;
    private UUID intentId;
    private UUID holdId;

    private static final Instant NOW =
            Instant.parse("2026-08-27T10:00:00Z");

    private static final Duration INTENT_EXPIRATION =
            Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new ConfirmWithdrawalExecutionService(
                withdrawalRequestCommandRepository,
                withdrawalIntentCommandRepository,
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                clock
        );

        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        intentId = UUID.randomUUID();
        holdId = UUID.randomUUID();
    }

    @Test
    void shouldConfirmWithdrawalSuccessfully() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        ArgumentCaptor<WithdrawalIntent> intentCaptor =
                ArgumentCaptor.forClass(
                        WithdrawalIntent.class
                );

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        userId,
                        requestId,
                        accountId
                );

        verify(
                withdrawalIntentCommandRepository
        ).save(intentCaptor.capture());

        WithdrawalIntent intent =
                intentCaptor.getValue();

        assertNotNull(response);

        assertEquals(
                requestId,
                response.requestId()
        );

        assertEquals(
                WithdrawalRequestStatus.CONFIRMED,
                response.requestStatus()
        );

        assertEquals(
                intent.getId(),
                response.intentId()
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
                "VND",
                response.currency()
        );

        assertEquals(
                NOW.plus(INTENT_EXPIRATION),
                response.intentExpiresAt()
        );

        assertEquals(
                WithdrawalRequestStatus.CONFIRMED,
                request.getStatus()
        );

        assertNotNull(intent.getId());

        assertEquals(
                requestId,
                intent.getWithdrawalRequestId()
        );

        assertEquals(
                userId,
                intent.getUserId()
        );

        assertEquals(
                accountId,
                intent.getAccountId()
        );

        assertEquals(
                holdId,
                intent.getHoldId()
        );

        verify(
                withdrawalRequestCommandRepository
        ).findById(requestId);

        verify(
                withdrawalAccountPort
        ).getWithdrawalInfo(accountId);

        verify(
                withdrawalHoldPort
        ).createHold(
                eq(intent.getId()),
                eq(accountId),
                eq(new BigDecimal("100000")),
                eq("VND")
        );

        verify(
                withdrawalNotificationPort
        ).sendWithdrawalCode(
                eq(intent.getId()),
                eq(userId),
                eq("WD-ABC123"),
                eq("123456"),
                eq(new BigDecimal("100000")),
                eq("VND"),
                eq(NOW.plus(INTENT_EXPIRATION))
        );
    }


    @Test
    void shouldCreateIntentWithCorrectData() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        ArgumentCaptor<WithdrawalIntent> captor =
                ArgumentCaptor.forClass(
                        WithdrawalIntent.class
                );

        service.execute(
                userId,
                requestId,
                accountId
        );

        verify(
                withdrawalIntentCommandRepository
        ).save(captor.capture());

        WithdrawalIntent intent =
                captor.getValue();

        assertNotNull(intent.getId());

        assertEquals(
                requestId,
                intent.getWithdrawalRequestId()
        );

        assertEquals(
                "WD-ABC123",
                intent.getWithdrawalReference()
        );

        assertEquals(
                userId,
                intent.getUserId()
        );

        assertEquals(
                accountId,
                intent.getAccountId()
        );

        assertEquals(
                holdId,
                intent.getHoldId()
        );

        assertEquals(
                new BigDecimal("100000"),
                intent.getAmount()
        );

        assertEquals(
                "VND",
                intent.getCurrency()
        );

        assertEquals(
                "HASHED-CODE",
                intent.getWithdrawalCodeHash()
        );

        assertEquals(
                WithdrawalIntentStatus.READY,
                intent.getStatus()
        );

        assertEquals(
                NOW.plus(INTENT_EXPIRATION),
                intent.getExpiresAt()
        );

        assertEquals(
                NOW,
                intent.getCreatedAt()
        );
    }

    @Test
    void shouldGenerateIntentIdBeforeCreatingHold() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();

        when(
                withdrawalHoldPort.createHold(
                        any(UUID.class),
                        eq(accountId),
                        any(BigDecimal.class),
                        eq("VND")
                )
        ).thenAnswer(invocation -> {

            UUID generatedIntentId =
                    invocation.getArgument(0);

            intentId = generatedIntentId;

            return holdId;
        });

        service.execute(
                userId,
                requestId,
                accountId
        );

        verify(
                withdrawalHoldPort
        ).createHold(
                any(UUID.class),
                eq(accountId),
                eq(new BigDecimal("100000")),
                eq("VND")
        );
    }

    @Test
    void shouldPassGeneratedIntentIdToCreateHold() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        ArgumentCaptor<UUID> intentIdCaptor =
                ArgumentCaptor.forClass(UUID.class);

        service.execute(
                userId,
                requestId,
                accountId
        );

        verify(
                withdrawalHoldPort
        ).createHold(
                intentIdCaptor.capture(),
                eq(accountId),
                eq(new BigDecimal("100000")),
                eq("VND")
        );

        UUID generatedIntentId =
                intentIdCaptor.getValue();

        assertNotNull(generatedIntentId);

        ArgumentCaptor<WithdrawalIntent> intentCaptor =
                ArgumentCaptor.forClass(
                        WithdrawalIntent.class
                );

        verify(
                withdrawalIntentCommandRepository
        ).save(intentCaptor.capture());

        assertEquals(
                generatedIntentId,
                intentCaptor.getValue().getId()
        );
    }

    @Test
    void shouldHashGeneratedWithdrawalCode() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        service.execute(
                userId,
                requestId,
                accountId
        );

        verify(
                withdrawalCodeHasher
        ).hash("123456");
    }

    @Test
    void shouldCreateHoldBeforeSavingIntent() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        InOrder inOrder = inOrder(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository
        );

        service.execute(
                userId,
                requestId,
                accountId
        );

        inOrder.verify(
                withdrawalHoldPort
        ).createHold(
                any(UUID.class),
                eq(accountId),
                eq(new BigDecimal("100000")),
                eq("VND")
        );

        inOrder.verify(
                withdrawalIntentCommandRepository
        ).save(any(WithdrawalIntent.class));
    }

    @Test
    void shouldSaveIntentBeforeConfirmingRequest() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        InOrder inOrder = inOrder(
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort
        );

        service.execute(
                userId,
                requestId,
                accountId
        );

        inOrder.verify(
                withdrawalIntentCommandRepository
        ).save(any(WithdrawalIntent.class));

        inOrder.verify(
                withdrawalNotificationPort
        ).sendWithdrawalCode(
                any(UUID.class),
                eq(userId),
                eq("WD-ABC123"),
                eq("123456"),
                eq(new BigDecimal("100000")),
                eq("VND"),
                eq(NOW.plus(INTENT_EXPIRATION))
        );
    }

    @Test
    void shouldSendNotificationWithPlaintextWithdrawalCode() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        service.execute(
                userId,
                requestId,
                accountId
        );

        verify(
                withdrawalNotificationPort
        ).sendWithdrawalCode(
                any(UUID.class),
                eq(userId),
                eq("WD-ABC123"),
                eq("123456"),
                eq(new BigDecimal("100000")),
                eq("VND"),
                eq(NOW.plus(INTENT_EXPIRATION))
        );
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
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties
        );
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnRequest() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        request.setUserId(anotherUserId);

        mockRequest(request);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties
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

        mockRequest(request);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_NOT_PENDING,
                exception.getErrorCode()
        );

        verifyNoInteractionsAfterRequestValidation();
    }

    @Test
    void shouldThrowWhenRequestIsExpired() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.minusSeconds(1)
                );

        mockRequest(request);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_EXPIRED,
                exception.getErrorCode()
        );

        assertEquals(
                WithdrawalRequestStatus.EXPIRED,
                request.getStatus()
        );

        verifyNoInteractionsAfterRequestValidation();
    }

    @Test
    void shouldThrowWhenRequestExpiresExactlyAtNow() {

        WithdrawalRequest request =
                pendingRequest(NOW);

        mockRequest(request);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.WITHDRAWAL_REQUEST_EXPIRED,
                exception.getErrorCode()
        );

        assertEquals(
                WithdrawalRequestStatus.EXPIRED,
                request.getStatus()
        );

        verifyNoInteractionsAfterRequestValidation();
    }

    @Test
    void shouldThrowWhenAccountDoesNotBelongToRequestUser() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);

        mockWithdrawalAccount(
                anotherUserId,
                "VND",
                "1000000"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(
                withdrawalAccountPort
        ).getWithdrawalInfo(accountId);

        verifyNoInteractions(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties
        );
    }

    @Test
    void shouldThrowWhenAccountCurrencyDoesNotMatch() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);

        mockWithdrawalAccount(
                userId,
                "USD",
                "1000000"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(
                withdrawalAccountPort
        ).getWithdrawalInfo(accountId);

        verifyNoInteractions(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties
        );
    }

    @Test
    void shouldAcceptCurrencyIgnoringCase() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);

        mockWithdrawalAccount(
                userId,
                "vnd",
                "1000000"
        );

        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        userId,
                        requestId,
                        accountId
                );

        assertNotNull(response);

        verify(
                withdrawalHoldPort
        ).createHold(
                any(UUID.class),
                eq(accountId),
                eq(new BigDecimal("100000")),
                eq("VND")
        );
    }

    @Test
    void shouldThrowWhenAvailableBalanceIsInsufficient() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);

        mockWithdrawalAccount(
                userId,
                "VND",
                "99999"
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                requestId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(
                withdrawalAccountPort
        ).getWithdrawalInfo(accountId);

        verifyNoInteractions(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties
        );
    }

    @Test
    void shouldAllowWhenAvailableBalanceEqualsAmount() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);

        mockWithdrawalAccount(
                userId,
                "VND",
                "100000"
        );

        mockIntentProperties();
        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        userId,
                        requestId,
                        accountId
                );

        assertNotNull(response);

        assertEquals(
                new BigDecimal("100000"),
                response.amount()
        );

        verify(
                withdrawalHoldPort
        ).createHold(
                any(UUID.class),
                eq(accountId),
                eq(new BigDecimal("100000")),
                eq("VND")
        );
    }

    @Test
    void shouldUseCurrentClockForIntentExpiration() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();

        Duration customExpiration =
                Duration.ofMinutes(20);

        when(
                withdrawalIntentProperties.getExpiration()
        ).thenReturn(customExpiration);

        mockReference();
        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        userId,
                        requestId,
                        accountId
                );

        assertEquals(
                NOW.plus(customExpiration),
                response.intentExpiresAt()
        );
    }

    @Test
    void shouldUseGeneratedReference() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();

        when(
                withdrawalReferenceGenerator.generate()
        ).thenReturn("WD-CUSTOM");

        mockWithdrawalCode();
        mockCodeHash();
        mockHold();

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        userId,
                        requestId,
                        accountId
                );

        assertEquals(
                "WD-CUSTOM",
                response.withdrawalReference()
        );
    }

    @Test
    void shouldUseGeneratedWithdrawalCode() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);
        mockValidAccount();
        mockIntentProperties();
        mockReference();

        when(
                withdrawalCodeGenerator.generate()
        ).thenReturn("654321");

        when(
                withdrawalCodeHasher.hash("654321")
        ).thenReturn("HASHED-654321");

        mockHold();

        service.execute(
                userId,
                requestId,
                accountId
        );

        ArgumentCaptor<WithdrawalIntent> captor =
                ArgumentCaptor.forClass(
                        WithdrawalIntent.class
                );

        verify(
                withdrawalIntentCommandRepository
        ).save(captor.capture());

        assertEquals(
                "HASHED-654321",
                captor.getValue()
                        .getWithdrawalCodeHash()
        );

        verify(
                withdrawalNotificationPort
        ).sendWithdrawalCode(
                any(UUID.class),
                eq(userId),
                eq("WD-ABC123"),
                eq("654321"),
                eq(new BigDecimal("100000")),
                eq("VND"),
                eq(NOW.plus(INTENT_EXPIRATION))
        );
    }

    @Test
    void shouldNotCreateHoldWhenAccountValidationFails() {

        WithdrawalRequest request =
                pendingRequest(
                        NOW.plusSeconds(300)
                );

        mockRequest(request);

        mockWithdrawalAccount(
                userId,
                "VND",
                "1"
        );

        assertThrows(
                BusinessException.class,
                () -> service.execute(
                        userId,
                        requestId,
                        accountId
                )
        );

        verifyNoInteractions(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort
        );
    }

    private void mockRequest(
            WithdrawalRequest request
    ) {

        when(
                withdrawalRequestCommandRepository
                        .findById(requestId)
        ).thenReturn(
                Optional.of(request)
        );
    }

    private void mockValidAccount() {

        mockWithdrawalAccount(
                userId,
                "VND",
                "1000000"
        );
    }

    private void mockWithdrawalAccount(
            UUID accountUserId,
            String currency,
            String availableBalance
    ) {

        when(
                withdrawalAccountPort.getWithdrawalInfo(
                        accountId
                )
        ).thenReturn(
                new WithdrawalAccountInfo(
                        accountId,
                        accountUserId,
                        currency,
                        new BigDecimal(availableBalance)
                )
        );
    }

    private void mockIntentProperties() {

        when(
                withdrawalIntentProperties.getExpiration()
        ).thenReturn(
                INTENT_EXPIRATION
        );
    }

    private void mockReference() {

        when(
                withdrawalReferenceGenerator.generate()
        ).thenReturn(
                "WD-ABC123"
        );
    }

    private void mockWithdrawalCode() {

        when(
                withdrawalCodeGenerator.generate()
        ).thenReturn(
                "123456"
        );
    }

    private void mockCodeHash() {

        when(
                withdrawalCodeHasher.hash("123456")
        ).thenReturn(
                "HASHED-CODE"
        );
    }

    private void mockHold() {

        when(
                withdrawalHoldPort.createHold(
                        any(UUID.class),
                        eq(accountId),
                        eq(new BigDecimal("100000")),
                        eq("VND")
                )
        ).thenReturn(
                holdId
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
                .currency("VND")
                .status(WithdrawalRequestStatus.PENDING)
                .expiresAt(expiresAt)
                .createdAt(NOW)
                .version(0L)
                .build();
    }

    private void verifyNoInteractionsAfterRequestValidation() {

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties
        );
    }
}
