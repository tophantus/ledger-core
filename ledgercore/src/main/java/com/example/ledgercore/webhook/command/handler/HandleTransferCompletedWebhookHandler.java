package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.webhook.command.port.inbound.HandleTransferCompletedWebhookUseCase;
import com.example.ledgercore.webhook.command.repository.WebhookDeliveryCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HandleTransferCompletedWebhookHandler
        implements HandleTransferCompletedWebhookUseCase {

    private final WebhookSubscriptionQueryRepository
            webhookSubscriptionQueryRepository;

    private final WebhookDeliveryCommandRepository
            webhookDeliveryCommandRepository;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void execute(
            TransferCompletedEvent event
    ) {
        String payload = serialize(event);

        createDeliveriesForAccount(
                event.sourceAccountId(),
                event.transactionId(),
                payload
        );

        createDeliveriesForAccount(
                event.destinationAccountId(),
                event.transactionId(),
                payload
        );
    }

    private void createDeliveriesForAccount(
            UUID accountId,
            UUID eventId,
            String payload
    ) {
        List<WebhookSubscription> subscriptions =
                webhookSubscriptionQueryRepository
                        .findActiveSubscriptions(
                                accountId,
                                WebhookEventType.TRANSACTION_COMPLETED,
                                WebhookStatus.ACTIVE
                        );

        if (subscriptions.isEmpty()) {
            return;
        }

        for (WebhookSubscription subscription : subscriptions) {
            createDelivery(
                    subscription,
                    eventId,
                    payload
            );
        }
    }

    private void createDelivery(
            WebhookSubscription subscription,
            UUID eventId,
            String payload
    ) {
        UUID endpointId =
                subscription.getWebhookEndpointId();

        boolean exists =
                webhookDeliveryCommandRepository
                        .existsByWebhookEndpointIdAndEventId(
                                endpointId,
                                eventId
                        );

        if (exists) {
            return;
        }

        WebhookDelivery delivery =
                WebhookDelivery.builder()
                        .webhookEndpointId(endpointId)
                        .eventId(eventId)
                        .eventType(
                                WebhookEventType.TRANSACTION_COMPLETED
                        )
                        .payload(payload)
                        .status(WebhookDeliveryStatus.PENDING)
                        .attemptCount(0)
                        .createdAt(Instant.now())
                        .build();

        webhookDeliveryCommandRepository.save(delivery);
    }

    private String serialize(
            TransferCompletedEvent event
    ) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize webhook event",
                    e
            );
        }
    }
}