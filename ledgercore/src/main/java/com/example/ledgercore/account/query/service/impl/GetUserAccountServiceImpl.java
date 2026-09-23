package com.example.ledgercore.account.query.service.impl;

import com.example.ledgercore.account.query.service.dto.GetUserAccountQuery;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.projection.UserAccountProjection;
import com.example.ledgercore.account.query.repository.UserAccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserAccountServiceImpl
        implements GetUserAccountService {

    private final UserAccountQueryRepository userAccountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public GetUserAccountResult execute(GetUserAccountQuery query) {

        UserAccountProjection projection =
                userAccountQueryRepository
                        .findUserAccount(query.accountId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        return new GetUserAccountResult(
                projection.getAccountId(),
                projection.getUserId(),
                projection.getProductId(),
                projection.getAccountNo(),
                projection.getCurrency(),
                projection.getBalance(),
                projection.getHoldAmount(),
                projection.getStatus(),
                projection.getVersion(),
                projection.getLedgerAccountId(),
                projection.getCreatedAt(),
                projection.getUpdatedAt()
        );
    }
}