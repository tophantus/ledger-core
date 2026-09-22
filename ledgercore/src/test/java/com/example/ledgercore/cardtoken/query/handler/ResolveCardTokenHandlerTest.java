package com.example.ledgercore.cardtoken.query.handler;

import com.example.ledgercore.cardtoken.entity.CardToken;
import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenQuery;
import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenResult;
import com.example.ledgercore.cardtoken.query.repository.CardTokenQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResolveCardTokenHandlerTest {

    @Mock
    private CardTokenQueryRepository cardTokenQueryRepository;

    private ResolveCardTokenHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ResolveCardTokenHandler(cardTokenQueryRepository);
    }

    @Test
    void shouldResolveActiveCardTokenSuccessfully() {
        UUID providerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        ResolveCardTokenQuery query =
                new ResolveCardTokenQuery(
                        providerId,
                        "tok-active"
                );

        CardToken token = CardToken.builder()
                .id(UUID.randomUUID())
                .token("tok-active")
                .cardId(cardId)
                .providerId(providerId)
                .providerCustomerReference("customer-ref-001")
                .status(CardTokenStatus.ACTIVE)
                .createdAt(Instant.parse("2026-09-20T10:00:00Z"))
                .updatedAt(Instant.parse("2026-09-20T10:00:00Z"))
                .build();

        when(cardTokenQueryRepository.findByTokenAndProviderIdAndStatus(
                "tok-active",
                providerId,
                CardTokenStatus.ACTIVE
        )).thenReturn(Optional.of(token));

        ResolveCardTokenResult result = handler.execute(query);

        assertThat(result).isNotNull();
        assertThat(result.cardId()).isEqualTo(cardId);

        verify(cardTokenQueryRepository).findByTokenAndProviderIdAndStatus(
                "tok-active",
                providerId,
                CardTokenStatus.ACTIVE
        );
    }

    @Test
    void shouldThrowWhenTokenDoesNotExistForProvider() {
        UUID providerId = UUID.randomUUID();

        ResolveCardTokenQuery query =
                new ResolveCardTokenQuery(
                        providerId,
                        "missing-token"
                );

        when(cardTokenQueryRepository.findByTokenAndProviderIdAndStatus(
                "missing-token",
                providerId,
                CardTokenStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_NOT_FOUND);

        verify(cardTokenQueryRepository).findByTokenAndProviderIdAndStatus(
                "missing-token",
                providerId,
                CardTokenStatus.ACTIVE
        );
    }

    @Test
    void shouldOnlyResolveActiveTokens() {
        UUID providerId = UUID.randomUUID();

        ResolveCardTokenQuery query =
                new ResolveCardTokenQuery(
                        providerId,
                        "tok-suspended"
                );

        when(cardTokenQueryRepository.findByTokenAndProviderIdAndStatus(
                "tok-suspended",
                providerId,
                CardTokenStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_NOT_FOUND);

        verify(cardTokenQueryRepository).findByTokenAndProviderIdAndStatus(
                "tok-suspended",
                providerId,
                CardTokenStatus.ACTIVE
        );
    }

    @Test
    void shouldThrowWhenQueryIsNull() {
        assertThatThrownBy(() -> handler.execute(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(cardTokenQueryRepository);
    }

    @Test
    void shouldThrowWhenProviderIdIsNull() {
        ResolveCardTokenQuery query =
                new ResolveCardTokenQuery(
                        null,
                        "tok-active"
                );

        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(cardTokenQueryRepository);
    }

    @Test
    void shouldThrowWhenTokenIsBlank() {
        ResolveCardTokenQuery query =
                new ResolveCardTokenQuery(
                        UUID.randomUUID(),
                        "   "
                );

        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CARD_TOKEN_REQUIRED);

        verifyNoInteractions(cardTokenQueryRepository);
    }
}
