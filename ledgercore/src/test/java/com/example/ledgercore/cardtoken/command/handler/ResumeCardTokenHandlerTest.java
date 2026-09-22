package com.example.ledgercore.cardtoken.command.handler;

import com.example.ledgercore.cardtoken.command.dto.ResumeCardTokenCommand;
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
class ResumeCardTokenHandlerTest {

    @Mock
    private CardTokenCommandRepository cardTokenCommandRepository;

    @Mock
    private ProviderAuthenticationPort providerAuthenticationPort;

    private Clock clock;

    private ResumeCardTokenHandler handler;

    private static final Instant NOW =
            Instant.parse("2026-09-20T12:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        handler = new ResumeCardTokenHandler(
                cardTokenCommandRepository,
                providerAuthenticationPort,
                clock
        );
    }

    @Test
    void shouldResumeSuspendedCardTokenSuccessfully() {
        UUID providerId = UUID.randomUUID();
        CardToken token = CardToken.builder()
                .id(UUID.randomUUID())
                .token("tok-suspended")
                .cardId(UUID.randomUUID())
                .providerId(providerId)
                .providerCustomerReference("customer-ref-001")
                .status(CardTokenStatus.SUSPENDED)
                .suspendedAt(NOW.minusSeconds(30))
                .createdAt(NOW.minusSeconds(120))
                .updatedAt(NOW.minusSeconds(120))
                .build();

        ResumeCardTokenCommand command =
                new ResumeCardTokenCommand(
                        "acme-client",
                        "secret-123",
                        "tok-suspended"
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
                "tok-suspended",
                providerId
        )).thenReturn(Optional.of(token));

        when(cardTokenCommandRepository.save(any(CardToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        handler.execute(command);

        assertThat(token.getStatus()).isEqualTo(CardTokenStatus.ACTIVE);
        assertThat(token.getSuspendedAt()).isNull();

        verify(providerAuthenticationPort).authenticate(
                "acme-client",
                "secret-123"
        );
        verify(cardTokenCommandRepository).findByTokenAndProviderId(
                "tok-suspended",
                providerId
        );
        verify(cardTokenCommandRepository).save(token);
    }

    @Test
    void shouldThrowWhenCardTokenDoesNotExist() {
        UUID providerId = UUID.randomUUID();

        ResumeCardTokenCommand command =
                new ResumeCardTokenCommand(
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
    void shouldThrowWhenTokenIsNotSuspended() {
        UUID providerId = UUID.randomUUID();
        CardToken token = CardToken.builder()
                .id(UUID.randomUUID())
                .token("tok-active")
                .cardId(UUID.randomUUID())
                .providerId(providerId)
                .providerCustomerReference("customer-ref-001")
                .status(CardTokenStatus.ACTIVE)
                .createdAt(NOW.minusSeconds(120))
                .updatedAt(NOW.minusSeconds(120))
                .build();

        ResumeCardTokenCommand command =
                new ResumeCardTokenCommand(
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
}
