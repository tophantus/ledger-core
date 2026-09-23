package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.port.inbound.CheckUserAccountOwnershipUseCase;
import com.example.ledgercore.account.query.repository.UserAccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckUserAccountOwnershipHandler
        implements CheckUserAccountOwnershipUseCase {

    private final UserAccountQueryRepository userAccountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean execute(
            UUID userId,
            UUID accountId
    ) {
        if (userId == null || accountId == null) {
            return false;
        }

        return userAccountQueryRepository.existsByAccountIdAndUserId(
                accountId,
                userId
        );
    }
}