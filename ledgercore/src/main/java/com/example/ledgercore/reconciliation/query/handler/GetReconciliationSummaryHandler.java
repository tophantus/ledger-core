package com.example.ledgercore.reconciliation.query.handler;

import com.example.ledgercore.reconciliation.query.dto.ReconciliationRunSummaryResponse;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationSummaryResponse;
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
    public ReconciliationSummaryResponse execute(
            LocalDate businessDate
    ) {

        LocalDate targetBusinessDate =
                businessDate != null
                        ? businessDate
                        : repository
                        .findLatestBusinessDate()
                        .orElse(null);

        if (targetBusinessDate == null) {
            return new ReconciliationSummaryResponse(
                    null,
                    List.of()
            );
        }

        List<ReconciliationRunSummaryResponse> runs =
                repository
                        .findSummaryByBusinessDate(
                                targetBusinessDate
                        )
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

        return new ReconciliationSummaryResponse(
                targetBusinessDate,
                runs
        );
    }
}