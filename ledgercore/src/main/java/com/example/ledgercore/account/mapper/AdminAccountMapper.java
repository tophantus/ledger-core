package com.example.ledgercore.account.mapper;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AdminAccountDetailResponse;
import com.example.ledgercore.account.query.dto.AdminAccountResponse;

public final class AdminAccountMapper {

    private AdminAccountMapper() {
    }

    public static AdminAccountResponse toResponse(
            Account account
    ) {
        return new AdminAccountResponse(
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
                account.getLedgerAccountId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public static AdminAccountDetailResponse toDetailResponse(
            Account account,
            AdminAccountDetailResponse.UserInfo user
    ) {
        return new AdminAccountDetailResponse(
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
                account.getLedgerAccountId(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                user
        );
    }
}