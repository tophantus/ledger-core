package com.example.ledgercore.card.query.repository;

import com.example.ledgercore.card.entity.CardCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardCredentialQueryRepository
        extends JpaRepository<CardCredential, UUID> {

    Optional<CardCredential> findByCardId(UUID cardId);
}