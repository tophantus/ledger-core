package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.port.inbound.StartBusinessDayReconciliationUseCase;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationRunCommandRepository;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StartBusinessDayReconciliationHandler
        implements StartBusinessDayReconciliationUseCase {

    private final ReconciliationRunCommandRepository reconciliationRunCommandRepository;

    @Override
    @Transactional
    public void execute(LocalDate businessDate) {

        for (ReconciliationType type : ReconciliationType.values()) {

            reconciliationRunCommandRepository.insertIfNotExists(
                    businessDate,
                    type.name()
            );
        }
    }
}