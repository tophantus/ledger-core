package com.example.ledgercore.account.mapper;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.AccountSummaryResponse;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNo(),
                account.getProductId(),
                account.getCurrency(),
                account.getBalance().toPlainString(),
                account.getHoldAmount().toPlainString(),
                account.getBalance()
                        .subtract(account.getHoldAmount())
                        .toPlainString(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public static AccountSummaryResponse toSummaryResponse(
            Account account
    ) {
        return new AccountSummaryResponse(
                account.getId(),
                account.getAccountNo(),
                account.getProductId(),
                account.getCurrency(),
                account.getBalance().toPlainString(),
                account.getHoldAmount().toPlainString(),
                account.getBalance()
                        .subtract(account.getHoldAmount())
                        .toPlainString(),
                account.getStatus()
        );
    }
}