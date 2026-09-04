package com.example.ledgercore.ledger.query.handler;

import com.example.ledgercore.ledger.query.dto.ReconciliationJournalData;
import com.example.ledgercore.ledger.query.port.inbound.GetJournalsForReconciliationUseCase;
import com.example.ledgercore.ledger.query.repository.JournalEntryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetJournalsForReconciliationHandler
        implements GetJournalsForReconciliationUseCase {

    private final JournalEntryQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationJournalData> execute(
            List<UUID> transactionIds
    ) {

        if (transactionIds == null || transactionIds.isEmpty()) {
            return List.of();
        }

        return repository
                .findForReconciliation(transactionIds)
                .stream()
                .map(row -> new ReconciliationJournalData(
                        row.getId(),
                        row.getTransactionId(),
                        row.getDebitTotal(),
                        row.getCreditTotal(),
                        row.getBusinessDate()
                ))
                .toList();
    }
}