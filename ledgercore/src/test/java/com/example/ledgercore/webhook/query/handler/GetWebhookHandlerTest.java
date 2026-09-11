package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.port.outbound.WebhookAccountOwnerPort;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.mapper.WebhookResponseMapper;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWebhookHandlerTest {

    @Mock
    private WebhookAccountOwnerPort webhookAccountOwnerPort;

    @Mock
    private WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    @Mock
    private WebhookSubscriptionQueryRepository
            webhookSubscriptionQueryRepository;

    @Mock
    private WebhookResponseMapper webhookResponseMapper;

    @InjectMocks
    private GetWebhookHandler handler;

    private UUID userId;
    private UUID webhookId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        webhookId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnWebhook() {
        WebhookEndpoint endpoint = endpoint();

        WebhookSubscription subscription =
                subscription();

        List<WebhookSubscription> subscriptions =
                List.of(subscription);

        WebhookResponse expectedResponse =
                mock(WebhookResponse.class);

        when(webhookEndpointQueryRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointId(webhookId))
                .thenReturn(subscriptions);

        when(webhookResponseMapper.map(
                endpoint,
                subscriptions
        )).thenReturn(expectedResponse);

        WebhookResponse response =
                handler.execute(userId, webhookId);

        assertSame(expectedResponse, response);

        verify(webhookAccountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        verify(webhookSubscriptionQueryRepository)
                .findAllByWebhookEndpointId(webhookId);

        verify(webhookResponseMapper)
                .map(
                        endpoint,
                        subscriptions
                );
    }

    @Test
    void shouldReturnWebhookWhenNoSubscriptionsExist() {
        WebhookEndpoint endpoint = endpoint();

        List<WebhookSubscription> subscriptions =
                List.of();

        WebhookResponse expectedResponse =
                mock(WebhookResponse.class);

        when(webhookEndpointQueryRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointId(webhookId))
                .thenReturn(subscriptions);

        when(webhookResponseMapper.map(
                endpoint,
                subscriptions
        )).thenReturn(expectedResponse);

        WebhookResponse response =
                handler.execute(userId, webhookId);

        assertSame(expectedResponse, response);

        verify(webhookSubscriptionQueryRepository)
                .findAllByWebhookEndpointId(webhookId);

        verify(webhookResponseMapper)
                .map(
                        endpoint,
                        subscriptions
                );
    }

    @Test
    void shouldThrowWhenWebhookEndpointNotFound() {
        when(webhookEndpointQueryRepository.findById(webhookId))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, webhookId)
        );

        verifyNoInteractions(
                webhookAccountOwnerPort,
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldNotQuerySubscriptionsWhenOwnershipVerificationFails() {
        WebhookEndpoint endpoint = endpoint();

        when(webhookEndpointQueryRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        doThrow(BusinessException.class)
                .when(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, webhookId)
        );

        verify(webhookEndpointQueryRepository)
                .findById(webhookId);

        verify(webhookAccountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        verifyNoInteractions(
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldUseEndpointAccountIdForOwnershipVerification() {
        WebhookEndpoint endpoint = endpoint();

        WebhookResponse expectedResponse =
                mock(WebhookResponse.class);

        when(webhookEndpointQueryRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointId(webhookId))
                .thenReturn(List.of());

        when(webhookResponseMapper.map(
                endpoint,
                List.of()
        )).thenReturn(expectedResponse);

        handler.execute(userId, webhookId);

        ArgumentCaptor<UUID> userIdCaptor =
                ArgumentCaptor.forClass(UUID.class);

        ArgumentCaptor<UUID> accountIdCaptor =
                ArgumentCaptor.forClass(UUID.class);

        verify(webhookAccountOwnerPort).verifyOwnership(
                userIdCaptor.capture(),
                accountIdCaptor.capture()
        );

        assertEquals(userId, userIdCaptor.getValue());
        assertEquals(accountId, accountIdCaptor.getValue());
    }

    @Test
    void shouldNotCallMapperWhenOwnershipVerificationFails() {
        WebhookEndpoint endpoint = endpoint();

        when(webhookEndpointQueryRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        doThrow(BusinessException.class)
                .when(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, webhookId)
        );

        verifyNoInteractions(
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    private WebhookEndpoint endpoint() {
        return WebhookEndpoint.builder()
                .id(webhookId)
                .accountId(accountId)
                .url("https://example.com/webhook")
                .secret("webhook-secret")
                .build();
    }

    private WebhookSubscription subscription() {
        return WebhookSubscription.builder()
                .id(UUID.randomUUID())
                .webhookEndpointId(webhookId)
                .eventType(
                        com.example.ledgercore.webhook.enums.WebhookEventType
                                .TRANSACTION_COMPLETED
                )
                .build();
    }
}
