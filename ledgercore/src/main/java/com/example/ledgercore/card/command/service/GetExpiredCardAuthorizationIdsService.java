package com.example.ledgercore.card.command.service;

import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.enums.CardAuthorizationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetExpiredCardAuthorizationIdsService {

    private final CardAuthorizationCommandRepository
            cardAuthorizationCommandRepository;

    public List<UUID> get(
            Instant now,
            int limit
    ) {
        return cardAuthorizationCommandRepository
                .findExpiredAuthorizationIds(
                        CardAuthorizationStatus.AUTHORIZED.name(),
                        now,
                        limit
                );
    }
}