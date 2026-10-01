package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountInterestEligibility;
import com.example.ledgercore.account.query.port.inbound.GetAccountsEligibleForInterestUseCase;
import com.example.ledgercore.account.query.projection.AccountInterestBalanceProjection;
import com.example.ledgercore.account.query.repository.AccountDailyBalanceQueryRepository;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAccountsEligibleForInterestHandler
        implements GetAccountsEligibleForInterestUseCase {

    private final AccountQueryRepository accountQueryRepository;

    private final AccountDailyBalanceQueryRepository
            accountDailyBalanceQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountInterestEligibility> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    ) {
        validate(
                businessDate,
                batchSize
        );

        List<Account> accounts =
                accountQueryRepository.findInterestEligibleBatch(
                        AccountStatus.ACTIVE,
                        lastProcessedId,
                        PageRequest.of(0, batchSize)
                );

        if (accounts.isEmpty()) {
            return List.of();
        }

        List<UUID> accountIds =
                accounts.stream()
                        .map(Account::getId)
                        .toList();

        Map<UUID, BigDecimal> closingBalances =
                accountDailyBalanceQueryRepository
                        .findEffectiveClosingBalances(
                                accountIds,
                                businessDate
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                AccountInterestBalanceProjection::getAccountId,
                                AccountInterestBalanceProjection::getClosingBalance
                        ));

        return accounts.stream()
                .map(account -> toEligibility(
                        account,
                        closingBalances.getOrDefault(
                                account.getId(),
                                BigDecimal.ZERO
                        )
                ))
                .toList();
    }

    private AccountInterestEligibility toEligibility(
            Account account,
            BigDecimal closingBalance
    ) {
        return new AccountInterestEligibility(
                account.getId(),
                account.getProductId(),
                account.getCurrency(),
                closingBalance
        );
    }

    private void validate(
            LocalDate businessDate,
            int batchSize
    ) {
        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than zero"
            );
        }
    }
}