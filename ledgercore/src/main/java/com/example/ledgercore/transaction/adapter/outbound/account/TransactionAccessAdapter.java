package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.CheckAccountOwnershipUseCase;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.query.port.outbound.TransactionAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionAccessAdapter
        implements TransactionAccessPort {

    private final CheckAccountOwnershipUseCase
            checkAccountOwnershipUseCase;

    @Override
    public void verifyAccess(
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

    @Override
    public void verifyAccess(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {
        if (sourceAccountId != null
                && checkAccountOwnershipUseCase.execute(
                    userId,
                    sourceAccountId)) {
            return;
        }

        if (destinationAccountId != null
                && checkAccountOwnershipUseCase.execute(
                    userId,
                    destinationAccountId)) {
            return;
        }

        throw new BusinessException(
                ErrorCode.ACCESS_DENIED
        );
    }
}