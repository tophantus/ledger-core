package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretCommand;
import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretResult;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.port.outbound.WebhookAccountOwnerPort;
import com.example.ledgercore.webhook.service.WebhookSecretGenerator;
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
class RotateWebhookSecretHandlerTest {

    @Mock
    private WebhookAccountOwnerPort webhookAccountOwnerPort;

    @Mock
    private WebhookEndpointCommandRepository webhookEndpointCommandRepository;

    @Mock
    private WebhookSecretGenerator webhookSecretGenerator;

    @InjectMocks
    private RotateWebhookSecretHandler handler;

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
    void shouldRotateWebhookSecret() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://example.com/webhook")
                        .secret("old-secret")
                        .build();

        RotateWebhookSecretCommand command =
                new RotateWebhookSecretCommand(
                        userId,
                        webhookId
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSecretGenerator.generate())
                .thenReturn("new-secret");

        RotateWebhookSecretResult result =
                handler.execute(command);

        assertNotNull(result);
        assertEquals(webhookId, result.webhookId());
        assertEquals("new-secret", result.secret());

        verify(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookSecretGenerator)
                .generate();
    }

    @Test
    void shouldThrowWebhookNotFound_whenWebhookDoesNotExist() {

        RotateWebhookSecretCommand command =
                new RotateWebhookSecretCommand(
                        userId,
                        webhookId
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.WEBHOOK_ENDPOINT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(webhookEndpointCommandRepository)
                .findById(webhookId);

        verifyNoInteractions(
                webhookAccountOwnerPort,
                webhookSecretGenerator
        );
    }

    @Test
    void shouldVerifyOwnershipBeforeRotatingSecret() {

        WebhookEndpoint endpoint =
                WebhookEndpoint.builder()
                        .id(webhookId)
                        .accountId(accountId)
                        .url("https://example.com/webhook")
                        .secret("old-secret")
                        .build();

        RotateWebhookSecretCommand command =
                new RotateWebhookSecretCommand(
                        userId,
                        webhookId
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(webhookSecretGenerator.generate())
                .thenReturn("new-secret");

        handler.execute(command);

        var inOrder = inOrder(
                webhookEndpointCommandRepository,
                webhookAccountOwnerPort,
                webhookSecretGenerator
        );

        inOrder.verify(webhookEndpointCommandRepository)
                .findById(webhookId);

        inOrder.verify(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        inOrder.verify(webhookSecretGenerator)
                .generate();
    }
}