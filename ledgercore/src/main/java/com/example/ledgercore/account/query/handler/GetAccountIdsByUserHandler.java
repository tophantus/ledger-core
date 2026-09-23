package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.port.inbound.GetAccountIdsByUserUseCase;
import com.example.ledgercore.account.query.repository.UserAccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountIdsByUserHandler
        implements GetAccountIdsByUserUseCase {

    private final UserAccountQueryRepository userAccountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UUID> execute(UUID userId) {
        return userAccountQueryRepository.findAccountIdsByUserId(userId);
    }
}