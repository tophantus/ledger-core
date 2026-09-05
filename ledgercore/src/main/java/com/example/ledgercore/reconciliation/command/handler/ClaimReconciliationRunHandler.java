package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.port.inbound.ClaimReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationRunCommandRepository;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ClaimReconciliationRunHandler
        implements ClaimReconciliationRunUseCase {

    private static final Duration HEARTBEAT_TIMEOUT =
            Duration.ofMinutes(5);

    private final ReconciliationRunCommandRepository repository;

    @Override
    @Transactional
    public ReconciliationRun execute(Instant now) {

        Instant staleBefore =
                now.minus(HEARTBEAT_TIMEOUT);

        ReconciliationRun run =
                repository.findClaimableRun(staleBefore)
                        .orElse(null);

        if (run == null) {
            return null;
        }

        run.start(now);

        return run;
    }
}