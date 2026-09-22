package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentResult;
import com.example.ledgercore.card.command.port.outbound.*;
import com.example.ledgercore.card.command.port.outbound.dto.CardAccountInfo;
import com.example.ledgercore.card.command.port.outbound.dto.CardCreditFacilityInfo;
import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.command.repository.CardVaultSecretCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardAuthorization;
import com.example.ledgercore.card.entity.CardVaultSecret;
import com.example.ledgercore.card.enums.*;
import com.example.ledgercore.card.infrastructure.security.CardEncryptionService;
import com.example.ledgercore.card.infrastructure.security.CardPanHashService;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizeCardPaymentHandlerTest {

    @Mock
    private CardCommandRepository cardCommandRepository;
    @Mock
    private CardAuthorizationCommandRepository cardAuthorizationCommandRepository;
    @Mock
    private CardVaultSecretCommandRepository cardVaultSecretCommandRepository;
    @Mock
    private CardAccountPort cardAccountPort;
    @Mock
    private CardCreditFacilityPort cardCreditFacilityPort;
    @Mock
    private AccountAuthorizationHoldPort accountAuthorizationHoldPort;
    @Mock
    private CreditAuthorizationHoldPort creditAuthorizationHoldPort;
    @Mock
    private ProviderAuthenticationPort providerAuthenticationPort;
    @Mock
    private CardPanHashService cardPanHashService;
    @Mock
    private CardEncryptionService cardEncryptionService;

    @InjectMocks
    private AuthorizeCardPaymentHandler handler;

    @Test
    void shouldAuthorizePanPaymentSuccessfullyAndStoreProviderId() {
        UUID providerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID holdId = UUID.randomUUID();

        AuthorizeCardPaymentCommand command = baseCommand();
        Card card = activeDebitCard(cardId, customerId, accountId);
        CardVaultSecret vaultSecret = vaultSecret(cardId);

        when(cardAuthorizationCommandRepository.existsByReference(command.reference())).thenReturn(false);
        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(providerId, "P1", "Provider 1", null));
        when(cardPanHashService.hash(command.pan())).thenReturn("pan-hash");
        when(cardCommandRepository.findByPanHash("pan-hash")).thenReturn(Optional.of(card));
        when(cardVaultSecretCommandRepository.findByCardId(cardId)).thenReturn(Optional.of(vaultSecret));
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedPan(), vaultSecret.getEncryptionVersion())).thenReturn(command.pan());
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedCvv(), vaultSecret.getEncryptionVersion())).thenReturn(command.cvv());
        when(cardAccountPort.getOwnedAccount(customerId, accountId))
                .thenReturn(new CardAccountInfo(accountId, customerId, UUID.randomUUID(), new BigDecimal("500.00"), command.currency()));
        when(accountAuthorizationHoldPort.createHold(eq(accountId), any(UUID.class), eq(command.amount()), eq(command.currency())))
                .thenReturn(new AccountAuthorizationHoldPort.HoldResult(holdId));
        when(cardAuthorizationCommandRepository.save(any(CardAuthorization.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorizeCardPaymentResult result = handler.execute(command);

        assertThat(result.cardId()).isEqualTo(cardId);
        assertThat(result.status()).isEqualTo(CardAuthorizationStatus.AUTHORIZED);

        ArgumentCaptor<CardAuthorization> captor = ArgumentCaptor.forClass(CardAuthorization.class);
        verify(cardAuthorizationCommandRepository).save(captor.capture());
        CardAuthorization saved = captor.getValue();
        assertThat(saved.getProviderId()).isEqualTo(providerId);
        assertThat(saved.getAuthorizationMethod()).isEqualTo(CardAuthorizationMethod.PAN);
        assertThat(saved.getHoldType()).isEqualTo(CardAuthorizationHoldType.ACCOUNT);
        assertThat(saved.getHoldId()).isEqualTo(holdId);
    }

    @Test
    void shouldFailWhenProviderAuthenticationFails() {
        AuthorizeCardPaymentCommand command = baseCommand();
        BusinessException authFailure = new BusinessException(ErrorCode.PROVIDER_AUTHENTICATION_FAILED);

        when(cardAuthorizationCommandRepository.existsByReference(command.reference())).thenReturn(false);
        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenThrow(authFailure);

        assertThatThrownBy(() -> handler.execute(command))
                .isSameAs(authFailure);

        verifyNoInteractions(cardPanHashService, cardCommandRepository, cardVaultSecretCommandRepository);
    }

    @Test
    void shouldFailWhenCardCredentialsInvalid() {
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        AuthorizeCardPaymentCommand command = baseCommand();
        Card card = activeDebitCard(cardId, customerId, accountId);
        CardVaultSecret vaultSecret = vaultSecret(cardId);

        when(cardAuthorizationCommandRepository.existsByReference(command.reference())).thenReturn(false);
        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(UUID.randomUUID(), "P1", "Provider 1", null));
        when(cardPanHashService.hash(command.pan())).thenReturn("pan-hash");
        when(cardCommandRepository.findByPanHash("pan-hash")).thenReturn(Optional.of(card));
        when(cardVaultSecretCommandRepository.findByCardId(cardId)).thenReturn(Optional.of(vaultSecret));
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedPan(), vaultSecret.getEncryptionVersion())).thenReturn(command.pan());
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedCvv(), vaultSecret.getEncryptionVersion())).thenReturn("999");

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_AUTHORIZATION_CVV_INVALID);
    }

    @Test
    void shouldFailWhenCardInactive() {
        UUID cardId = UUID.randomUUID();
        AuthorizeCardPaymentCommand command = baseCommand();

        when(cardAuthorizationCommandRepository.existsByReference(command.reference())).thenReturn(false);
        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(UUID.randomUUID(), "P1", "Provider 1", null));
        when(cardPanHashService.hash(command.pan())).thenReturn("pan-hash");
        when(cardCommandRepository.findByPanHash("pan-hash"))
                .thenReturn(Optional.of(Card.builder().id(cardId).status(CardStatus.BLOCKED).type(CardType.DEBIT).build()));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_NOT_ACTIVE);
    }

    @Test
    void shouldFailWhenCurrencyMismatch() {
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        AuthorizeCardPaymentCommand command = baseCommand();
        Card card = activeDebitCard(cardId, customerId, accountId);
        CardVaultSecret vaultSecret = vaultSecret(cardId);

        when(cardAuthorizationCommandRepository.existsByReference(command.reference())).thenReturn(false);
        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(UUID.randomUUID(), "P1", "Provider 1", null));
        when(cardPanHashService.hash(command.pan())).thenReturn("pan-hash");
        when(cardCommandRepository.findByPanHash("pan-hash")).thenReturn(Optional.of(card));
        when(cardVaultSecretCommandRepository.findByCardId(cardId)).thenReturn(Optional.of(vaultSecret));
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedPan(), vaultSecret.getEncryptionVersion())).thenReturn(command.pan());
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedCvv(), vaultSecret.getEncryptionVersion())).thenReturn(command.cvv());
        when(cardAccountPort.getOwnedAccount(customerId, accountId))
                .thenReturn(new CardAccountInfo(accountId, customerId, UUID.randomUUID(), new BigDecimal("500.00"), Currency.VND));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_AUTHORIZATION_CURRENCY_MISMATCH);
    }

    @Test
    void shouldFailWhenInsufficientBalance() {
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        AuthorizeCardPaymentCommand command = baseCommand();
        Card card = activeDebitCard(cardId, customerId, accountId);
        CardVaultSecret vaultSecret = vaultSecret(cardId);

        when(cardAuthorizationCommandRepository.existsByReference(command.reference())).thenReturn(false);
        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(UUID.randomUUID(), "P1", "Provider 1", null));
        when(cardPanHashService.hash(command.pan())).thenReturn("pan-hash");
        when(cardCommandRepository.findByPanHash("pan-hash")).thenReturn(Optional.of(card));
        when(cardVaultSecretCommandRepository.findByCardId(cardId)).thenReturn(Optional.of(vaultSecret));
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedPan(), vaultSecret.getEncryptionVersion())).thenReturn(command.pan());
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedCvv(), vaultSecret.getEncryptionVersion())).thenReturn(command.cvv());
        when(cardAccountPort.getOwnedAccount(customerId, accountId))
                .thenReturn(new CardAccountInfo(accountId, customerId, UUID.randomUUID(), new BigDecimal("10.00"), command.currency()));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE);
    }

    @Test
    void shouldFailWhenInsufficientCredit() {
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        CardVaultSecret vaultSecret = vaultSecret(cardId);
        AuthorizeCardPaymentCommand command = baseCommand();

        Card card = Card.builder()
                .id(cardId)
                .customerId(customerId)
                .creditFacilityId(facilityId)
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .expiryMonth(command.expiryMonth())
                .expiryYear((short) (2000 + command.expiryYear()))
                .build();

        when(cardAuthorizationCommandRepository.existsByReference(command.reference())).thenReturn(false);
        when(providerAuthenticationPort.authenticate(command.providerClientId(), command.providerCredential()))
                .thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(UUID.randomUUID(), "P1", "Provider 1", null));
        when(cardPanHashService.hash(command.pan())).thenReturn("pan-hash");
        when(cardCommandRepository.findByPanHash("pan-hash")).thenReturn(Optional.of(card));
        when(cardVaultSecretCommandRepository.findByCardId(cardId)).thenReturn(Optional.of(vaultSecret));
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedPan(), vaultSecret.getEncryptionVersion())).thenReturn(command.pan());
        when(cardEncryptionService.decrypt(vaultSecret.getEncryptedCvv(), vaultSecret.getEncryptionVersion())).thenReturn(command.cvv());
        when(cardCreditFacilityPort.getOwnedCreditFacility(customerId, facilityId))
                .thenReturn(new CardCreditFacilityInfo(
                        facilityId,
                        customerId,
                        UUID.randomUUID(),
                        new BigDecimal("5.00"),
                        Currency.USD,
                        CreditFacilityStatus.ACTIVE
                ));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_AUTHORIZATION_INSUFFICIENT_CREDIT);
    }

    private static AuthorizeCardPaymentCommand baseCommand() {
        return new AuthorizeCardPaymentCommand(
                "provider-client",
                "provider-secret",
                "AUTH-REF-1",
                "4111111111111111",
                (short) 12,
                (short) 50,
                "123",
                "merchant-ref",
                new BigDecimal("50.00"),
                Currency.USD
        );
    }

    private static Card activeDebitCard(UUID cardId, UUID customerId, UUID accountId) {
        return Card.builder()
                .id(cardId)
                .customerId(customerId)
                .accountId(accountId)
                .type(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .expiryMonth((short) 12)
                .expiryYear((short) 2050)
                .build();
    }

    private static CardVaultSecret vaultSecret(UUID cardId) {
        CardVaultSecret secret = new CardVaultSecret();
        secret.setCardId(cardId);
        secret.setEncryptedPan("encrypted-pan");
        secret.setEncryptedCvv("encrypted-cvv");
        secret.setEncryptionVersion("v1");
        return secret;
    }
}
