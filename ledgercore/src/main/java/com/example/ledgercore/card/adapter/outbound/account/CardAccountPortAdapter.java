package com.example.ledgercore.card.adapter.outbound.account;

import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountQuery;
import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountResult;
import com.example.ledgercore.account.query.port.inbound.GetActiveOwnedAccountUseCase;
import com.example.ledgercore.card.command.port.outbound.CardAccountPort;
import com.example.ledgercore.card.command.port.outbound.dto.CardAccountInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardAccountPortAdapter
        implements CardAccountPort {

    private final GetActiveOwnedAccountUseCase getActiveOwnedAccountUseCase;

    @Override
    public CardAccountInfo getOwnedAccount(
            UUID customerId,
            UUID accountId
    ) {
        GetActiveOwnedAccountResult result =
                getActiveOwnedAccountUseCase.execute(
                        new GetActiveOwnedAccountQuery(
                                customerId,
                                accountId
                        )
                );

        return new CardAccountInfo(
                result.id(),
                result.userId(),
                result.productId(),
                result.availableBalance(),
                result.currency()
        );
    }
}