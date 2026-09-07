package com.example.ledgercore.reconciliation.adapter.inbound.scheduler;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;
import com.example.ledgercore.reconciliation.command.port.inbound.ClaimReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.DispatchReconciliationRunUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@ConditionalOnProperty(
        prefix = "reconciliation.scheduler",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class ReconciliationScheduler {

    private final ClaimReconciliationRunUseCase
            claimReconciliationRunUseCase;

    private final DispatchReconciliationRunUseCase
            dispatchReconciliationRunUseCase;

    @Scheduled(
            fixedDelayString =
                    "${reconciliation.scheduler.fixed-delay:1000}"
    )
    public void process() {

        Optional<ClaimedReconciliationRun> optionalRun =
                claimReconciliationRunUseCase.execute(
                        Instant.now()
                );

        if (optionalRun.isEmpty()) {
            return;
        }

        dispatchReconciliationRunUseCase.execute(
                optionalRun.get()
        );
    }
}