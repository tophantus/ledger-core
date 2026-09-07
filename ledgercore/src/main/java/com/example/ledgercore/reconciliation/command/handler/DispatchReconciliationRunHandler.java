package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;
import com.example.ledgercore.reconciliation.command.port.inbound.DispatchReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.service.ReconciliationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DispatchReconciliationRunHandler
        implements DispatchReconciliationRunUseCase {

    private final ReconciliationDispatcher dispatcher;

    @Override
    public void execute(ClaimedReconciliationRun run) {
        dispatcher.dispatch(run);
    }
}