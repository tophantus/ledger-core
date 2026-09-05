package com.example.ledgercore.ledger.query.handler;

import com.example.ledgercore.ledger.query.dto.JournalBalanceReconciliationData;
import com.example.ledgercore.ledger.query.port.inbound.GetJournalsForBalanceReconciliationUseCase;
import com.example.ledgercore.ledger.query.repository.JournalEntryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetJournalsForBalanceReconciliationHandler
        implements GetJournalsForBalanceReconciliationUseCase {

    private final JournalEntryQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<JournalBalanceReconciliationData> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    ) {

        Pageable pageable =
                PageRequest.of(0, limit);


        return repository
                .findForBalanceReconciliation(
                        businessDate,
                        lastProcessedId,
                        pageable
                )
                .stream()
                .map(data -> new JournalBalanceReconciliationData(
                        data.getId(),
                        data.getDebitTotal(),
                        data.getCreditTotal(),
                        data.getBusinessDate()
                ))
                .toList();
    }
}