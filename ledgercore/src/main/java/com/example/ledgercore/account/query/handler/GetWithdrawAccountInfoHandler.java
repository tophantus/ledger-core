package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;
import com.example.ledgercore.account.query.port.inbound.GetWithdrawAccountInfoUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetWithdrawAccountInfoHandler
        implements GetWithdrawAccountInfoUseCase {

    private final AccountQueryRepository accountQueryRepository;
    
    @Override
    @Transactional(readOnly = true)
    public AccountWithdrawInfo execute(
            UUID userId,
            UUID accountId
    ) {
        Account account =
                accountQueryRepository
                        .findByIdAndUserId(accountId, userId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        if (account.getStatus()
                != com.example.ledgercore.account.enums.AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new AccountWithdrawInfo(
                account.getId(),
                account.getCurrency(),
                account.getBalance()
        );
    }
}