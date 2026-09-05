package com.example.ledgercore.reconciliation.adapter.outbound.transaction;

import com.example.ledgercore.reconciliation.command.port.outbound.transaction.TransactionQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.transaction.TransactionReconciliationData;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionsForReconciliationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionQueryAdapter
        implements TransactionQueryPort {

    private final GetTransactionsForReconciliationUseCase useCase;

    @Override
    public List<TransactionReconciliationData> findBatch(
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
                .map(data -> new TransactionReconciliationData(
                        data.id(),
                        data.amount(),
                        data.businessDate()
                ))
                .toList();
    }
}
