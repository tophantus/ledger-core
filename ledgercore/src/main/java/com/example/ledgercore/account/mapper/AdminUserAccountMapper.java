package com.example.ledgercore.account.mapper;

import com.example.ledgercore.account.query.dto.AdminUserAccountDetailResponse;
import com.example.ledgercore.account.query.dto.AdminUserAccountResponse;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.projection.AdminUserAccountProjection;

public final class AdminUserAccountMapper {

    private AdminUserAccountMapper() {
    }

    public static AdminUserAccountResponse toResponse(
            AdminUserAccountProjection projection
    ) {
        return new AdminUserAccountResponse(
                projection.getAccountId(),
                projection.getUserId(),
                projection.getAccountNo(),
                projection.getProductId(),
                projection.getCurrency(),
                projection.getBalance().toPlainString(),
                projection.getHoldAmount().toPlainString(),
                projection.getBalance()
                        .subtract(projection.getHoldAmount())
                        .toPlainString(),
                projection.getStatus(),
                projection.getLedgerAccountId(),
                projection.getCreatedAt(),
                projection.getUpdatedAt()
        );
    }

    public static AdminUserAccountDetailResponse toDetailResponse(
            GetUserAccountResult account,
            AdminUserAccountDetailResponse.UserInfo user
    ) {
        return new AdminUserAccountDetailResponse(
                account.accountId(),
                account.accountNo(),
                account.productId(),
                account.currency(),
                account.balance().toPlainString(),
                account.holdAmount().toPlainString(),
                account.getAvailableBalance().toPlainString(),
                account.status(),
                account.ledgerAccountId(),
                account.createdAt(),
                account.updatedAt(),
                user
        );
    }
}