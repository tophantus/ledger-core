package com.example.ledgercore.card.command.service;

import com.example.ledgercore.card.command.port.outbound.CardAuthorizationHoldPort;
import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.entity.CardAuthorization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpireCardAuthorizationService {

    private final CardAuthorizationCommandRepository
            cardAuthorizationCommandRepository;

    private final CardAuthorizationHoldPort
            cardAuthorizationHoldPort;

    private final Clock clock;

    @Transactional
    public void expire(UUID authorizationId) {

        Instant now = Instant.now(clock);

        CardAuthorization authorization =
                cardAuthorizationCommandRepository
                        .findByIdForUpdate(authorizationId)
                        .orElse(null);

        if (authorization == null) {
            return;
        }

        if (!authorization.isAuthorized()) {
            return;
        }

        if (!authorization.isExpired(now)) {
            return;
        }

        cardAuthorizationHoldPort.releaseHold(
                authorization.getHoldType(),
                authorization.getHoldId()
        );

        authorization.expire(now);
    }
}