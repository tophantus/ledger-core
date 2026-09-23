package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.CaptureCardPaymentCommand;
import com.example.ledgercore.card.command.dto.CaptureCardPaymentResult;
import com.example.ledgercore.card.command.port.outbound.CardProviderAccountPort;
import com.example.ledgercore.card.command.port.outbound.CardProviderTransferPort;
import com.example.ledgercore.card.command.port.outbound.CardReleaseHoldPort;
import com.example.ledgercore.card.command.port.outbound.ProviderAuthenticationPort;
import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.command.repository.CardCaptureCommandRepository;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardAuthorization;
import com.example.ledgercore.card.entity.CardCapture;
import com.example.ledgercore.card.enums.CardAuthorizationHoldType;
import com.example.ledgercore.card.enums.CardAuthorizationStatus;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class CaptureCardPaymentHandlerTest {

    @Mock
    private CardAuthorizationCommandRepository cardAuthorizationCommandRepository;
    @Mock
    private CardCommandRepository cardCommandRepository;
    @Mock
    private CardCaptureCommandRepository cardCaptureCommandRepository;
    @Mock
    private ProviderAuthenticationPort providerAuthenticationPort;
    @Mock
    private CardProviderAccountPort cardProviderAccountPort;
    @Mock
    private CardProviderTransferPort cardProviderTransferPort;
    @Mock
    private CardReleaseHoldPort cardReleaseHoldPort;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-01-01T10:15:30Z"),
            ZoneOffset.UTC
    );

    private CaptureCardPaymentHandler handler;

    @Test
    void shouldCaptureDebitAuthorizationSuccessfully() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID holdId = UUID.randomUUID();
        UUID sourceAccountId = UUID.randomUUID();
        UUID providerAccountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = authorizedAuthorization(
                authorizationId,
                cardId,
                providerId,
                holdId,
                amount
        );
        Card card = debitCard(cardId, sourceAccountId);

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(card));
        when(cardProviderAccountPort.getProviderAccountId(providerId, Currency.USD))
                .thenReturn(providerAccountId);
        when(cardProviderTransferPort.transferFromDebitCard(
                sourceAccountId,
                providerAccountId,
                amount,
                Currency.USD,
                command.reference(),
                command.description()
        )).thenReturn(transactionId);
        when(cardCaptureCommandRepository.save(any(CardCapture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CaptureCardPaymentResult result = handler.execute(command);

        assertThat(result.authorizationStatus())
                .isEqualTo(CardAuthorizationStatus.CAPTURED);
        assertThat(result.authorizationId()).isEqualTo(authorizationId);
        assertThat(result.transactionId()).isEqualTo(transactionId);
        assertThat(result.amount()).isEqualTo(amount);
        assertThat(result.capturedAt()).isEqualTo(Instant.now(clock));

        verify(cardReleaseHoldPort).releaseHold(
                CardAuthorizationHoldType.ACCOUNT,
                holdId
        );
        verify(cardProviderTransferPort, never())
                .transferFromCreditCard(any(), any(), any(), any(), any(), any());

        ArgumentCaptor<CardCapture> captureCaptor =
                ArgumentCaptor.forClass(CardCapture.class);
        verify(cardCaptureCommandRepository).save(captureCaptor.capture());
        CardCapture saved = captureCaptor.getValue();
        assertThat(saved.getAuthorizationId()).isEqualTo(authorizationId);
        assertThat(saved.getTransactionId()).isEqualTo(transactionId);
        assertThat(saved.getCapturedAt()).isEqualTo(Instant.now(clock));
        assertThat(authorization.getStatus()).isEqualTo(CardAuthorizationStatus.CAPTURED);
    }

    @Test
    void shouldCaptureCreditAuthorizationSuccessfully() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID holdId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID providerAccountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = authorizedAuthorization(
                authorizationId,
                cardId,
                providerId,
                holdId,
                new BigDecimal("75.00")
        );
        Card card = creditCard(cardId, facilityId);

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(card));
        when(cardProviderAccountPort.getProviderAccountId(providerId, Currency.USD))
                .thenReturn(providerAccountId);
        when(cardProviderTransferPort.transferFromCreditCard(
                facilityId,
                providerAccountId,
                authorization.getAmount(),
                Currency.USD,
                command.reference(),
                command.description()
        )).thenReturn(transactionId);
        when(cardCaptureCommandRepository.save(any(CardCapture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        handler.execute(command);

        verify(cardProviderTransferPort).transferFromCreditCard(
                facilityId,
                providerAccountId,
                authorization.getAmount(),
                Currency.USD,
                command.reference(),
                command.description()
        );
        verify(cardProviderTransferPort, never())
                .transferFromDebitCard(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldFailWhenProviderMismatch() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID otherProviderId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = authorizedAuthorization(
                authorizationId,
                cardId,
                otherProviderId,
                UUID.randomUUID(),
                new BigDecimal("50.00")
        );

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(debitCard(cardId, UUID.randomUUID())));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_CAPTURE_PROVIDER_MISMATCH);
    }

    @Test
    void shouldFailWhenAuthorizationNotFound() {
        initHandler();
        UUID authorizationId = UUID.randomUUID();
        CaptureCardPaymentCommand command = command(authorizationId);

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(UUID.randomUUID(), "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_NOT_FOUND);
    }

    @Test
    void shouldFailWhenCardNotFound() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = authorizedAuthorization(
                authorizationId,
                cardId,
                providerId,
                UUID.randomUUID(),
                new BigDecimal("50.00")
        );

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_NOT_FOUND);
    }

    @Test
    void shouldFailWhenAuthorizationExpired() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = CardAuthorization.builder()
                .id(authorizationId)
                .cardId(cardId)
                .providerId(providerId)
                .reference("AUTH-REF")
                .amount(new BigDecimal("50.00"))
                .currency(Currency.USD)
                .status(CardAuthorizationStatus.AUTHORIZED)
                .holdId(UUID.randomUUID())
                .holdType(CardAuthorizationHoldType.ACCOUNT)
                .authorizedAt(Instant.parse("2025-12-01T00:00:00Z"))
                .expiresAt(Instant.parse("2025-12-01T00:01:00Z"))
                .createdAt(Instant.parse("2025-12-01T00:00:00Z"))
                .updatedAt(Instant.parse("2025-12-01T00:00:00Z"))
                .build();

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(debitCard(cardId, UUID.randomUUID())));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_CAPTURE_NOT_ALLOWED);
    }

    @Test
    void shouldFailWhenAuthorizationIsNotAuthorizedStatus() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = CardAuthorization.builder()
                .id(authorizationId)
                .cardId(cardId)
                .providerId(providerId)
                .reference("AUTH-REF")
                .amount(new BigDecimal("50.00"))
                .currency(Currency.USD)
                .status(CardAuthorizationStatus.CAPTURED)
                .holdId(UUID.randomUUID())
                .holdType(CardAuthorizationHoldType.ACCOUNT)
                .authorizedAt(Instant.parse("2026-01-01T09:00:00Z"))
                .expiresAt(Instant.parse("2026-01-01T11:00:00Z"))
                .createdAt(Instant.parse("2026-01-01T09:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T09:00:00Z"))
                .build();

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(debitCard(cardId, UUID.randomUUID())));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_CAPTURE_NOT_ALLOWED);
    }

    @Test
    void shouldFailWhenFundingSourceIsInvalidBecauseBothPresent() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = authorizedAuthorization(
                authorizationId,
                cardId,
                providerId,
                UUID.randomUUID(),
                new BigDecimal("50.00")
        );

        Card card = Card.builder()
                .id(cardId)
                .accountId(UUID.randomUUID())
                .creditFacilityId(UUID.randomUUID())
                .build();

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(card));
        when(cardProviderAccountPort.getProviderAccountId(providerId, Currency.USD))
                .thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_CAPTURE_INVALID_FUNDING_SOURCE);
    }

    @Test
    void shouldPropagateFailureAfterHoldReleaseAndNotPersistCapture() {
        initHandler();
        UUID providerId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID sourceAccountId = UUID.randomUUID();
        UUID providerAccountId = UUID.randomUUID();

        CaptureCardPaymentCommand command = command(authorizationId);
        CardAuthorization authorization = authorizedAuthorization(
                authorizationId,
                cardId,
                providerId,
                UUID.randomUUID(),
                new BigDecimal("50.00")
        );
        Card card = debitCard(cardId, sourceAccountId);
        RuntimeException transferFailure = new RuntimeException("transfer failed");

        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider", null));
        when(cardAuthorizationCommandRepository.findByIdForUpdate(authorizationId))
                .thenReturn(Optional.of(authorization));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(card));
        when(cardProviderAccountPort.getProviderAccountId(providerId, Currency.USD))
                .thenReturn(providerAccountId);
        when(cardProviderTransferPort.transferFromDebitCard(
                sourceAccountId,
                providerAccountId,
                authorization.getAmount(),
                authorization.getCurrency(),
                command.reference(),
                command.description()
        )).thenThrow(transferFailure);

        assertThatThrownBy(() -> handler.execute(command))
                .isSameAs(transferFailure);

        verify(cardReleaseHoldPort).releaseHold(
                authorization.getHoldType(),
                authorization.getHoldId()
        );
        verify(cardCaptureCommandRepository, never()).save(any());
    }

    private static CaptureCardPaymentCommand command(UUID authorizationId) {
        return new CaptureCardPaymentCommand(
                "provider-client",
                "provider-secret",
                authorizationId,
                "CAP-REF-1",
                "Card payment capture"
        );
    }

    private void initHandler() {
        handler = new CaptureCardPaymentHandler(
                cardAuthorizationCommandRepository,
                cardCommandRepository,
                cardCaptureCommandRepository,
                providerAuthenticationPort,
                cardProviderAccountPort,
                cardProviderTransferPort,
                cardReleaseHoldPort,
                clock
        );
    }

    private static CardAuthorization authorizedAuthorization(
            UUID authorizationId,
            UUID cardId,
            UUID providerId,
            UUID holdId,
            BigDecimal amount
    ) {
        return CardAuthorization.builder()
                .id(authorizationId)
                .cardId(cardId)
                .providerId(providerId)
                .reference("AUTH-REF")
                .amount(amount)
                .currency(Currency.USD)
                .status(CardAuthorizationStatus.AUTHORIZED)
                .holdId(holdId)
                .holdType(CardAuthorizationHoldType.ACCOUNT)
                .authorizedAt(Instant.parse("2026-01-01T09:00:00Z"))
                .expiresAt(Instant.parse("2026-01-01T11:00:00Z"))
                .createdAt(Instant.parse("2026-01-01T09:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T09:00:00Z"))
                .build();
    }

    private static Card debitCard(
            UUID cardId,
            UUID accountId
    ) {
        return Card.builder()
                .id(cardId)
                .accountId(accountId)
                .build();
    }

    private static Card creditCard(
            UUID cardId,
            UUID creditFacilityId
    ) {
        return Card.builder()
                .id(cardId)
                .creditFacilityId(creditFacilityId)
                .build();
    }
}
