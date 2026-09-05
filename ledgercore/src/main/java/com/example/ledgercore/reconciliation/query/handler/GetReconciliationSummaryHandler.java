package com.example.ledgercore.reconciliation.query.handler;

import com.example.ledgercore.reconciliation.query.dto.ReconciliationRunSummaryResponse;
import com.example.ledgercore.reconciliation.query.port.inbound.GetReconciliationSummaryUseCase;
import com.example.ledgercore.reconciliation.query.repository.ReconciliationRunQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetReconciliationSummaryHandler
        implements GetReconciliationSummaryUseCase {

    private final ReconciliationRunQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationRunSummaryResponse> execute(
            LocalDate businessDate
    ) {

        return repository
                .findSummaryByBusinessDate(businessDate)
                .stream()
                .map(data ->
                        new ReconciliationRunSummaryResponse(
                                data.getId(),
                                data.getType(),
                                data.getStatus(),
                                data.getProcessedCount()
                        )
                )
                .toList();
    }
}