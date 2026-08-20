package com.example.ledgercore.account.adapter.outbound;

import com.example.ledgercore.account.command.port.outbound.UserAccountPort;
import com.example.ledgercore.user.query.port.inbound.CheckUserExistsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAccountAdapter implements UserAccountPort {

    private final CheckUserExistsUseCase checkUserExistsUseCase;

    @Override
    public boolean existsById(UUID userId) {
        return checkUserExistsUseCase.execute(userId);
    }
}