package com.example.ledgercore.credit.adapter.outbound.account;

import com.example.ledgercore.account.query.dto.GetAccountAvailableBalanceQuery;
import com.example.ledgercore.account.query.dto.GetAccountAvailableBalanceResult;
import com.example.ledgercore.account.query.port.inbound.GetAccountAvailableBalanceUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditAccountBalancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditAccountBalanceAdapter implements CreditAccountBalancePort {

    private final GetAccountAvailableBalanceUseCase
            getAccountAvailableBalanceUseCase;

    @Override
    public AccountBalance getActiveAccount(UUID accountId) {
        GetAccountAvailableBalanceResult result =
                getAccountAvailableBalanceUseCase.execute(
                        new GetAccountAvailableBalanceQuery(accountId)
                );

        return new AccountBalance(
                result.accountId(),
                result.availableBalance(),
                result.currency()
        );
    }
}