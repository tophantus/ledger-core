package com.example.ledgercore.webhook.adapter.outbound;

import com.example.ledgercore.account.query.port.inbound.CheckAccountOwnershipUseCase;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountOwnerAdapter
        implements AccountOwnerPort {

    private final CheckAccountOwnershipUseCase
            checkAccountOwnershipUseCase;

    @Override
    public void verifyOwnership(
            UUID userId,
            UUID accountId
    ) {
        boolean isOwner = checkAccountOwnershipUseCase.execute(
                userId,
                accountId
        );

        if (!isOwner) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}