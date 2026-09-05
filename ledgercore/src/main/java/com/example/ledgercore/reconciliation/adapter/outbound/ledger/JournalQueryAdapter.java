package com.example.ledgercore.reconciliation.adapter.outbound.ledger;

import com.example.ledgercore.ledger.query.port.inbound.GetJournalsForReconciliationUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalReconciliationData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JournalQueryAdapter
        implements JournalQueryPort {

    private final GetJournalsForReconciliationUseCase useCase;

    @Override
    public List<JournalReconciliationData> findByTransactionIds(
            List<UUID> transactionIds
    ) {

        return useCase.execute(transactionIds)
                .stream()
                .map(data -> new JournalReconciliationData(
                        data.id(),
                        data.transactionId(),
                        data.debitTotal(),
                        data.creditTotal(),
                        data.businessDate()
                ))
                .toList();
    }
}