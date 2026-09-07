package com.example.ledgercore.interest.adapter.outbound.account;

import com.example.ledgercore.account.query.dto.AccountInterestEligibility;
import com.example.ledgercore.account.query.port.inbound.GetAccountsEligibleForInterestUseCase;
import com.example.ledgercore.interest.command.port.outbound.account.InterestEligibleAccount;
import com.example.ledgercore.interest.command.port.outbound.account.InterestEligibleAccountQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountInterestEligibleAdapter
        implements InterestEligibleAccountQueryPort {

    private final GetAccountsEligibleForInterestUseCase
            getAccountsEligibleForInterestUseCase;

    @Override
    public List<InterestEligibleAccount> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    ) {
        return getAccountsEligibleForInterestUseCase
                .execute(
                        businessDate,
                        lastProcessedId,
                        batchSize
                )
                .stream()
                .map(this::toInterestEligibleAccount)
                .toList();
    }

    private InterestEligibleAccount toInterestEligibleAccount(
            AccountInterestEligibility account
    ) {
        return new InterestEligibleAccount(
                account.accountId(),
                account.productId(),
                account.currency()
        );
    }
}