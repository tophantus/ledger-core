package com.example.ledgercore.card.query.repository;

import com.example.ledgercore.card.entity.CardVaultSecret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardVaultSecretQueryRepository
        extends JpaRepository<CardVaultSecret, UUID> {

    Optional<CardVaultSecret> findByCardId(UUID cardId);
}