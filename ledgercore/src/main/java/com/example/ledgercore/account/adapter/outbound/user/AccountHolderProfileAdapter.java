package com.example.ledgercore.account.adapter.outbound.user;

import com.example.ledgercore.account.query.port.outbound.AccountHolderProfilePort;
import com.example.ledgercore.user.query.port.inbound.GetUserFullNameUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountHolderProfileAdapter
        implements AccountHolderProfilePort {

    private final GetUserFullNameUseCase getUserFullNameUseCase;

    @Override
    public String getFullName(UUID userId) {
        return getUserFullNameUseCase.execute(userId);
    }
}