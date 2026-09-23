package com.example.ledgercore.provider.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.CreateProviderAccountCommand;
import com.example.ledgercore.account.command.port.inbound.CreateProviderAccountUseCase;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.provider.command.port.outbound.ProviderAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProviderAccountAdapter
        implements ProviderAccountPort {

    private final CreateProviderAccountUseCase
            createProviderAccountUseCase;

    @Override
    public void createProviderAccount(
            UUID providerId,
            Currency currency
    ) {
        createProviderAccountUseCase.execute(
                new CreateProviderAccountCommand(
                        providerId,
                        currency
                )
        );
    }
}