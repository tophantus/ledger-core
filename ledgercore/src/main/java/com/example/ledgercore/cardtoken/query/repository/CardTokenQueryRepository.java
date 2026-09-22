package com.example.ledgercore.cardtoken.query.repository;

import com.example.ledgercore.cardtoken.entity.CardToken;
import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardTokenQueryRepository
        extends JpaRepository<CardToken, UUID> {

    Optional<CardToken> findByTokenAndStatus(
            String token,
            CardTokenStatus status
    );
}