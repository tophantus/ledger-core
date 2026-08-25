package com.example.ledgercore.outbox.query.handler;

import com.example.ledgercore.outbox.entity.OutboxEvent;
import com.example.ledgercore.outbox.query.port.inbound.GetPendingOutboxEventsUseCase;
import com.example.ledgercore.outbox.query.repository.OutboxEventQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPendingOutboxEventsHandler
        implements GetPendingOutboxEventsUseCase {

    private final OutboxEventQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> execute(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "Batch size must be greater than zero"
            );
        }

        return repository.findByPublishedFalseOrderByCreatedAtAsc(
                PageRequest.of(0, batchSize)
        );
    }
}