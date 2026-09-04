package com.example.ledgercore.account.command.repository;

import com.example.ledgercore.account.entity.AccountDailyBalance;
import com.example.ledgercore.account.entity.AccountDailyBalance.AccountDailyBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDailyBalanceCommandRepository
        extends JpaRepository<AccountDailyBalance, AccountDailyBalanceId> {
}