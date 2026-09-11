package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestResponse;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalLookupCodeCommand;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalLookupCodeResponse;
import com.example.ledgercore.withdrawal.command.port.inbound.CreateWithdrawalLookupCodeUseCase;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmWithdrawalExecutionServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-09-10T15:00:00Z");

    private static final UUID USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final UUID ANOTHER_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000099");

    private static final UUID REQUEST_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static final UUID ACCOUNT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static final UUID HOLD_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000005");

    private static final BigDecimal AMOUNT =
            new BigDecimal("1000000");

    private static final Currency CURRENCY = Currency.VND;

    private static final String WITHDRAWAL_REFERENCE =
            "WD-20260910-000001";

    private static final String WITHDRAWAL_CODE =
            "123456";

    private static final String WITHDRAWAL_CODE_HASH =
            "hashed-code";

    private static final String LOOKUP_CODE =
            "12345678";

    private static final Duration INTENT_EXPIRATION =
            Duration.ofMinutes(10);

    @Mock
    private WithdrawalRequestCommandRepository
            withdrawalRequestCommandRepository;

    @Mock
    private WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    @Mock
    private CreateWithdrawalLookupCodeUseCase
            createWithdrawalLookupCodeUseCase;

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

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new ConfirmWithdrawalExecutionService(
                withdrawalRequestCommandRepository,
                withdrawalIntentCommandRepository,
                createWithdrawalLookupCodeUseCase,
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalNotificationPort,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                clock
        );
    }

    @Test
    void shouldConfirmWithdrawalSuccessfully() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getId())
                .thenReturn(REQUEST_ID);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.getAccountId())
                .thenReturn(ACCOUNT_ID);

        when(request.getAmount())
                .thenReturn(AMOUNT);

        when(request.getCurrency())
                .thenReturn(CURRENCY);

        when(request.isPending())
                .thenReturn(true);

        when(request.isExpired(NOW))
                .thenReturn(false);

        WithdrawalAccountInfo account =
                new WithdrawalAccountInfo(
                        ACCOUNT_ID,
                        USER_ID,
                        CURRENCY,
                        new BigDecimal("5000000")
                );

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        when(withdrawalAccountPort.getWithdrawalInfo(ACCOUNT_ID))
                .thenReturn(account);

        when(withdrawalReferenceGenerator.generate())
                .thenReturn(WITHDRAWAL_REFERENCE);

        when(withdrawalCodeGenerator.generate())
                .thenReturn(WITHDRAWAL_CODE);

        when(withdrawalCodeHasher.hash(WITHDRAWAL_CODE))
                .thenReturn(WITHDRAWAL_CODE_HASH);

        when(withdrawalIntentProperties.getExpiration())
                .thenReturn(INTENT_EXPIRATION);

        when(withdrawalHoldPort.createHold(
                any(UUID.class),
                eq(ACCOUNT_ID),
                eq(AMOUNT),
                eq(CURRENCY)
        )).thenReturn(HOLD_ID);

        when(createWithdrawalLookupCodeUseCase.execute(
                any(CreateWithdrawalLookupCodeCommand.class)
        )).thenReturn(
                new CreateWithdrawalLookupCodeResponse(
                        LOOKUP_CODE
                )
        );

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.requestId())
                .isEqualTo(REQUEST_ID);

        assertThat(response.amount())
                .isEqualTo(AMOUNT.toPlainString());

        assertThat(response.currency())
                .isEqualTo(CURRENCY);

        assertThat(response.intentExpiresAt())
                .isEqualTo(
                        NOW.plus(INTENT_EXPIRATION)
                );

        ArgumentCaptor<WithdrawalIntent> intentCaptor =
                ArgumentCaptor.forClass(
                        WithdrawalIntent.class
                );

        verify(withdrawalIntentCommandRepository)
                .save(intentCaptor.capture());

        WithdrawalIntent intent =
                intentCaptor.getValue();

        assertThat(intent.getId())
                .isNotNull();

        assertThat(intent.getWithdrawalRequestId())
                .isEqualTo(REQUEST_ID);

        assertThat(intent.getWithdrawalReference())
                .isEqualTo(WITHDRAWAL_REFERENCE);

        assertThat(intent.getUserId())
                .isEqualTo(USER_ID);

        assertThat(intent.getAccountId())
                .isEqualTo(ACCOUNT_ID);

        assertThat(intent.getHoldId())
                .isEqualTo(HOLD_ID);

        assertThat(intent.getAmount())
                .isEqualByComparingTo(AMOUNT);

        assertThat(intent.getCurrency())
                .isEqualTo(CURRENCY);

        assertThat(intent.getWithdrawalCodeHash())
                .isEqualTo(WITHDRAWAL_CODE_HASH);

        assertThat(intent.getStatus())
                .isEqualTo(WithdrawalIntentStatus.READY);

        assertThat(intent.getCreatedAt())
                .isEqualTo(NOW);

        assertThat(intent.getExpiresAt())
                .isEqualTo(
                        NOW.plus(INTENT_EXPIRATION)
                );

        verify(withdrawalHoldPort)
                .createHold(
                        any(UUID.class),
                        eq(ACCOUNT_ID),
                        eq(AMOUNT),
                        eq(CURRENCY)
                );

        verify(withdrawalReferenceGenerator)
                .generate();

        verify(withdrawalCodeGenerator)
                .generate();

        verify(withdrawalCodeHasher)
                .hash(WITHDRAWAL_CODE);

        verify(createWithdrawalLookupCodeUseCase)
                .execute(
                        any(CreateWithdrawalLookupCodeCommand.class)
                );

        verify(withdrawalNotificationPort)
                .sendWithdrawalCode(
                        eq(intent.getId()),
                        eq(USER_ID),
                        eq(LOOKUP_CODE),
                        eq(WITHDRAWAL_CODE),
                        eq(AMOUNT),
                        eq(CURRENCY),
                        eq(NOW.plus(INTENT_EXPIRATION))
                );

        verify(request)
                .confirm(NOW);
    }

    @Test
    void shouldThrowWhenWithdrawalRequestNotFound() {
        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND
                );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                createWithdrawalLookupCodeUseCase,
                withdrawalNotificationPort
        );
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnWithdrawalRequest() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() ->
                service.execute(
                        ANOTHER_USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ACCESS_DENIED
                );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                createWithdrawalLookupCodeUseCase,
                withdrawalNotificationPort
        );
    }

    @Test
    void shouldThrowWhenWithdrawalRequestIsNotPending() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.isPending())
                .thenReturn(false);

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() ->
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.WITHDRAWAL_REQUEST_NOT_PENDING
                );

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                createWithdrawalLookupCodeUseCase,
                withdrawalNotificationPort
        );
    }

    @Test
    void shouldExpireAndThrowWhenWithdrawalRequestIsExpired() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.isPending())
                .thenReturn(true);

        when(request.isExpired(NOW))
                .thenReturn(true);

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() ->
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.WITHDRAWAL_REQUEST_EXPIRED
                );

        verify(request)
                .expire();

        verifyNoInteractions(
                withdrawalAccountPort,
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                createWithdrawalLookupCodeUseCase,
                withdrawalNotificationPort
        );
    }

    @Test
    void shouldThrowWhenAccountDoesNotBelongToUser() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.isPending())
                .thenReturn(true);

        when(request.isExpired(NOW))
                .thenReturn(false);

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        WithdrawalAccountInfo account =
                new WithdrawalAccountInfo(
                        ACCOUNT_ID,
                        ANOTHER_USER_ID,
                        CURRENCY,
                        new BigDecimal("5000000")
                );

        when(withdrawalAccountPort.getWithdrawalInfo(ACCOUNT_ID))
                .thenReturn(account);

        assertThatThrownBy(() ->
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ACCESS_DENIED
                );

        verifyNoInteractions(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                createWithdrawalLookupCodeUseCase,
                withdrawalNotificationPort
        );
    }

    @Test
    void shouldThrowWhenAccountCurrencyDoesNotMatch() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.isPending())
                .thenReturn(true);

        when(request.isExpired(NOW))
                .thenReturn(false);

        when(request.getCurrency())
                .thenReturn(CURRENCY);

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        WithdrawalAccountInfo account =
                new WithdrawalAccountInfo(
                        ACCOUNT_ID,
                        USER_ID,
                        Currency.USD,
                        new BigDecimal("5000000")
                );

        when(withdrawalAccountPort.getWithdrawalInfo(ACCOUNT_ID))
                .thenReturn(account);

        assertThatThrownBy(() ->
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ACCOUNT_CURRENCY_MISMATCH
                );

        verifyNoInteractions(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                createWithdrawalLookupCodeUseCase,
                withdrawalNotificationPort
        );
    }

    @Test
    void shouldThrowWhenAvailableBalanceIsInsufficient() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.isPending())
                .thenReturn(true);

        when(request.isExpired(NOW))
                .thenReturn(false);

        when(request.getCurrency())
                .thenReturn(CURRENCY);

        when(request.getAmount())
                .thenReturn(AMOUNT);

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        WithdrawalAccountInfo account =
                new WithdrawalAccountInfo(
                        ACCOUNT_ID,
                        USER_ID,
                        CURRENCY,
                        new BigDecimal("999999")
                );

        when(withdrawalAccountPort.getWithdrawalInfo(ACCOUNT_ID))
                .thenReturn(account);

        assertThatThrownBy(() ->
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
                );

        verifyNoInteractions(
                withdrawalHoldPort,
                withdrawalIntentCommandRepository,
                withdrawalReferenceGenerator,
                withdrawalCodeGenerator,
                withdrawalCodeHasher,
                withdrawalIntentProperties,
                createWithdrawalLookupCodeUseCase,
                withdrawalNotificationPort
        );
    }

    @Test
    void shouldAllowWithdrawalWhenAvailableBalanceEqualsAmount() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getId())
                .thenReturn(REQUEST_ID);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.getAccountId())
                .thenReturn(ACCOUNT_ID);

        when(request.getAmount())
                .thenReturn(AMOUNT);

        when(request.getCurrency())
                .thenReturn(CURRENCY);

        when(request.isPending())
                .thenReturn(true);

        when(request.isExpired(NOW))
                .thenReturn(false);

        WithdrawalAccountInfo account =
                new WithdrawalAccountInfo(
                        ACCOUNT_ID,
                        USER_ID,
                        CURRENCY,
                        AMOUNT
                );

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        when(withdrawalAccountPort.getWithdrawalInfo(ACCOUNT_ID))
                .thenReturn(account);

        when(withdrawalReferenceGenerator.generate())
                .thenReturn(WITHDRAWAL_REFERENCE);

        when(withdrawalCodeGenerator.generate())
                .thenReturn(WITHDRAWAL_CODE);

        when(withdrawalCodeHasher.hash(WITHDRAWAL_CODE))
                .thenReturn(WITHDRAWAL_CODE_HASH);

        when(withdrawalIntentProperties.getExpiration())
                .thenReturn(INTENT_EXPIRATION);

        when(withdrawalHoldPort.createHold(
                any(UUID.class),
                eq(ACCOUNT_ID),
                eq(AMOUNT),
                eq(CURRENCY)
        )).thenReturn(HOLD_ID);

        when(createWithdrawalLookupCodeUseCase.execute(
                any(CreateWithdrawalLookupCodeCommand.class)
        )).thenReturn(
                new CreateWithdrawalLookupCodeResponse(
                        LOOKUP_CODE
                )
        );

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.requestId())
                .isEqualTo(REQUEST_ID);

        verify(withdrawalHoldPort)
                .createHold(
                        any(UUID.class),
                        eq(ACCOUNT_ID),
                        eq(AMOUNT),
                        eq(CURRENCY)
                );

        verify(withdrawalIntentCommandRepository)
                .save(any(WithdrawalIntent.class));

        verify(withdrawalNotificationPort)
                .sendWithdrawalCode(
                        any(UUID.class),
                        eq(USER_ID),
                        eq(LOOKUP_CODE),
                        eq(WITHDRAWAL_CODE),
                        eq(AMOUNT),
                        eq(CURRENCY),
                        eq(NOW.plus(INTENT_EXPIRATION))
                );
    }

    @Test
    void shouldAcceptCurrencyCaseInsensitive() {
        WithdrawalRequest request =
                mock(WithdrawalRequest.class);

        when(request.getId())
                .thenReturn(REQUEST_ID);

        when(request.getUserId())
                .thenReturn(USER_ID);

        when(request.getAccountId())
                .thenReturn(ACCOUNT_ID);

        when(request.getAmount())
                .thenReturn(AMOUNT);

        when(request.getCurrency())
                .thenReturn(Currency.VND);

        when(request.isPending())
                .thenReturn(true);

        when(request.isExpired(NOW))
                .thenReturn(false);

        WithdrawalAccountInfo account =
                new WithdrawalAccountInfo(
                        ACCOUNT_ID,
                        USER_ID,
                        Currency.VND,
                        new BigDecimal("5000000")
                );

        when(withdrawalRequestCommandRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        when(withdrawalAccountPort.getWithdrawalInfo(ACCOUNT_ID))
                .thenReturn(account);

        when(withdrawalReferenceGenerator.generate())
                .thenReturn(WITHDRAWAL_REFERENCE);

        when(withdrawalCodeGenerator.generate())
                .thenReturn(WITHDRAWAL_CODE);

        when(withdrawalCodeHasher.hash(WITHDRAWAL_CODE))
                .thenReturn(WITHDRAWAL_CODE_HASH);

        when(withdrawalIntentProperties.getExpiration())
                .thenReturn(INTENT_EXPIRATION);

        when(withdrawalHoldPort.createHold(
                any(UUID.class),
                eq(ACCOUNT_ID),
                eq(AMOUNT),
                eq(Currency.VND)
        )).thenReturn(HOLD_ID);

        when(createWithdrawalLookupCodeUseCase.execute(
                any(CreateWithdrawalLookupCodeCommand.class)
        )).thenReturn(
                new CreateWithdrawalLookupCodeResponse(
                        LOOKUP_CODE
                )
        );

        ConfirmWithdrawalRequestResponse response =
                service.execute(
                        USER_ID,
                        REQUEST_ID,
                        ACCOUNT_ID
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.currency())
                .isEqualTo(Currency.VND);

        verify(withdrawalHoldPort)
                .createHold(
                        any(UUID.class),
                        eq(ACCOUNT_ID),
                        eq(AMOUNT),
                        eq(Currency.VND)
                );

        ArgumentCaptor<WithdrawalIntent> intentCaptor =
                ArgumentCaptor.forClass(
                        WithdrawalIntent.class
                );

        verify(withdrawalIntentCommandRepository)
                .save(intentCaptor.capture());

        assertThat(intentCaptor.getValue().getCurrency())
                .isEqualTo(Currency.VND);
    }
}
