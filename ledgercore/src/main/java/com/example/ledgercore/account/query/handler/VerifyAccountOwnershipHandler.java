package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.port.inbound.VerifyAccountOwnershipUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyAccountOwnershipHandler
        implements VerifyAccountOwnershipUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public void execute(
            UUID userId,
            UUID accountId
    ) {
        boolean exists = accountQueryRepository
                .existsByIdAndUserId(
                        accountId,
                        userId
                );

        if (!exists) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_FOUND
            );
        }
    }
}