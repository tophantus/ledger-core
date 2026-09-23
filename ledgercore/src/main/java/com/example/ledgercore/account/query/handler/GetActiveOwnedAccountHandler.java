package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountQuery;
import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountResult;
import com.example.ledgercore.account.query.service.dto.GetUserAccountQuery;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.port.inbound.GetActiveOwnedAccountUseCase;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetActiveOwnedAccountHandler
        implements GetActiveOwnedAccountUseCase {

    private final GetUserAccountService getUserAccountService;

    @Override
    @Transactional(readOnly = true)
    public GetActiveOwnedAccountResult execute(
            GetActiveOwnedAccountQuery query
    ) {
        validateQuery(query);

        GetUserAccountResult account = getUserAccountService.execute(
                new GetUserAccountQuery(query.accountId())
        );

        if (!account.userId().equals(query.customerId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (account.status() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new GetActiveOwnedAccountResult(
                account.accountId(),
                account.userId(),
                account.productId(),
                account.getAvailableBalance(),
                account.currency(),
                account.status()
        );
    }

    private void validateQuery(
            GetActiveOwnedAccountQuery query
    ) {
        if (query == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.customerId() == null
                || query.accountId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}