package com.example.ledgercore.account.mapper;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.AccountSummaryResponse;
import com.example.ledgercore.account.query.projection.ProviderAccountProjection;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.projection.UserAccountProjection;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
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

    public static AccountResponse toResponse(
            GetUserAccountResult result
    ) {
        return new AccountResponse(
                result.accountId(),
                result.accountNo(),
                result.productId(),
                result.currency(),
                result.balance().toPlainString(),
                result.holdAmount().toPlainString(),
                result.getAvailableBalance().toPlainString(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    public static AccountResponse toResponse(
            ProviderAccountProjection projection
    ) {
        return new AccountResponse(
                projection.getAccountId(),
                projection.getAccountNo(),
                projection.getProductId(),
                projection.getCurrency(),
                projection.getBalance().toPlainString(),
                projection.getHoldAmount().toPlainString(),
                projection.getBalance()
                        .subtract(projection.getHoldAmount())
                        .toPlainString(),
                projection.getStatus(),
                projection.getCreatedAt(),
                projection.getUpdatedAt()
        );
    }

    public static AccountSummaryResponse toSummaryResponse(
            UserAccountProjection projection
    ) {
        return new AccountSummaryResponse(
                projection.getAccountId(),
                projection.getAccountNo(),
                projection.getProductId(),
                projection.getCurrency(),
                projection.getBalance().toPlainString(),
                projection.getHoldAmount().toPlainString(),
                projection.getBalance()
                        .subtract(projection.getHoldAmount())
                        .toPlainString(),
                projection.getStatus()
        );
    }
}