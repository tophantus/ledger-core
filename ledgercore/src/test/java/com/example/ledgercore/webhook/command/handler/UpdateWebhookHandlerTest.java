package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookCommand;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookResult;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateWebhookHandlerTest {

    @Mock
    private AccountOwnerPort accountOwnerPort;

    @Mock
    private WebhookEndpointCommandRepository webhookEndpointCommandRepository;

    @InjectMocks
    private UpdateWebhookHandler handler;

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
    void shouldUpdateWebhookUrl() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://old.example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookCommand command =
                new UpdateWebhookCommand(
                        userId,
                        webhookId,
                        "https://new.example.com/webhook"
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        UpdateWebhookResult result =
                handler.execute(command);

        assertNotNull(result);
        assertEquals(webhookId, result.id());
        assertEquals(accountId, result.accountId());
        assertEquals(
                "https://new.example.com/webhook",
                result.url()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        assertEquals(
                "https://new.example.com/webhook",
                endpoint.getUrl()
        );
    }

    @Test
    void shouldThrowWebhookNotFound_whenWebhookDoesNotExist() {

        UpdateWebhookCommand command =
                new UpdateWebhookCommand(
                        userId,
                        webhookId,
                        "https://new.example.com/webhook"
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

        verifyNoInteractions(accountOwnerPort);
    }

    @Test
    void shouldThrowInvalidWebhookUrl_whenUrlIsNull() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://old.example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookCommand command =
                new UpdateWebhookCommand(
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
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);
    }

    @Test
    void shouldThrowInvalidWebhookUrl_whenUrlIsBlank() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://old.example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookCommand command =
                new UpdateWebhookCommand(
                        userId,
                        webhookId,
                        "   "
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);
    }

    @Test
    void shouldThrowInvalidWebhookUrl_whenUrlIsMalformed() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://old.example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookCommand command =
                new UpdateWebhookCommand(
                        userId,
                        webhookId,
                        "not-a-valid-url"
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);
    }

    @Test
    void shouldRejectHttpUrl() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://old.example.com/webhook")
                        .secret("secret")
                        .build();

        UpdateWebhookCommand command =
                new UpdateWebhookCommand(
                        userId,
                        webhookId,
                        "http://example.com/webhook"
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_WEBHOOK_URL,
                exception.getErrorCode()
        );

        verify(accountOwnerPort)
                .verifyOwnership(userId, accountId);

        assertEquals(
                "https://old.example.com/webhook",
                endpoint.getUrl()
        );
    }
}