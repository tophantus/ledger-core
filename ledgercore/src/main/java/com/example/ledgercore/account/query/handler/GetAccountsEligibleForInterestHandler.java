package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountInterestEligibility;
import com.example.ledgercore.account.query.port.inbound.GetAccountsEligibleForInterestUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
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
public class GetAccountsEligibleForInterestHandler
        implements GetAccountsEligibleForInterestUseCase {

    private final AccountQueryRepository accountQueryRepository;

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

        Pageable pageable =
                PageRequest.of(0, batchSize);

        return accountQueryRepository
                .findInterestEligibleBatch(
                        AccountStatus.ACTIVE,
                        lastProcessedId,
                        pageable
                )
                .stream()
                .map(this::toEligibility)
                .toList();
    }

    private AccountInterestEligibility toEligibility(
            Account account
    ) {
        return new AccountInterestEligibility(
                account.getId(),
                account.getProductId(),
                account.getCurrency()
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