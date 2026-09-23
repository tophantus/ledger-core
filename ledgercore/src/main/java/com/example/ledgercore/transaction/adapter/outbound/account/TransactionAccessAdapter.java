package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.CheckUserAccountOwnershipUseCase;
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

    private final CheckUserAccountOwnershipUseCase
            checkUserAccountOwnershipUseCase;

    @Override
    public void verifyAccess(
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

    @Override
    public void verifyAccess(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {
        if (sourceAccountId != null
                && checkUserAccountOwnershipUseCase.execute(
                    userId,
                    sourceAccountId)) {
            return;
        }

        if (destinationAccountId != null
                && checkUserAccountOwnershipUseCase.execute(
                    userId,
                    destinationAccountId)) {
            return;
        }

        throw new BusinessException(
                ErrorCode.ACCESS_DENIED
        );
    }
}