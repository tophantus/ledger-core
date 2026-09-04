package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.query.dto.ReconciliationTransactionData;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionsForReconciliationUseCase;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTransactionsForReconciliationHandler
        implements GetTransactionsForReconciliationUseCase {

    private final TransactionQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationTransactionData> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    ) {

        Pageable pageable =
                PageRequest.of(
                        0,
                        limit,
                        Sort.by(Sort.Direction.ASC, "id")
                );

        return repository
                .findForReconciliation(
                        businessDate,
                        lastProcessedId,
                        pageable
                )
                .stream()
                .map(this::toData)
                .toList();
    }

    private ReconciliationTransactionData toData(
            MoneyTransaction transaction
    ) {
        return new ReconciliationTransactionData(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getBusinessDate()
        );
    }
}