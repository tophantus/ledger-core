package com.example.ledgercore.reconciliation.adapter.outbound.ledger;

import com.example.ledgercore.ledger.query.port.inbound.GetJournalsForBalanceReconciliationUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalBalanceQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalBalanceReconciliationData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JournalBalanceQueryAdapter
        implements JournalBalanceQueryPort {

    private final GetJournalsForBalanceReconciliationUseCase useCase;

    @Override
    public List<JournalBalanceReconciliationData> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    ) {

        return useCase.execute(
                        businessDate,
                        lastProcessedId,
                        limit
                )
                .stream()
                .map(data -> new JournalBalanceReconciliationData(
                        data.id(),
                        data.debitTotal(),
                        data.creditTotal(),
                        data.businessDate()
                ))
                .toList();
    }
}