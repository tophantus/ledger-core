package com.example.ledgercore.webhook.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.CheckUserAccountOwnershipUseCase;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.port.outbound.WebhookAccountOwnerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebhookAccountOwnerAdapter
        implements WebhookAccountOwnerPort {

    private final CheckUserAccountOwnershipUseCase
            checkUserAccountOwnershipUseCase;

    @Override
    public void verifyOwnership(
            UUID userId,
            UUID accountId
    ) {
        boolean isOwner = checkUserAccountOwnershipUseCase.execute(
                userId,
                accountId
        );

        if (!isOwner) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}