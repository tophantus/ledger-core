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

        log.debug(
                "Starting webhook delivery processing deliveryId={}",
                deliveryId
        );

        int claimResult =
                webhookDeliveryCommandRepository.claim(
                        deliveryId,
                        WebhookDeliveryStatus.PROCESSING,
                        WebhookDeliveryStatus.PENDING,
                        WebhookDeliveryStatus.RETRYING,
                        now
                );

        boolean claimed = claimResult == 1;

        log.debug(
                "Webhook delivery claim result deliveryId={}, " +
                        "claimed={}, rowsUpdated={}",
                deliveryId,
                claimed,
                claimResult
        );

        if (!claimed) {
            log.debug(
                    "Webhook delivery was not claimed, " +
                            "possibly already processing or completed " +
                            "deliveryId={}",
                    deliveryId
            );
            return;
        }

        WebhookDelivery delivery =
                webhookDeliveryQueryRepository
                        .findById(deliveryId)
                        .orElse(null);

        if (delivery == null) {
            log.warn(
                    "Webhook delivery not found after claim " +
                            "deliveryId={}",
                    deliveryId
            );
            return;
        }

        log.debug(
                "Webhook delivery claimed deliveryId={}, " +
                        "status={}, attemptCount={}, nextAttemptAt={}, " +
                        "attemptStartedAt={}",
                delivery.getId(),
                delivery.getStatus(),
                delivery.getAttemptCount(),
                delivery.getNextAttemptAt(),
                delivery.getAttemptStartedAt()
        );

        WebhookEndpoint endpoint =
                webhookEndpointQueryRepository
                        .findById(
                                delivery.getWebhookEndpointId()
                        )
                        .orElse(null);

        if (endpoint == null) {

            log.warn(
                    "Webhook endpoint not found deliveryId={}, " +
                            "endpointId={}",
                    deliveryId,
                    delivery.getWebhookEndpointId()
            );

            fail(
                    delivery,
                    "Webhook endpoint not found"
            );
            return;
        }

        log.debug(
                "Resolved webhook endpoint deliveryId={}, " +
                        "endpointId={}, status={}, url={}",
                deliveryId,
                endpoint.getId(),
                endpoint.getStatus(),
                endpoint.getUrl()
        );

        if (endpoint.getStatus()
                != WebhookStatus.ACTIVE) {

            log.warn(
                    "Webhook endpoint is not active deliveryId={}, " +
                            "endpointId={}, status={}",
                    deliveryId,
                    endpoint.getId(),
                    endpoint.getStatus()
            );

            fail(
                    delivery,
                    "Webhook endpoint is not active"
            );
            return;
        }

        log.debug(
                "Sending webhook delivery deliveryId={}, " +
                        "attempt={}, url={}",
                deliveryId,
                delivery.getAttemptCount(),
                endpoint.getUrl()
        );

        WebhookHttpClientPort.WebhookResponse response =
                webhookHttpClientPort.send(
                        endpoint.getUrl(),
                        endpoint.getSecret(),
                        delivery.getPayload()
                );

        log.debug(
                "Webhook HTTP response deliveryId={}, " +
                        "statusCode={}, success={}, retryable={}, " +
                        "error={}",
                deliveryId,
                response.statusCode(),
                response.isSuccess(),
                response.isRetryable(),
                response.error()
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
        log.debug(
                "Handling webhook response deliveryId={}, " +
                        "attempt={}, statusCode={}, success={}, retryable={}",
                delivery.getId(),
                delivery.getAttemptCount(),
                response.statusCode(),
                response.isSuccess(),
                response.isRetryable()
        );

        if (response.isSuccess()) {

            log.debug(
                    "Webhook delivery succeeded deliveryId={}, " +
                            "attempt={}, statusCode={}",
                    delivery.getId(),
                    delivery.getAttemptCount(),
                    response.statusCode()
            );

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

        log.debug(
                "Webhook delivery failed deliveryId={}, " +
                        "attempt={}, retryable={}, error={}",
                delivery.getId(),
                delivery.getAttemptCount(),
                response.isRetryable(),
                error
        );

        if (!response.isRetryable()) {

            log.debug(
                    "Webhook delivery failure is non-retryable " +
                            "deliveryId={}, attempt={}",
                    delivery.getId(),
                    delivery.getAttemptCount()
            );

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
        int attemptCount =
                delivery.getAttemptCount();

        boolean shouldRetry =
                retryPolicy.shouldRetry(
                        attemptCount
                );

        log.debug(
                "Webhook retry decision deliveryId={}, " +
                        "attempt={}, shouldRetry={}",
                delivery.getId(),
                attemptCount,
                shouldRetry
        );

        if (!shouldRetry) {

            log.debug(
                    "Webhook retry limit reached deliveryId={}, " +
                            "attempt={}",
                    delivery.getId(),
                    attemptCount
            );

            fail(
                    delivery,
                    error
            );

            return;
        }

        var delay =
                retryPolicy.getDelay(
                        attemptCount
                );

        Instant nextAttemptAt =
                Instant.now().plus(delay);

        log.debug(
                "Scheduling webhook retry deliveryId={}, " +
                        "attempt={}, delay={}, nextAttemptAt={}",
                delivery.getId(),
                attemptCount,
                delay,
                nextAttemptAt
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
        log.debug(
                "Marking webhook delivery as failed " +
                        "deliveryId={}, attempt={}, error={}",
                delivery.getId(),
                delivery.getAttemptCount(),
                error
        );
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