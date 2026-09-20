package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountQuery;
import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountResult;
import com.example.ledgercore.account.query.port.inbound.GetActiveOwnedAccountUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetActiveOwnedAccountHandler
        implements GetActiveOwnedAccountUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public GetActiveOwnedAccountResult execute(
            GetActiveOwnedAccountQuery query
    ) {
        validateQuery(query);

        Account account =
                accountQueryRepository
                        .findByIdAndUserId(
                                query.accountId(),
                                query.customerId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new GetActiveOwnedAccountResult(
                account.getId(),
                account.getUserId(),
                account.getProductId(),
                account.getStatus()
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