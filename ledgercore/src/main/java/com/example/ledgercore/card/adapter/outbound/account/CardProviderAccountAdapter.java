package com.example.ledgercore.card.adapter.outbound.account;

import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.port.inbound.GetProviderAccountUseCase;
import com.example.ledgercore.card.command.port.outbound.CardProviderAccountPort;
import com.example.ledgercore.common.currency.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardProviderAccountAdapter
        implements CardProviderAccountPort {

    private final GetProviderAccountUseCase
            getProviderAccountUseCase;

    @Override
    public UUID getProviderAccountId(
            UUID providerId,
            Currency currency
    ) {
        AccountResponse response =
                getProviderAccountUseCase.execute(
                        providerId,
                        currency
                );

        return response.id();
    }
}
