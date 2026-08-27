package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookSubscriptionsCommand;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.command.repository.WebhookSubscriptionCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateWebhookSubscriptionsHandlerTest {

    @Mock
    private AccountOwnerPort accountOwnerPort;

    @Mock
    private WebhookEndpointCommandRepository webhookEndpointCommandRepository;

    @Mock
    private WebhookSubscriptionCommandRepository webhookSubscriptionCommandRepository;

    @InjectMocks
    private UpdateWebhookSubscriptionsHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID webhookId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        webhookId = UUID.randomUUID();
    }

    @Test
    void shouldUpdateWebhookSubscriptions() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://example.com/webhook")
                        .secret("secret")
                        .build();

        Set<WebhookEventType> eventTypes =
                Set.of(
                        WebhookEventType.ACCOUNT_BALANCE_CHANGED,
                        WebhookEventType.TRANSACTION_COMPLETED
                );

        UpdateWebhookSubscriptionsCommand command =
                new UpdateWebhookSubscriptionsCommand(
                        userId,
                        webhookId,
                        eventTypes
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSubscriptionCommandRepository.saveAll(
                anyList()
        )).thenReturn(List.of());

        assertDoesNotThrow(
                () -> handler.execute(command)
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookSubscriptionCommandRepository)
                .deleteAllByWebhookEndpointId(webhookId);

        verify(webhookSubscriptionCommandRepository)
                .saveAll(anyList());
    }

    @Test
    void shouldCreateSubscriptionsForAllEventTypes() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://example.com/webhook")
                        .secret("secret")
                        .build();

        Set<WebhookEventType> eventTypes =
                Set.of(
                        WebhookEventType.ACCOUNT_BALANCE_CHANGED,
                        WebhookEventType.TRANSACTION_COMPLETED,
                        WebhookEventType.TRANSACTION_FAILED
                );

        UpdateWebhookSubscriptionsCommand command =
                new UpdateWebhookSubscriptionsCommand(
                        userId,
                        webhookId,
                        eventTypes
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSubscriptionCommandRepository.saveAll(
                anyList()
        )).thenReturn(List.of());

        handler.execute(command);

        ArgumentCaptor<List<WebhookSubscription>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(webhookSubscriptionCommandRepository)
                .saveAll(captor.capture());

        List<WebhookSubscription> subscriptions =
                captor.getValue();

        assertEquals(3, subscriptions.size());

        assertTrue(
                subscriptions.stream()
                        .allMatch(subscription ->
                                webhookId.equals(
                                        subscription.getWebhookEndpointId()
                                )
                        )
        );

        assertEquals(
                eventTypes,
                subscriptions.stream()
                        .map(WebhookSubscription::getEventType)
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void shouldThrowWebhookNotFound_whenWebhookDoesNotExist() {

        UpdateWebhookSubscriptionsCommand command =
                new UpdateWebhookSubscriptionsCommand(
                        userId,
                        webhookId,
                        Set.of(
                                WebhookEventType.TRANSACTION_COMPLETED
                        )
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.WEBHOOK_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(webhookEndpointCommandRepository)
                .findById(webhookId);

        verifyNoInteractions(
                accountOwnerPort,
                webhookSubscriptionCommandRepository
        );
    }

    @Test
    void shouldThrowInvalidEventTypes_whenEventTypesAreNull() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookSubscriptionsCommand command =
                new UpdateWebhookSubscriptionsCommand(
                        userId,
                        webhookId,
                        null
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_EVENT_TYPES,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        verifyNoInteractions(
                webhookSubscriptionCommandRepository
        );
    }

    @Test
    void shouldThrowInvalidEventTypes_whenEventTypesAreEmpty() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookSubscriptionsCommand command =
                new UpdateWebhookSubscriptionsCommand(
                        userId,
                        webhookId,
                        Set.of()
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_EVENT_TYPES,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        verifyNoInteractions(
                webhookSubscriptionCommandRepository
        );
    }

    @Test
    void shouldDeleteOldSubscriptionsBeforeCreatingNewOnes() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookSubscriptionsCommand command =
                new UpdateWebhookSubscriptionsCommand(
                        userId,
                        webhookId,
                        Set.of(
                                WebhookEventType.TRANSACTION_COMPLETED
                        )
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSubscriptionCommandRepository.saveAll(
                anyList()
        )).thenReturn(List.of());

        handler.execute(command);

        var inOrder = inOrder(
                webhookSubscriptionCommandRepository
        );

        inOrder.verify(
                webhookSubscriptionCommandRepository
        ).deleteAllByWebhookEndpointId(webhookId);

        inOrder.verify(
                webhookSubscriptionCommandRepository
        ).saveAll(anyList());
    }
}