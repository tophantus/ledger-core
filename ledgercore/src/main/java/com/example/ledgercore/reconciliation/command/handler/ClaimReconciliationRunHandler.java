package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;
import com.example.ledgercore.reconciliation.command.port.inbound.ClaimReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationRunCommandRepository;
import com.example.ledgercore.reconciliation.config.ReconciliationRunProperties;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClaimReconciliationRunHandler
        implements ClaimReconciliationRunUseCase {

    private final ReconciliationRunCommandRepository repository;
    private final ReconciliationRunProperties properties;

    @Override
    @Transactional
    public Optional<ClaimedReconciliationRun> execute(
            Instant claimAt
    ) {

        if (claimAt == null) {
            throw new IllegalArgumentException(
                    "claimAt must not be null"
            );
        }

        Instant staleBefore =
                claimAt.minus(
                        properties.getLeaseDuration()
                );

        Optional<ReconciliationRun> optionalRun =
                repository.findClaimableRun(staleBefore);

        if (optionalRun.isEmpty()) {
            return Optional.empty();
        }

        ReconciliationRun run = optionalRun.get();

        run.start(claimAt);

        return Optional.of(
                new ClaimedReconciliationRun(
                        run.getId(),
                        run.getBusinessDate(),
                        run.getType(),
                        run.getLastProcessedId(),
                        run.getProcessedCount()
                )
        );
    }
}