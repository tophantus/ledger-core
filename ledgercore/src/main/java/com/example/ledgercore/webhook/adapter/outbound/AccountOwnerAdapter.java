package com.example.ledgercore.webhook.adapter.outbound;

import com.example.ledgercore.account.query.port.inbound.VerifyAccountOwnershipUseCase;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountOwnerAdapter
        implements AccountOwnerPort {

    private final VerifyAccountOwnershipUseCase
            verifyAccountOwnershipUseCase;

    @Override
    public void verifyOwnership(
            UUID userId,
            UUID accountId
    ) {
        verifyAccountOwnershipUseCase.execute(
                userId,
                accountId
        );
    }
}