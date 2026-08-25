package com.example.ledgercore.webhook.adapter.inbound.scheduler;

import com.example.ledgercore.webhook.command.repository.WebhookDeliveryCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.query.repository.WebhookDeliveryQueryRepository;
import com.example.ledgercore.webhook.service.WebhookRetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryRecoveryScheduler {

    private static final int BATCH_SIZE = 100;

    private final WebhookDeliveryQueryRepository
            webhookDeliveryQueryRepository;

    private final WebhookDeliveryCommandRepository
            webhookDeliveryCommandRepository;

    private final WebhookRetryPolicy retryPolicy;

    @Scheduled(fixedDelay = 10_000)
    public void recoverStaleDeliveries() {

        Instant threshold =
                Instant.now()
                        .minus(
                                retryPolicy.getProcessingTimeout()
                        );

        List<WebhookDelivery> deliveries =
                webhookDeliveryQueryRepository
                        .findByStatusAndAttemptStartedAtBefore(
                                WebhookDeliveryStatus.PROCESSING,
                                threshold,
                                PageRequest.of(
                                        0,
                                        BATCH_SIZE
                                )
                        );

        for (WebhookDelivery delivery : deliveries) {

            Instant nextAttemptAt =
                    Instant.now()
                            .plus(
                                    retryPolicy.getDelay(
                                            delivery.getAttemptCount()
                                    )
                            );

            int updated =
                    webhookDeliveryCommandRepository
                            .recoverStaleProcessing(
                                    delivery.getId(),
                                    WebhookDeliveryStatus.PROCESSING,
                                    WebhookDeliveryStatus.RETRYING,
                                    nextAttemptAt,
                                    "Webhook delivery processing timed out"
                            );

            if (updated == 1) {
                log.warn(
                        "Recovered stale webhook delivery " +
                                "id={}, attempt={}",
                        delivery.getId(),
                        delivery.getAttemptCount()
                );
            }
        }
    }
}