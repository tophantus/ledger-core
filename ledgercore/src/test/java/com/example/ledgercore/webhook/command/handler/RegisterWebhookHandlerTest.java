package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookCommand;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookResult;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.command.repository.WebhookSubscriptionCommandRepository;
import com.example.ledgercore.webhook.config.WebhookProperties;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.service.WebhookSecretGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterWebhookHandlerTest {

    @Mock
    private AccountOwnerPort accountOwnerPort;

    @Mock
    private WebhookEndpointCommandRepository webhookEndpointCommandRepository;

    @Mock
    private WebhookSubscriptionCommandRepository webhookSubscriptionCommandRepository;

    @Mock
    private WebhookSecretGenerator webhookSecretGenerator;

    @Mock
    private WebhookProperties webhookProperties;

    @InjectMocks
    private RegisterWebhookHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID webhookId;
    private Instant createdAt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        webhookId = UUID.randomUUID();
        createdAt = Instant.now();
    }

    // =========================================================
    // SUCCESS
    // =========================================================

    @Test
    void shouldRegisterWebhook() {

        RegisterWebhookCommand command = command(
                "https://example.com/webhook",
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        WebhookEndpoint savedEndpoint =
                mockSavedEndpoint(command.url());

        when(webhookSecretGenerator.generate())
                .thenReturn("secret-123");

        when(webhookEndpointCommandRepository.save(any(WebhookEndpoint.class)))
                .thenReturn(savedEndpoint);

        RegisterWebhookResult result =
                handler.execute(command);

        assertNotNull(result);

        assertEquals(
                webhookId,
                result.webhookId()
        );

        assertEquals(
                accountId,
                result.accountId()
        );

        assertEquals(
                command.url(),
                result.url()
        );

        assertEquals(
                "secret-123",
                result.secret()
        );

        assertEquals(
                Set.of(WebhookEventType.TRANSACTION_COMPLETED),
                result.eventTypes()
        );

        assertEquals(
                createdAt,
                result.createdAt()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookSecretGenerator)
                .generate();

        verify(webhookEndpointCommandRepository)
                .save(any(WebhookEndpoint.class));

        verify(webhookSubscriptionCommandRepository)
                .saveAll(any());

        // HTTPS => short-circuit, allowHttp() không được gọi
        verify(webhookProperties, never())
                .allowHttp();
    }

    @Test
    void shouldCreateSubscriptionsForAllEventTypes() {

        Set<WebhookEventType> eventTypes = Set.of(
                WebhookEventType.ACCOUNT_BALANCE_CHANGED,
                WebhookEventType.TRANSACTION_COMPLETED,
                WebhookEventType.TRANSACTION_FAILED
        );

        RegisterWebhookCommand command = command(
                "https://example.com/webhook",
                eventTypes
        );

        WebhookEndpoint savedEndpoint =
                mockSavedEndpoint(command.url());

        when(webhookSecretGenerator.generate())
                .thenReturn("secret-123");

        when(webhookEndpointCommandRepository.save(any(WebhookEndpoint.class)))
                .thenReturn(savedEndpoint);

        handler.execute(command);

        ArgumentCaptor<List<WebhookSubscription>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(webhookSubscriptionCommandRepository)
                .saveAll(captor.capture());

        List<WebhookSubscription> subscriptions =
                captor.getValue();

        assertEquals(
                3,
                subscriptions.size()
        );

        Set<WebhookEventType> actualEventTypes =
                subscriptions.stream()
                        .map(WebhookSubscription::getEventType)
                        .collect(Collectors.toSet());

        assertEquals(
                eventTypes,
                actualEventTypes
        );

        assertTrue(
                subscriptions.stream()
                        .allMatch(subscription ->
                                webhookId.equals(
                                        subscription.getWebhookEndpointId()
                                )
                        )
        );

        verify(webhookProperties, never())
                .allowHttp();
    }

    // =========================================================
    // HTTP
    // =========================================================

    @Test
    void shouldRejectHttp_whenHttpIsDisabled() {

        RegisterWebhookCommand command = command(
                "http://example.com/webhook",
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        when(webhookProperties.allowHttp())
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookProperties)
                .allowHttp();

        verify(webhookSecretGenerator, never())
                .generate();

        verify(webhookEndpointCommandRepository, never())
                .save(any());

        verify(webhookSubscriptionCommandRepository, never())
                .saveAll(any());
    }

    @Test
    void shouldAcceptHttp_whenHttpIsEnabled() {

        RegisterWebhookCommand command = command(
                "http://example.com/webhook",
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        WebhookEndpoint savedEndpoint =
                mockSavedEndpoint(command.url());

        when(webhookProperties.allowHttp())
                .thenReturn(true);

        when(webhookSecretGenerator.generate())
                .thenReturn("secret-123");

        when(webhookEndpointCommandRepository.save(any(WebhookEndpoint.class)))
                .thenReturn(savedEndpoint);

        RegisterWebhookResult result =
                handler.execute(command);

        assertEquals(
                "http://example.com/webhook",
                result.url()
        );

        verify(webhookProperties)
                .allowHttp();

        verify(webhookEndpointCommandRepository)
                .save(any(WebhookEndpoint.class));
    }

    // =========================================================
    // URL VALIDATION
    // =========================================================

    @Test
    void shouldRejectNullUrl() {

        RegisterWebhookCommand command = command(
                null,
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        // null => return trước khi gọi allowHttp()
        verify(webhookProperties, never())
                .allowHttp();

        verify(webhookSecretGenerator, never())
                .generate();

        verify(webhookEndpointCommandRepository, never())
                .save(any());

        verify(webhookSubscriptionCommandRepository, never())
                .saveAll(any());
    }

    @Test
    void shouldRejectBlankUrl() {

        RegisterWebhookCommand command = command(
                "   ",
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookProperties, never())
                .allowHttp();

        verify(webhookSecretGenerator, never())
                .generate();

        verify(webhookEndpointCommandRepository, never())
                .save(any());

        verify(webhookSubscriptionCommandRepository, never())
                .saveAll(any());
    }

    @Test
    void shouldRejectMalformedUrl() {

        RegisterWebhookCommand command = command(
                "not-a-valid-url",
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        /*
         * URI.create("not-a-valid-url") không throw.
         *
         * scheme = null
         *
         * validScheme:
         *
         * "https".equalsIgnoreCase(null)
         *      || (allowHttp && "http".equalsIgnoreCase(null))
         *
         * => allowHttp() được gọi.
         */
        verify(webhookProperties)
                .allowHttp();

        verify(webhookSecretGenerator, never())
                .generate();

        verify(webhookEndpointCommandRepository, never())
                .save(any());

        verify(webhookSubscriptionCommandRepository, never())
                .saveAll(any());
    }

    @Test
    void shouldRejectUrlWithoutHost() {

        RegisterWebhookCommand command = command(
                "https://",
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(webhookProperties, never())
                .allowHttp();

        verify(webhookSecretGenerator, never())
                .generate();

        verify(webhookEndpointCommandRepository, never())
                .save(any());

        verify(webhookSubscriptionCommandRepository, never())
                .saveAll(any());
    }

    // =========================================================
    // EVENT TYPES
    // =========================================================

    @Test
    void shouldRejectNullEventTypes() {

        RegisterWebhookCommand command = command(
                "https://example.com/webhook",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_EVENT_TYPES,
                exception.getErrorCode()
        );

        // HTTPS => không gọi allowHttp()
        verify(webhookProperties, never())
                .allowHttp();

        verify(webhookSecretGenerator, never())
                .generate();

        verify(webhookEndpointCommandRepository, never())
                .save(any());

        verify(webhookSubscriptionCommandRepository, never())
                .saveAll(any());
    }

    @Test
    void shouldRejectEmptyEventTypes() {

        RegisterWebhookCommand command = command(
                "https://example.com/webhook",
                Set.of()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_EVENT_TYPES,
                exception.getErrorCode()
        );

        verify(webhookProperties, never())
                .allowHttp();

        verify(webhookSecretGenerator, never())
                .generate();

        verify(webhookEndpointCommandRepository, never())
                .save(any());

        verify(webhookSubscriptionCommandRepository, never())
                .saveAll(any());
    }

    // =========================================================
    // OWNERSHIP
    // =========================================================

    @Test
    void shouldVerifyOwnershipBeforeCreatingWebhook() {

        RegisterWebhookCommand command = command(
                "https://example.com/webhook",
                Set.of(WebhookEventType.TRANSACTION_COMPLETED)
        );

        WebhookEndpoint savedEndpoint =
                mockSavedEndpoint(command.url());

        when(webhookSecretGenerator.generate())
                .thenReturn("secret-123");

        when(webhookEndpointCommandRepository.save(any(WebhookEndpoint.class)))
                .thenReturn(savedEndpoint);

        handler.execute(command);

        var inOrder = inOrder(
                accountOwnerPort,
                webhookEndpointCommandRepository
        );

        inOrder.verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        inOrder.verify(webhookEndpointCommandRepository)
                .save(any(WebhookEndpoint.class));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private RegisterWebhookCommand command(
            String url,
            Set<WebhookEventType> eventTypes
    ) {
        return new RegisterWebhookCommand(
                userId,
                accountId,
                url,
                eventTypes
        );
    }

    private WebhookEndpoint mockSavedEndpoint(String url) {

        WebhookEndpoint endpoint =
                mock(WebhookEndpoint.class);

        when(endpoint.getId())
                .thenReturn(webhookId);

        when(endpoint.getAccountId())
                .thenReturn(accountId);

        when(endpoint.getUrl())
                .thenReturn(url);

        when(endpoint.getStatus())
                .thenReturn(null);

        when(endpoint.getCreatedAt())
                .thenReturn(createdAt);

        return endpoint;
    }
}