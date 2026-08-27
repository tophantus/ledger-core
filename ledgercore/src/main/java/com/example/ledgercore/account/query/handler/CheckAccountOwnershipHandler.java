package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.port.inbound.CheckAccountOwnershipUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckAccountOwnershipHandler
        implements CheckAccountOwnershipUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean execute(
            UUID userId,
            UUID accountId
    ) {
        return accountQueryRepository
                .existsByIdAndUserId(
                        accountId,
                        userId
                );
    }
}