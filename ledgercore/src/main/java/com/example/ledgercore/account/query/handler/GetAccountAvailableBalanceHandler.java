package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.GetAccountAvailableBalanceQuery;
import com.example.ledgercore.account.query.dto.GetAccountAvailableBalanceResult;
import com.example.ledgercore.account.query.port.inbound.GetAccountAvailableBalanceUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAccountAvailableBalanceHandler
        implements GetAccountAvailableBalanceUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public GetAccountAvailableBalanceResult execute(
            GetAccountAvailableBalanceQuery query
    ) {
        Account account = accountQueryRepository
                .findById(query.accountId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new GetAccountAvailableBalanceResult(
                account.getId(),
                account.getAvailableBalance(),
                account.getCurrency()
        );
    }
}