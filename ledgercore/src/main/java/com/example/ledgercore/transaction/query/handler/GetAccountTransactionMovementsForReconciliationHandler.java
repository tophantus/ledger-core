package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.query.dto.AccountTransactionMovementData;
import com.example.ledgercore.transaction.query.port.inbound.GetAccountTransactionMovementsForReconciliationUseCase;
import com.example.ledgercore.transaction.query.repository.AccountTransactionMovementProjection;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountTransactionMovementsForReconciliationHandler
        implements GetAccountTransactionMovementsForReconciliationUseCase {

    private final TransactionQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountTransactionMovementData> execute(
            LocalDate businessDate,
            List<UUID> accountIds
    ) {

        if (accountIds == null || accountIds.isEmpty()) {
            return List.of();
        }

        return repository
                .findAccountMovementsForReconciliation(
                        businessDate,
                        accountIds,
                        TransactionStatus.COMPLETED
                )
                .stream()
                .map(this::toData)
                .toList();
    }

    private AccountTransactionMovementData toData(
            AccountTransactionMovementProjection data
    ) {

        return new AccountTransactionMovementData(
                data.getAccountId(),
                data.getTotalCredit(),
                data.getTotalDebit()
        );
    }
}