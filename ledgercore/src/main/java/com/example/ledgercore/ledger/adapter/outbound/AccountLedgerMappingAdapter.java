package com.example.ledgercore.ledger.adapter.outbound;

import com.example.ledgercore.account.query.port.inbound.GetAccountLedgerAccountIdUseCase;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountLedgerMappingAdapter
        implements AccountLedgerMappingPort {

    private final GetAccountLedgerAccountIdUseCase
            getAccountLedgerAccountIdUseCase;

    @Override
    public UUID getLedgerAccountId(UUID accountId) {
        return getAccountLedgerAccountIdUseCase.execute(accountId);
    }
}