package com.example.ledgercore.card.command.repository;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CardCommandRepository
        extends JpaRepository<Card, UUID> {

    Optional<Card> findByPanHash(String panHash);

    boolean existsByAccountIdAndStatusIn(
            UUID accountId,
            Collection<CardStatus> statuses
    );

    boolean existsByCreditFacilityIdAndStatusIn(
            UUID creditFacilityId,
            Set<CardStatus> statuses
    );
}