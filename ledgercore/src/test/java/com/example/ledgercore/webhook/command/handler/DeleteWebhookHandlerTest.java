package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.DeleteWebhookCommand;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteWebhookHandlerTest {

    @Mock
    private AccountOwnerPort accountOwnerPort;

    @Mock
    private WebhookEndpointCommandRepository webhookEndpointCommandRepository;

    @Mock
    private WebhookEndpoint endpoint;

    @InjectMocks
    private DeleteWebhookHandler handler;

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
    void shouldInactivateWebhook_whenWebhookExistsAndUserOwnsAccount() {

        DeleteWebhookCommand command =
                new DeleteWebhookCommand(
                        userId,
                        webhookId
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(endpoint.getAccountId())
                .thenReturn(accountId);

        handler.execute(command);

        verify(webhookEndpointCommandRepository)
                .findById(webhookId);

        verify(accountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        verify(endpoint)
                .inactivate();

        verifyNoMoreInteractions(
                webhookEndpointCommandRepository,
                accountOwnerPort,
                endpoint
        );
    }

    @Test
    void shouldThrowWebhookNotFound_whenWebhookDoesNotExist() {

        DeleteWebhookCommand command =
                new DeleteWebhookCommand(
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
                ErrorCode.WEBHOOK_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(webhookEndpointCommandRepository)
                .findById(webhookId);

        verifyNoInteractions(
                accountOwnerPort
        );

        verifyNoInteractions(
                endpoint
        );
    }

    @Test
    void shouldVerifyOwnershipBeforeInactivatingWebhook() {

        DeleteWebhookCommand command =
                new DeleteWebhookCommand(
                        userId,
                        webhookId
                );

        when(webhookEndpointCommandRepository.findById(webhookId))
                .thenReturn(Optional.of(endpoint));

        when(endpoint.getAccountId())
                .thenReturn(accountId);

        handler.execute(command);

        var inOrder = inOrder(
                accountOwnerPort,
                endpoint
        );

        inOrder.verify(accountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );

        inOrder.verify(endpoint)
                .inactivate();
    }
}