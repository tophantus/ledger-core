package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.query.dto.AccountSummaryResponse;
import com.example.ledgercore.account.query.dto.GetActiveUserAccountsQuery;
import com.example.ledgercore.account.query.port.inbound.GetUserActiveAccountsUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUserActiveAccountsHandler implements GetUserActiveAccountsUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> execute(
            GetActiveUserAccountsQuery query
    ) {
        return accountQueryRepository
                .findAllByUserIdAndStatusNot(
                        query.userId(),
                        AccountStatus.CLOSED
                )
                .stream()
                .map(AccountMapper::toSummaryResponse)
                .toList();
    }
}