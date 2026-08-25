package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookCommand;
import com.example.ledgercore.webhook.command.port.inbound.RegisterWebhookUseCase;
import com.example.ledgercore.webhook.command.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.command.repository.WebhookSubscriptionCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.service.WebhookSecretGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterWebhookHandler
        implements RegisterWebhookUseCase {

    private final AccountOwnerPort accountOwnerPort;
    private final WebhookEndpointCommandRepository webhookEndpointCommandRepository;
    private final WebhookSubscriptionCommandRepository webhookSubscriptionCommandRepository;
    private final WebhookSecretGenerator webhookSecretGenerator;

    @Override
    @Transactional
    public UUID execute(RegisterWebhookCommand command) {
        validateAccountOwnership(command);
        validateUrl(command.url());
        validateEventTypes(command);

        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .accountId(command.accountId())
                .url(command.url())
                .secret(webhookSecretGenerator.generate())
                .build();

        WebhookEndpoint savedEndpoint =
                webhookEndpointCommandRepository.save(endpoint);

        var subscriptions = command.eventTypes().stream()
                .map(eventType ->
                        WebhookSubscription.builder()
                                .webhookEndpointId(
                                        savedEndpoint.getId()
                                )
                                .eventType(eventType)
                                .build()
                )
                .toList();

        webhookSubscriptionCommandRepository.saveAll(
                subscriptions
        );

        return savedEndpoint.getId();
    }

    private void validateAccountOwnership(
            RegisterWebhookCommand command
    ) {
        if (!accountOwnerPort.isOwner(
                command.accountId(),
                command.userId()
        )) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_FOUND
            );
        }
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_WEBHOOK_URL
            );
        }

        try {
            URI uri = URI.create(url);

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_WEBHOOK_URL
                );
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.INVALID_WEBHOOK_URL
            );
        }
    }

    private void validateEventTypes(
            RegisterWebhookCommand command
    ) {
        if (command.eventTypes() == null
                || command.eventTypes().isEmpty()
                || command.eventTypes().stream()
                .anyMatch(eventType ->
                        eventType == null
                                || eventType.isBlank())) {
            throw new BusinessException(
                    ErrorCode.INVALID_WEBHOOK_EVENT_TYPES
            );
        }
    }
}