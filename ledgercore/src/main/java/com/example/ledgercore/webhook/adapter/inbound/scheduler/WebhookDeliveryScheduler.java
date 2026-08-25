package com.example.ledgercore.webhook.adapter.inbound.scheduler;

import com.example.ledgercore.webhook.command.port.inbound.ProcessWebhookDeliveryUseCase;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.query.repository.WebhookDeliveryQueryRepository;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryScheduler {

    private static final int BATCH_SIZE = 100;

    private final WebhookDeliveryQueryRepository
            webhookDeliveryQueryRepository;

    private final ProcessWebhookDeliveryUseCase
            processWebhookDeliveryUseCase;

    @Scheduled(fixedDelay = 1000)
    public void processDeliveries() {

        List<UUID> deliveryIds =
                webhookDeliveryQueryRepository
                        .findDueDeliveries(
                                WebhookDeliveryStatus.PENDING,
                                WebhookDeliveryStatus.RETRYING,
                                Instant.now(),
                                PageRequest.of(
                                        0,
                                        BATCH_SIZE
                                )
                        )
                        .stream()
                        .map(WebhookDelivery::getId
                        )
                        .toList();

        for (UUID deliveryId : deliveryIds) {
            try {
                processWebhookDeliveryUseCase.execute(
                        deliveryId
                );
            } catch (Exception ex) {
                log.error(
                        "Failed to process webhook delivery id={}",
                        deliveryId,
                        ex
                );
            }
        }
    }
}