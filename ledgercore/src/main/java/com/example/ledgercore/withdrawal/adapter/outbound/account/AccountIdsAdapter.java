package com.example.ledgercore.withdrawal.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.GetAccountIdsByUserUseCase;
import com.example.ledgercore.withdrawal.query.port.outbound.AccountIdsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountIdsAdapter
        implements AccountIdsPort {

    private final GetAccountIdsByUserUseCase
            getAccountIdsByUserUseCase;

    @Override
    public List<UUID> getAccountIdsByUser(
            UUID userId
    ) {
        return getAccountIdsByUserUseCase.execute(
                userId
        );
    }
}