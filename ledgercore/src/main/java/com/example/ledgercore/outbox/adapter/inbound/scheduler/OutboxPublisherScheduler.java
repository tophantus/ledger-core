package com.example.ledgercore.outbox.adapter.inbound.scheduler;

import com.example.ledgercore.outbox.command.port.inbound.PublishPendingOutboxEventsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private static final int BATCH_SIZE = 100;

    private final PublishPendingOutboxEventsUseCase
            publishPendingOutboxEventsUseCase;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        int published =
                publishPendingOutboxEventsUseCase.execute(
                        BATCH_SIZE
                );

        if (published > 0) {
            log.debug(
                    "Published {} pending outbox events",
                    published
            );
        }
    }
}