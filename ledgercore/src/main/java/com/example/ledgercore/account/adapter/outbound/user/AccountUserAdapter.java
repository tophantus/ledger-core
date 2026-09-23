package com.example.ledgercore.account.adapter.outbound.user;

import com.example.ledgercore.account.command.port.outbound.AccountUserPort;
import com.example.ledgercore.user.query.port.inbound.CheckUserExistsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountUserAdapter implements AccountUserPort {

    private final CheckUserExistsUseCase checkUserExistsUseCase;

    @Override
    public boolean existsById(UUID userId) {
        return checkUserExistsUseCase.execute(userId);
    }
}