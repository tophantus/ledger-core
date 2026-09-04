package com.example.ledgercore.account.command.service;

import com.example.ledgercore.account.command.repository.AccountDailyBalanceCommandRepository;
import com.example.ledgercore.account.entity.AccountDailyBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountDailyBalanceService {

    private final AccountDailyBalanceCommandRepository repository;

    public void updateClosingBalance(
            UUID accountId,
            LocalDate businessDate,
            BigDecimal closingBalance
    ) {
        AccountDailyBalance.AccountDailyBalanceId id =
                new AccountDailyBalance.AccountDailyBalanceId(
                        accountId,
                        businessDate
                );

        AccountDailyBalance dailyBalance =
                repository.findById(id)
                        .orElseGet(() ->
                                AccountDailyBalance.builder()
                                        .id(id)
                                        .closingBalance(closingBalance)
                                        .build()
                        );

        dailyBalance.updateClosingBalance(closingBalance);

        repository.save(dailyBalance);
    }
}