package com.example.ledgercore.card.command.repository;

import com.example.ledgercore.card.entity.CardVaultSecret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardVaultSecretCommandRepository
        extends JpaRepository<CardVaultSecret, UUID> {
}