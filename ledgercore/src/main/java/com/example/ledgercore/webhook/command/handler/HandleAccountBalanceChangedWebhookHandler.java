package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;
import com.example.ledgercore.webhook.command.port.inbound.HandleAccountBalanceChangedWebhookUseCase;
import com.example.ledgercore.webhook.command.repository.WebhookDeliveryCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandleAccountBalanceChangedWebhookHandler
        implements HandleAccountBalanceChangedWebhookUseCase {

    private final WebhookSubscriptionQueryRepository
            webhookSubscriptionQueryRepository;

    private final WebhookDeliveryCommandRepository
            webhookDeliveryCommandRepository;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void execute(
            AccountBalanceChangedEvent event
    ) {
        String payload = serialize(event);

        createDeliveriesForAccount(
                event.accountId(),
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
                                WebhookEventType.ACCOUNT_BALANCE_CHANGED,
                                WebhookStatus.ACTIVE
                        );

        if (subscriptions.isEmpty()) {
            log.debug(
                    "No active webhook subscriptions found for account {} and event type {}",
                    accountId,
                    WebhookEventType.ACCOUNT_BALANCE_CHANGED
            );
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
            log.debug(
                    "Webhook delivery already exists for endpoint {} and event {}",
                    endpointId,
                    eventId
            );
            return;
        }

        WebhookDelivery delivery =
                WebhookDelivery.builder()
                        .webhookEndpointId(endpointId)
                        .eventId(eventId)
                        .eventType(
                                WebhookEventType.ACCOUNT_BALANCE_CHANGED
                        )
                        .payload(payload)
                        .status(WebhookDeliveryStatus.PENDING)
                        .attemptCount(0)
                        .createdAt(Instant.now())
                        .build();

        webhookDeliveryCommandRepository.save(delivery);

        log.debug(
                "Created webhook delivery for endpoint {} and event {}",
                endpointId,
                eventId
        );
    }

    private String serialize(
            AccountBalanceChangedEvent event
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