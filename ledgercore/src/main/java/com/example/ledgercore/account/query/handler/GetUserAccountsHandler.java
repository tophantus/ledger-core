package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AccountSummaryResponse;
import com.example.ledgercore.account.query.dto.GetUserAccountsQuery;
import com.example.ledgercore.account.query.port.inbound.GetUserAccountsUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUserAccountsHandler implements GetUserAccountsUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> execute(
            GetUserAccountsQuery query
    ) {
        return accountQueryRepository
                .findAllByUserId(query.userId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AccountSummaryResponse toResponse(Account account) {
        return new AccountSummaryResponse(
                account.getId(),
                account.getAccountNo(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus()
        );
    }
}