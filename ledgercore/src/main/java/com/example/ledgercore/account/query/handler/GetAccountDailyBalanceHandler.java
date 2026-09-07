package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.dto.AccountDailyBalanceResponse;
import com.example.ledgercore.account.query.port.inbound.GetAccountDailyBalanceUseCase;
import com.example.ledgercore.account.query.repository.AccountDailyBalanceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountDailyBalanceHandler
        implements GetAccountDailyBalanceUseCase {

    private final AccountDailyBalanceQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public AccountDailyBalanceResponse execute(
            UUID accountId,
            LocalDate businessDate
    ) {
        if (accountId == null) {
            throw new IllegalArgumentException(
                    "accountId must not be null"
            );
        }

        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        BigDecimal closingBalance =
                repository.findEffectiveClosingBalance(
                        accountId,
                        businessDate
                );

        return new AccountDailyBalanceResponse(
                accountId,
                businessDate,
                closingBalance
        );
    }
}