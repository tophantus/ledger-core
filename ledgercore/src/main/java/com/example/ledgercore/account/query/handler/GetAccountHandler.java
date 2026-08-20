package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetAccountQuery;
import com.example.ledgercore.account.query.port.inbound.GetAccountUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAccountHandler implements GetAccountUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse execute(GetAccountQuery query) {
        Account account = accountQueryRepository.findById(query.accountId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
                );

        if (!account.getUserId().equals(query.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNo(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}