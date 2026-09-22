package com.example.ledgercore.cardtoken.command.handler;

import com.example.ledgercore.cardtoken.command.dto.SuspendCardTokenCommand;
import com.example.ledgercore.cardtoken.command.port.outbound.ProviderAuthenticationPort;
import com.example.ledgercore.cardtoken.command.repository.CardTokenCommandRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuspendCardTokenHandlerTest {

    @Mock
    private CardTokenCommandRepository cardTokenCommandRepository;

    @Mock
    private ProviderAuthenticationPort providerAuthenticationPort;

    private Clock clock;

    private SuspendCardTokenHandler handler;

    private static final Instant NOW =
            Instant.parse("2026-09-20T12:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        handler = new SuspendCardTokenHandler(
                cardTokenCommandRepository,
                providerAuthenticationPort,
                clock
        );
    }

    @Test
    void shouldSuspendActiveCardTokenSuccessfully() {
        UUID providerId = UUID.randomUUID();
        CardToken token = CardToken.builder()
                .id(UUID.randomUUID())
                .token("tok-active")
                .cardId(UUID.randomUUID())
                .providerId(providerId)
                .providerCustomerReference("customer-ref-001")
                .status(CardTokenStatus.ACTIVE)
                .createdAt(NOW.minusSeconds(60))
                .updatedAt(NOW.minusSeconds(60))
                .build();

        SuspendCardTokenCommand command =
                new SuspendCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "tok-active"
                );

        when(providerAuthenticationPort.authenticate(
                "acme-client",
                "secret-123"
        )).thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(
                providerId,
                "ACME",
                "Acme Pay",
                ProviderType.PSP
        ));

        when(cardTokenCommandRepository.findByTokenAndProviderId(
                "tok-active",
                providerId
        )).thenReturn(Optional.of(token));

        when(cardTokenCommandRepository.save(any(CardToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        handler.execute(command);

        assertThat(token.getStatus()).isEqualTo(CardTokenStatus.SUSPENDED);
        assertThat(token.getSuspendedAt()).isEqualTo(NOW);

        verify(providerAuthenticationPort).authenticate(
                "acme-client",
                "secret-123"
        );
        verify(cardTokenCommandRepository).findByTokenAndProviderId(
                "tok-active",
                providerId
        );
        verify(cardTokenCommandRepository).save(token);
    }

    @Test
    void shouldThrowWhenCardTokenDoesNotExist() {
        UUID providerId = UUID.randomUUID();

        SuspendCardTokenCommand command =
                new SuspendCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "missing-token"
                );

        when(providerAuthenticationPort.authenticate(
                "acme-client",
                "secret-123"
        )).thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(
                providerId,
                "ACME",
                "Acme Pay",
                ProviderType.PSP
        ));

        when(cardTokenCommandRepository.findByTokenAndProviderId(
                "missing-token",
                providerId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_NOT_FOUND);

        verify(cardTokenCommandRepository, never()).save(any(CardToken.class));
    }

    @Test
    void shouldThrowWhenTokenIsNotActive() {
        UUID providerId = UUID.randomUUID();
        CardToken token = CardToken.builder()
                .id(UUID.randomUUID())
                .token("tok-revoked")
                .cardId(UUID.randomUUID())
                .providerId(providerId)
                .providerCustomerReference("customer-ref-001")
                .status(CardTokenStatus.REVOKED)
                .createdAt(NOW.minusSeconds(60))
                .updatedAt(NOW.minusSeconds(60))
                .build();

        SuspendCardTokenCommand command =
                new SuspendCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "tok-revoked"
                );

        when(providerAuthenticationPort.authenticate(
                "acme-client",
                "secret-123"
        )).thenReturn(new ProviderAuthenticationPort.ProviderAuthenticationResult(
                providerId,
                "ACME",
                "Acme Pay",
                ProviderType.PSP
        ));

        when(cardTokenCommandRepository.findByTokenAndProviderId(
                "tok-revoked",
                providerId
        )).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_INVALID_STATUS);

        verify(cardTokenCommandRepository, never()).save(any(CardToken.class));
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        assertThatThrownBy(() -> handler.execute(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(cardTokenCommandRepository, providerAuthenticationPort);
    }

    @Test
    void shouldThrowWhenTokenIsBlank() {
        SuspendCardTokenCommand command =
                new SuspendCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "   "
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_REQUIRED);

        verifyNoInteractions(cardTokenCommandRepository, providerAuthenticationPort);
    }
}
