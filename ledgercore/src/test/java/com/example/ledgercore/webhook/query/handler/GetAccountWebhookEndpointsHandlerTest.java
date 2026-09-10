package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.mapper.WebhookResponseMapper;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountWebhookEndpointsHandlerTest {

    @Mock
    private AccountOwnerPort accountOwnerPort;

    @Mock
    private WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    @Mock
    private WebhookSubscriptionQueryRepository
            webhookSubscriptionQueryRepository;

    @Mock
    private WebhookResponseMapper webhookResponseMapper;

    @InjectMocks
    private GetAccountWebhookEndpointsHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnWebhookEndpointsWithSubscriptions() {
        WebhookEndpoint endpoint1 =
                endpoint(UUID.randomUUID());

        WebhookEndpoint endpoint2 =
                endpoint(UUID.randomUUID());

        List<WebhookEndpoint> endpoints =
                List.of(endpoint1, endpoint2);

        WebhookSubscription subscription1 =
                subscription(endpoint1.getId());

        WebhookSubscription subscription2 =
                subscription(endpoint2.getId());

        List<WebhookSubscription> subscriptions =
                List.of(subscription1, subscription2);

        List<WebhookResponse> expected =
                List.of(
                        mock(WebhookResponse.class),
                        mock(WebhookResponse.class)
                );

        when(webhookEndpointQueryRepository
                .findAllByAccountId(accountId))
                .thenReturn(endpoints);

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointIdIn(
                        List.of(
                                endpoint1.getId(),
                                endpoint2.getId()
                        )
                ))
                .thenReturn(subscriptions);

        when(webhookResponseMapper.map(
                endpoints,
                subscriptions
        )).thenReturn(expected);

        List<WebhookResponse> result =
                handler.execute(userId, accountId);

        assertSame(expected, result);

        verify(accountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        verify(webhookEndpointQueryRepository)
                .findAllByAccountId(accountId);

        verify(webhookSubscriptionQueryRepository)
                .findAllByWebhookEndpointIdIn(
                        List.of(
                                endpoint1.getId(),
                                endpoint2.getId()
                        )
                );

        verify(webhookResponseMapper)
                .map(
                        endpoints,
                        subscriptions
                );
    }

    @Test
    void shouldReturnEmptyListWhenNoWebhookEndpointsExist() {
        when(webhookEndpointQueryRepository
                .findAllByAccountId(accountId))
                .thenReturn(List.of());

        List<WebhookResponse> result =
                handler.execute(userId, accountId);

        assertTrue(result.isEmpty());

        verify(accountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        verify(webhookEndpointQueryRepository)
                .findAllByAccountId(accountId);

        verifyNoInteractions(
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldNotQuerySubscriptionsWhenOwnershipVerificationFails() {
        doThrow(BusinessException.class)
                .when(accountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        userId,
                        accountId
                )
        );

        verify(accountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldQuerySubscriptionsUsingAllEndpointIds() {
        WebhookEndpoint endpoint1 =
                endpoint(UUID.randomUUID());

        WebhookEndpoint endpoint2 =
                endpoint(UUID.randomUUID());

        List<WebhookEndpoint> endpoints =
                List.of(endpoint1, endpoint2);

        when(webhookEndpointQueryRepository
                .findAllByAccountId(accountId))
                .thenReturn(endpoints);

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointIdIn(
                        List.of(
                                endpoint1.getId(),
                                endpoint2.getId()
                        )
                ))
                .thenReturn(List.of());

        when(webhookResponseMapper.map(
                endpoints,
                List.of()
        )).thenReturn(List.of());

        handler.execute(userId, accountId);

        verify(webhookSubscriptionQueryRepository)
                .findAllByWebhookEndpointIdIn(
                        List.of(
                                endpoint1.getId(),
                                endpoint2.getId()
                        )
                );
    }

    private WebhookEndpoint endpoint(UUID id) {
        return WebhookEndpoint.builder()
                .id(id)
                .accountId(accountId)
                .url("https://example.com/webhook/" + id)
                .secret("webhook-secret")
                .build();
    }

    private WebhookSubscription subscription(
            UUID endpointId
    ) {
        return WebhookSubscription.builder()
                .id(UUID.randomUUID())
                .webhookEndpointId(endpointId)
                .build();
    }
}
