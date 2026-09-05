package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.dto.AccountDailyBalanceReconciliationData;
import com.example.ledgercore.account.query.port.inbound.GetAccountDailyBalancesForReconciliationUseCase;
import com.example.ledgercore.account.query.repository.AccountDailyBalanceQueryRepository;
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
public class GetAccountDailyBalancesForReconciliationHandler
        implements GetAccountDailyBalancesForReconciliationUseCase {

    private final AccountDailyBalanceQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountDailyBalanceReconciliationData> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    ) {

        Pageable pageable =
                PageRequest.of(
                        0,
                        limit,
                        Sort.by(
                                Sort.Direction.ASC,
                                "accountId"
                        )
                );

        return repository
                .findForReconciliation(
                        businessDate,
                        lastProcessedId,
                        pageable
                )
                .stream()
                .map(data ->
                        new AccountDailyBalanceReconciliationData(
                                data.getAccountId(),
                                data.getBusinessDate(),
                                data.getOpeningBalance(),
                                data.getClosingBalance()
                        )
                )
                .toList();
    }
}