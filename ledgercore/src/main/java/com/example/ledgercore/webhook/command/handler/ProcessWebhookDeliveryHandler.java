package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.webhook.command.port.inbound.ProcessWebhookDeliveryUseCase;
import com.example.ledgercore.webhook.command.port.outbound.WebhookHttpClientPort;
import com.example.ledgercore.webhook.command.repository.WebhookDeliveryCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import com.example.ledgercore.webhook.query.repository.WebhookDeliveryQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessWebhookDeliveryHandler
        implements ProcessWebhookDeliveryUseCase {

    private final WebhookDeliveryQueryRepository
            webhookDeliveryQueryRepository;

    private final WebhookDeliveryCommandRepository
            webhookDeliveryCommandRepository;

    private final WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    private final WebhookHttpClientPort
            webhookHttpClientPort;

    @Override
    public void execute(UUID deliveryId) {

        if (!claimDelivery(deliveryId)) {
            return;
        }

        WebhookDelivery delivery =
                webhookDeliveryQueryRepository
                        .findById(deliveryId)
                        .orElse(null);

        if (delivery == null) {
            return;
        }

        WebhookEndpoint endpoint =
                webhookEndpointQueryRepository
                        .findById(
                                delivery.getWebhookEndpointId()
                        )
                        .orElse(null);

        if (endpoint == null
                || endpoint.getStatus()
                != WebhookStatus.ACTIVE) {

            markFailed(
                    deliveryId,
                    "Webhook endpoint is not active"
            );

            return;
        }

        try {
            webhookHttpClientPort.send(
                    endpoint.getUrl(),
                    endpoint.getSecret(),
                    delivery.getPayload()
            );

            markDelivered(deliveryId);

        } catch (Exception ex) {

            log.warn(
                    "Webhook delivery failed deliveryId={}",
                    deliveryId,
                    ex
            );

            markFailed(
                    deliveryId,
                    ex.getMessage()
            );
        }
    }

    @Transactional
    protected boolean claimDelivery(
            UUID deliveryId
    ) {
        return webhookDeliveryCommandRepository.claim(
                deliveryId,
                WebhookDeliveryStatus.PROCESSING,
                WebhookDeliveryStatus.PENDING,
                WebhookDeliveryStatus.RETRYING,
                Instant.now()
        ) == 1;
    }

    @Transactional
    protected void markDelivered(
            UUID deliveryId
    ) {
        webhookDeliveryCommandRepository
                .findById(deliveryId)
                .ifPresent(delivery ->
                        delivery.markDelivered(
                                Instant.now()
                        )
                );
    }

    @Transactional
    protected void markFailed(
            UUID deliveryId,
            String error
    ) {
        webhookDeliveryCommandRepository
                .findById(deliveryId)
                .ifPresent(delivery ->
                        delivery.markFailed(
                                truncate(error)
                        )
                );
    }

    private String truncate(String error) {
        if (error == null || error.isBlank()) {
            return "Unknown webhook delivery error";
        }

        return error.length() <= 2000
                ? error
                : error.substring(0, 2000);
    }
}