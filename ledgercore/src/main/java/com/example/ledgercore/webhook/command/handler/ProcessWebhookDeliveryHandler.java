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
import com.example.ledgercore.webhook.service.WebhookRetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final WebhookRetryPolicy retryPolicy;

    @Override
    public void execute(UUID deliveryId) {

        Instant now = Instant.now();

        boolean claimed =
                webhookDeliveryCommandRepository.claim(
                        deliveryId,
                        WebhookDeliveryStatus.PROCESSING,
                        WebhookDeliveryStatus.PENDING,
                        WebhookDeliveryStatus.RETRYING,
                        now
                ) == 1;

        if (!claimed) {
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

        if (endpoint == null) {
            fail(
                    delivery,
                    "Webhook endpoint not found"
            );
            return;
        }

        if (endpoint.getStatus()
                != WebhookStatus.ACTIVE) {

            fail(
                    delivery,
                    "Webhook endpoint is not active"
            );
            return;
        }

        WebhookHttpClientPort.WebhookResponse response =
                webhookHttpClientPort.send(
                        endpoint.getUrl(),
                        endpoint.getSecret(),
                        delivery.getPayload()
                );

        handleResponse(
                delivery,
                response
        );
    }

    private void handleResponse(
            WebhookDelivery delivery,
            WebhookHttpClientPort.WebhookResponse response
    ) {
        if (response.isSuccess()) {

            webhookDeliveryCommandRepository.markDelivered(
                    delivery.getId(),
                    WebhookDeliveryStatus.DELIVERED,
                    WebhookDeliveryStatus.PROCESSING,
                    Instant.now()
            );

            return;
        }

        String error =
                buildErrorMessage(response);

        if (!response.isRetryable()) {

            fail(
                    delivery,
                    error
            );

            return;
        }

        retry(
                delivery,
                error
        );
    }

    private void retry(
            WebhookDelivery delivery,
            String error
    ) {
        if (!retryPolicy.shouldRetry(
                delivery.getAttemptCount()
        )) {

            fail(
                    delivery,
                    error
            );

            return;
        }

        Instant nextAttemptAt =
                Instant.now()
                        .plus(
                                retryPolicy.getDelay(
                                        delivery.getAttemptCount()
                                )
                        );

        webhookDeliveryCommandRepository.markRetry(
                delivery.getId(),
                WebhookDeliveryStatus.RETRYING,
                WebhookDeliveryStatus.PROCESSING,
                nextAttemptAt,
                error
        );

        log.warn(
                "Webhook delivery scheduled for retry " +
                        "deliveryId={}, attempt={}, nextAttemptAt={}",
                delivery.getId(),
                delivery.getAttemptCount(),
                nextAttemptAt
        );
    }

    private void fail(
            WebhookDelivery delivery,
            String error
    ) {
        webhookDeliveryCommandRepository.markFailed(
                delivery.getId(),
                WebhookDeliveryStatus.FAILED,
                WebhookDeliveryStatus.PROCESSING,
                truncate(error)
        );

        log.error(
                "Webhook delivery permanently failed " +
                        "deliveryId={}, attempt={}",
                delivery.getId(),
                delivery.getAttemptCount()
        );
    }

    private String buildErrorMessage(
            WebhookHttpClientPort.WebhookResponse response
    ) {
        if (response.error() != null
                && !response.error().isBlank()) {

            return truncate(response.error());
        }

        String message =
                "Webhook returned HTTP "
                        + response.statusCode();

        if (response.responseBody() != null
                && !response.responseBody().isBlank()) {

            message += ": "
                    + response.responseBody();
        }

        return truncate(message);
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown webhook delivery error";
        }

        return value.length() <= 2000
                ? value
                : value.substring(0, 2000);
    }
}