package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.user.query.port.inbound.CheckUserExistsUseCase;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckUserExistsHandler implements CheckUserExistsUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean execute(UUID userId) {
        return userQueryRepository.existsById(userId);
    }
}