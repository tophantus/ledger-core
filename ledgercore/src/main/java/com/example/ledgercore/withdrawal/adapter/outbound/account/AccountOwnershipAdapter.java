package com.example.ledgercore.withdrawal.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.CheckUserAccountOwnershipUseCase;
import com.example.ledgercore.withdrawal.query.port.outbound.AccountOwnershipPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountOwnershipAdapter
        implements AccountOwnershipPort {

    private final CheckUserAccountOwnershipUseCase
            checkUserAccountOwnershipUseCase;

    @Override
    public boolean checkOwnership(
            UUID userId,
            UUID accountId
    ) {
        return checkUserAccountOwnershipUseCase.execute(
                userId,
                accountId
        );
    }
}