package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.reconciliation.command.port.inbound.GetReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationRunCommandRepository;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetReconciliationRunHandler
        implements GetReconciliationRunUseCase {

    private final ReconciliationRunCommandRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ReconciliationRun execute(UUID runId) {

        return repository.findById(runId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.RECONCILIATION_RUN_NOT_FOUND
                        )
                );
    }
}