package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.reconciliation.command.port.inbound.HeartbeatReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationRunCommandRepository;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeartbeatReconciliationRunHandler
        implements HeartbeatReconciliationRunUseCase {

    private final ReconciliationRunCommandRepository repository;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            Instant heartbeatAt
    ) {

        ReconciliationRun run =
                repository.findById(runId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RECONCILIATION_RUN_NOT_FOUND
                                )
                        );

        run.heartbeat(heartbeatAt);
    }
}