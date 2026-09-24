package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;
import com.example.ledgercore.account.query.service.dto.GetUserAccountCriteria;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.port.inbound.GetWithdrawAccountInfoUseCase;
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

    private final GetUserAccountService getUserAccountService;
    
    @Override
    @Transactional(readOnly = true)
    public AccountWithdrawInfo execute(
            UUID accountId
    ) {
        GetUserAccountResult account = getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        );

        if (account.status()
                != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new AccountWithdrawInfo(
                account.accountId(),
                account.userId(),
                account.currency(),
                account.getAvailableBalance()
        );
    }
}