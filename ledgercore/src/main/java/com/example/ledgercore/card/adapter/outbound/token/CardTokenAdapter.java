package com.example.ledgercore.card.adapter.outbound.token;

import com.example.ledgercore.card.command.port.outbound.CardTokenPort;
import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenQuery;
import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenResult;
import com.example.ledgercore.cardtoken.query.port.inbound.ResolveCardTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardTokenAdapter
        implements CardTokenPort {

    private final ResolveCardTokenUseCase resolveCardTokenUseCase;

    @Override
    public CardTokenInfo resolve(
            UUID providerId,
            String token
    ) {
        ResolveCardTokenResult result =
                resolveCardTokenUseCase.execute(
                        new ResolveCardTokenQuery(
                                providerId,
                                token
                        )
                );

        return new CardTokenInfo(
                result.cardId()
        );
    }
}
