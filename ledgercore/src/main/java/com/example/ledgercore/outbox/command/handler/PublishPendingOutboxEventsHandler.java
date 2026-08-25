package com.example.ledgercore.outbox.command.handler;

import com.example.ledgercore.outbox.command.port.inbound.PublishOutboxEventUseCase;
import com.example.ledgercore.outbox.command.port.inbound.PublishPendingOutboxEventsUseCase;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import com.example.ledgercore.outbox.query.repository.OutboxEventQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublishPendingOutboxEventsHandler
        implements PublishPendingOutboxEventsUseCase {

    private final OutboxEventQueryRepository outboxEventQueryRepository;
    private final PublishOutboxEventUseCase publishOutboxEventUseCase;

    @Override
    public int execute(int batchSize) {

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "Batch size must be greater than zero"
            );
        }

        List<OutboxEvent> events =
                outboxEventQueryRepository
                        .findByPublishedFalseOrderByCreatedAtAsc(
                                PageRequest.of(0, batchSize)
                        );

        int publishedCount = 0;

        for (OutboxEvent event : events) {
            try {
                publishOutboxEventUseCase.execute(event);
                publishedCount++;
            } catch (Exception ex) {
                log.warn(
                        "Failed to publish outbox event eventId={}",
                        event.getId(),
                        ex
                );
            }
        }

        return publishedCount;
    }
}