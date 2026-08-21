package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.port.inbound.GetAccountIdByAccountNoUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountIdByAccountNoHandler
        implements GetAccountIdByAccountNoUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public UUID execute(String accountNo) {
        return accountQueryRepository
                .findByAccountNo(accountNo)
                .map(Account::getId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.ACCOUNT_NOT_FOUND
                        )
                );
    }
}