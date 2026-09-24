package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetUserAccountQuery;
import com.example.ledgercore.account.query.service.dto.GetUserAccountCriteria;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.port.inbound.GetUserAccountUseCase;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserAccountHandler implements GetUserAccountUseCase {

    private final GetUserAccountService getUserAccountService;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse execute(GetUserAccountQuery query) {
        GetUserAccountResult account = getUserAccountService.execute(
                new GetUserAccountCriteria(query.accountId())
        );

        if (!account.userId().equals(query.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return AccountMapper.toResponse(account);
    }
}