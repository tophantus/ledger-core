package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookSubscriptionsCommand;
import com.example.ledgercore.webhook.command.port.inbound.UpdateWebhookSubscriptionsUseCase;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.command.repository.WebhookSubscriptionCommandRepository;
import com.example.ledgercore.webhook.command.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UpdateWebhookSubscriptionsHandler
        implements UpdateWebhookSubscriptionsUseCase {

    private final AccountOwnerPort accountOwnerPort;
    private final WebhookEndpointCommandRepository webhookEndpointCommandRepository;
    private final WebhookSubscriptionCommandRepository webhookSubscriptionCommandRepository;

    @Override
    @Transactional
    public void execute(
            UpdateWebhookSubscriptionsCommand command
    ) {
        WebhookEndpoint endpoint = webhookEndpointCommandRepository
                .findById(command.webhookId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.WEBHOOK_NOT_FOUND
                ));

        accountOwnerPort.verifyOwnership(
                command.userId(),
                endpoint.getAccountId()
        );

        validateEventTypes(command.eventTypes());

        webhookSubscriptionCommandRepository
                .deleteAllByWebhookEndpointId(endpoint.getId());

        List<WebhookSubscription> subscriptions =
                command.eventTypes().stream()
                        .map(eventType ->
                                WebhookSubscription.builder()
                                        .webhookEndpointId(endpoint.getId())
                                        .eventType(eventType)
                                        .build()
                        )
                        .toList();

        webhookSubscriptionCommandRepository.saveAll(
                subscriptions
        );
    }

    private void validateEventTypes(
            Set<?> eventTypes
    ) {
        if (eventTypes == null || eventTypes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_WEBHOOK_EVENT_TYPES
            );
        }
    }
}