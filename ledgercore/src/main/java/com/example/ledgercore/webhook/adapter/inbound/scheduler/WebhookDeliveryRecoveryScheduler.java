package com.example.ledgercore.webhook.adapter.inbound.scheduler;

import com.example.ledgercore.webhook.command.repository.WebhookDeliveryCommandRepository;
import com.example.ledgercore.webhook.config.WebhookDeliveryRecoverySchedulerProperties;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.query.repository.WebhookDeliveryQueryRepository;
import com.example.ledgercore.webhook.service.WebhookRetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "webhook.delivery.recovery.scheduler",
        name = "enabled",
        havingValue = "true"
)
public class WebhookDeliveryRecoveryScheduler {

    private final WebhookDeliveryQueryRepository
            webhookDeliveryQueryRepository;

    private final WebhookDeliveryCommandRepository
            webhookDeliveryCommandRepository;

    private final WebhookRetryPolicy retryPolicy;

    private final WebhookDeliveryRecoverySchedulerProperties
            schedulerProperties;

    private final Clock clock;

    @Scheduled(
            fixedDelayString =
                    "${webhook.delivery.recovery.scheduler.fixed-delay:10s}"
    )
    public void recoverStaleDeliveries() {

        Instant now = Instant.now(clock);

        Instant threshold =
                now.minus(
                        retryPolicy.getProcessingTimeout()
                );

        List<WebhookDelivery> deliveries =
                webhookDeliveryQueryRepository
                        .findByStatusAndAttemptStartedAtBefore(
                                WebhookDeliveryStatus.PROCESSING,
                                threshold,
                                PageRequest.of(
                                        0,
                                        schedulerProperties.getBatchSize()
                                )
                        );

        int recoveredCount = 0;

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
                recoveredCount++;
            }
        }

        if (recoveredCount > 0) {
            log.info(
                    "Recovered stale webhook deliveries: recovered={}, total={}",
                    recoveredCount,
                    deliveries.size()
            );
        }
    }
}