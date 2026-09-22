package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenResult;
import com.example.ledgercore.card.command.port.outbound.*;
import com.example.ledgercore.card.command.port.outbound.dto.CardAccountInfo;
import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardAuthorization;
import com.example.ledgercore.card.enums.*;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizeCardPaymentByTokenHandlerTest {

    @Mock
    private CardCommandRepository cardCommandRepository;

    @Mock
    private CardAuthorizationCommandRepository cardAuthorizationCommandRepository;

    @Mock
    private ProviderAuthenticationPort providerAuthenticationPort;

    @Mock
    private CardTokenPort cardTokenPort;

    @Mock
    private CardAccountPort cardAccountPort;

    @Mock
    private CardCreditFacilityPort cardCreditFacilityPort;

    @Mock
    private AccountAuthorizationHoldPort accountAuthorizationHoldPort;

    @Mock
    private CreditAuthorizationHoldPort creditAuthorizationHoldPort;

    @InjectMocks
    private AuthorizeCardPaymentByTokenHandler handler;

    @Test
    void shouldAuthorizeDebitCardByTokenSuccessfully() {
        UUID providerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID holdId = UUID.randomUUID();

        AuthorizeCardPaymentByTokenCommand command =
                new AuthorizeCardPaymentByTokenCommand(
                        "AUTH-REF-1",
                        "provider-client",
                        "provider-secret",
                        "token-123",
                        "merchant-ref",
                        new BigDecimal("50.00"),
                        Currency.USD
                );

        Card card = Card.builder()
                .id(cardId)
                .customerId(customerId)
                .accountId(accountId)
                .type(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardAuthorizationCommandRepository.existsByReference(
                command.reference()
        )).thenReturn(false);
        when(providerAuthenticationPort.authenticate(
                command.providerClientId(),
                command.providerCredential()
        )).thenReturn(
                new ProviderAuthenticationPort.ProviderAuthenticationResult(
                        providerId,
                        "P1",
                        "Provider 1",
                        null
                )
        );
        when(cardTokenPort.resolve(providerId, command.token()))
                .thenReturn(new CardTokenPort.CardTokenInfo(cardId));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(card));
        when(cardAccountPort.getOwnedAccount(customerId, accountId))
                .thenReturn(new CardAccountInfo(
                        accountId,
                        customerId,
                        UUID.randomUUID(),
                        new BigDecimal("100.00"),
                        Currency.USD
                ));
        when(accountAuthorizationHoldPort.createHold(
                eq(accountId),
                any(UUID.class),
                eq(command.amount()),
                eq(command.currency())
        )).thenReturn(
                new AccountAuthorizationHoldPort.HoldResult(holdId)
        );
        when(cardAuthorizationCommandRepository.save(any(CardAuthorization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthorizeCardPaymentByTokenResult result =
                handler.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.cardId()).isEqualTo(cardId);
        assertThat(result.reference()).isEqualTo(command.reference());
        assertThat(result.status())
                .isEqualTo(CardAuthorizationStatus.AUTHORIZED);
        assertThat(result.amount()).isEqualTo(command.amount().toPlainString());
        assertThat(result.currency()).isEqualTo(command.currency());

        ArgumentCaptor<CardAuthorization> authorizationCaptor =
                ArgumentCaptor.forClass(CardAuthorization.class);
        verify(cardAuthorizationCommandRepository)
                .save(authorizationCaptor.capture());

        CardAuthorization saved = authorizationCaptor.getValue();
        assertThat(saved.getAuthorizationMethod())
                .isEqualTo(CardAuthorizationMethod.TOKEN);
        assertThat(saved.getHoldType())
                .isEqualTo(CardAuthorizationHoldType.ACCOUNT);
        assertThat(saved.getHoldId()).isEqualTo(holdId);

        verifyNoInteractions(cardCreditFacilityPort);
        verifyNoInteractions(creditAuthorizationHoldPort);
    }

    @Test
    void shouldFailWhenReferenceAlreadyExists() {
        AuthorizeCardPaymentByTokenCommand command =
                new AuthorizeCardPaymentByTokenCommand(
                        "AUTH-REF-1",
                        "provider-client",
                        "provider-secret",
                        "token-123",
                        "merchant-ref",
                        new BigDecimal("50.00"),
                        Currency.USD
                );

        when(cardAuthorizationCommandRepository.existsByReference(
                command.reference()
        )).thenReturn(true);

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(
                        ErrorCode.CARD_AUTHORIZATION_REFERENCE_ALREADY_EXISTS
                );

        verifyNoInteractions(providerAuthenticationPort);
        verifyNoInteractions(cardTokenPort);
    }

    @Test
    void shouldFailWhenResolvedCardIsNotActive() {
        UUID providerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        AuthorizeCardPaymentByTokenCommand command =
                new AuthorizeCardPaymentByTokenCommand(
                        "AUTH-REF-1",
                        "provider-client",
                        "provider-secret",
                        "token-123",
                        "merchant-ref",
                        new BigDecimal("50.00"),
                        Currency.USD
                );

        Card card = Card.builder()
                .id(cardId)
                .type(CardType.DEBIT)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardAuthorizationCommandRepository.existsByReference(
                command.reference()
        )).thenReturn(false);
        when(providerAuthenticationPort.authenticate(
                command.providerClientId(),
                command.providerCredential()
        )).thenReturn(
                new ProviderAuthenticationPort.ProviderAuthenticationResult(
                        providerId,
                        "P1",
                        "Provider 1",
                        null
                )
        );
        when(cardTokenPort.resolve(providerId, command.token()))
                .thenReturn(new CardTokenPort.CardTokenInfo(cardId));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_NOT_ACTIVE);

        verifyNoInteractions(accountAuthorizationHoldPort);
        verifyNoInteractions(creditAuthorizationHoldPort);
    }

    @Test
    void shouldFailWhenDebitCurrencyMismatch() {
        UUID providerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        AuthorizeCardPaymentByTokenCommand command =
                new AuthorizeCardPaymentByTokenCommand(
                        "AUTH-REF-1",
                        "provider-client",
                        "provider-secret",
                        "token-123",
                        "merchant-ref",
                        new BigDecimal("50.00"),
                        Currency.USD
                );

        Card card = Card.builder()
                .id(cardId)
                .customerId(customerId)
                .accountId(accountId)
                .type(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardAuthorizationCommandRepository.existsByReference(
                command.reference()
        )).thenReturn(false);
        when(providerAuthenticationPort.authenticate(
                command.providerClientId(),
                command.providerCredential()
        )).thenReturn(
                new ProviderAuthenticationPort.ProviderAuthenticationResult(
                        providerId,
                        "P1",
                        "Provider 1",
                        null
                )
        );
        when(cardTokenPort.resolve(providerId, command.token()))
                .thenReturn(new CardTokenPort.CardTokenInfo(cardId));
        when(cardCommandRepository.findById(cardId))
                .thenReturn(Optional.of(card));
        when(cardAccountPort.getOwnedAccount(customerId, accountId))
                .thenReturn(new CardAccountInfo(
                        accountId,
                        customerId,
                        UUID.randomUUID(),
                        new BigDecimal("100.00"),
                        Currency.VND
                ));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CARD_AUTHORIZATION_CURRENCY_MISMATCH);

        verifyNoInteractions(accountAuthorizationHoldPort);
        verify(cardAuthorizationCommandRepository, never())
                .save(any(CardAuthorization.class));
    }
}
