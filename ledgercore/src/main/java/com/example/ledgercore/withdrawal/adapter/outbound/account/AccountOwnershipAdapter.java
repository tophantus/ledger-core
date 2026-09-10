package com.example.ledgercore.withdrawal.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.CheckAccountOwnershipUseCase;
import com.example.ledgercore.withdrawal.query.port.outbound.AccountOwnershipPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountOwnershipAdapter
        implements AccountOwnershipPort {

    private final CheckAccountOwnershipUseCase
            checkAccountOwnershipUseCase;

    @Override
    public boolean checkOwnership(
            UUID userId,
            UUID accountId
    ) {
        return checkAccountOwnershipUseCase.execute(
                userId,
                accountId
        );
    }
}