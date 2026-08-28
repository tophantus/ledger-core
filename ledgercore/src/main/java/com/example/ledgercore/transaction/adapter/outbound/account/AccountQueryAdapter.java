package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.GetAccountNoUseCase;
import com.example.ledgercore.transaction.query.port.outbound.AccountQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountQueryAdapter
        implements AccountQueryPort {

    private final GetAccountNoUseCase getAccountNoUseCase;

    @Override
    public String getAccountNoByAccountId(
            UUID accountId
    ) {
        return getAccountNoUseCase.execute(accountId);
    }
}