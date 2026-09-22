package com.example.ledgercore.cardtoken.command.handler;

import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenCommand;
import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenResult;
import com.example.ledgercore.cardtoken.command.port.outbound.CardVerificationPort;
import com.example.ledgercore.cardtoken.command.port.outbound.ProviderAuthenticationPort;
import com.example.ledgercore.cardtoken.command.repository.CardTokenCommandRepository;
import com.example.ledgercore.cardtoken.command.service.CardTokenGenerator;
import com.example.ledgercore.cardtoken.entity.CardToken;
import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.provider.enums.ProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvisionCardTokenHandlerTest {

    @Mock
    private CardTokenCommandRepository cardTokenCommandRepository;

    @Mock
    private ProviderAuthenticationPort providerAuthenticationPort;

    @Mock
    private CardVerificationPort cardVerificationPort;

    @Mock
    private CardTokenGenerator cardTokenGenerator;

    private Clock clock;

    private ProvisionCardTokenHandler handler;

    private static final Instant NOW =
            Instant.parse("2026-09-20T12:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        handler = new ProvisionCardTokenHandler(
                cardTokenCommandRepository,
                providerAuthenticationPort,
                cardVerificationPort,
                cardTokenGenerator,
                clock
        );
    }

    @Test
    void shouldProvisionTokenSuccessfully() {
        UUID providerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        ProvisionCardTokenCommand command =
                new ProvisionCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "4111111111111111",
                        (short) 12,
                        (short) 2031,
                        "123",
                        "customer-ref-001"
                );

        ProviderAuthenticationPort.ProviderAuthenticationResult provider =
                new ProviderAuthenticationPort.ProviderAuthenticationResult(
                        providerId,
                        "ACME",
                        "Acme Pay",
                        ProviderType.PSP
                );

        when(providerAuthenticationPort.authenticate(
                command.clientId(),
                command.credential()
        )).thenReturn(provider);

        when(cardVerificationPort.verify(
                command.pan(),
                command.expiryMonth(),
                command.expiryYear(),
                command.cvv()
        )).thenReturn(new CardVerificationPort.CardVerificationResult(cardId));

        when(cardTokenCommandRepository.existsByCardIdAndProviderIdAndProviderCustomerReferenceAndStatus(
                cardId,
                providerId,
                command.providerCustomerReference(),
                CardTokenStatus.ACTIVE
        )).thenReturn(false);

        when(cardTokenGenerator.generate()).thenReturn("ct_new_token");
        when(cardTokenCommandRepository.existsByToken("ct_new_token")).thenReturn(false);

        when(cardTokenCommandRepository.save(any(CardToken.class))).thenAnswer(invocation -> {
            CardToken saved = invocation.getArgument(0);
            saved.setId(tokenId);
            return saved;
        });

        ProvisionCardTokenResult result = handler.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.tokenId()).isEqualTo(tokenId);
        assertThat(result.cardId()).isEqualTo(cardId);
        assertThat(result.token()).isEqualTo("ct_new_token");
        assertThat(result.providerId()).isEqualTo(providerId);
        assertThat(result.providerCustomerReference()).isEqualTo("customer-ref-001");

        verify(providerAuthenticationPort).authenticate(
                "acme-client",
                "secret-123"
        );
        verify(cardVerificationPort).verify(
                "4111111111111111",
                (short) 12,
                (short) 2031,
                "123"
        );
        verify(cardTokenCommandRepository).existsByCardIdAndProviderIdAndProviderCustomerReferenceAndStatus(
                cardId,
                providerId,
                "customer-ref-001",
                CardTokenStatus.ACTIVE
        );
        verify(cardTokenGenerator).generate();
        verify(cardTokenCommandRepository).existsByToken("ct_new_token");
        verify(cardTokenCommandRepository).save(any(CardToken.class));
    }

    @Test
    void shouldRejectDuplicateActiveTokenForSameProviderReference() {
        UUID providerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        ProvisionCardTokenCommand command =
                new ProvisionCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "4111111111111111",
                        (short) 12,
                        (short) 2031,
                        "123",
                        "customer-ref-001"
                );

        when(providerAuthenticationPort.authenticate(
                command.clientId(),
                command.credential()
        )).thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(
                providerId,
                "ACME",
                "Acme Pay",
                ProviderType.PSP
        ));

        when(cardVerificationPort.verify(
                command.pan(),
                command.expiryMonth(),
                command.expiryYear(),
                command.cvv()
        )).thenReturn(new CardVerificationPort.CardVerificationResult(cardId));

        when(cardTokenCommandRepository.existsByCardIdAndProviderIdAndProviderCustomerReferenceAndStatus(
                cardId,
                providerId,
                command.providerCustomerReference(),
                CardTokenStatus.ACTIVE
        )).thenReturn(true);

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_ALREADY_EXISTS);

        verify(cardTokenGenerator, never()).generate();
        verify(cardTokenCommandRepository, never()).save(any(CardToken.class));
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        assertThatThrownBy(() -> handler.execute(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                cardTokenCommandRepository,
                providerAuthenticationPort,
                cardVerificationPort,
                cardTokenGenerator
        );
    }

    @Test
    void shouldThrowWhenClientIdIsBlank() {
        ProvisionCardTokenCommand command =
                new ProvisionCardTokenCommand(
                        "   ",
                        "secret-123",
                        "4111111111111111",
                        (short) 12,
                        (short) 2031,
                        "123",
                        "customer-ref-001"
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_CLIENT_ID_REQUIRED);

        verifyNoInteractions(
                cardTokenCommandRepository,
                providerAuthenticationPort,
                cardVerificationPort,
                cardTokenGenerator
        );
    }

    @Test
    void shouldThrowWhenCustomerReferenceIsBlank() {
        ProvisionCardTokenCommand command =
                new ProvisionCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "4111111111111111",
                        (short) 12,
                        (short) 2031,
                        "123",
                        "   "
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_PROVIDER_CUSTOMER_REFERENCE_REQUIRED);

        verifyNoInteractions(
                cardTokenCommandRepository,
                providerAuthenticationPort,
                cardVerificationPort,
                cardTokenGenerator
        );
    }
}
