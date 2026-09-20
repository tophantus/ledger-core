package com.example.ledgercore.card.command.repository;

import com.example.ledgercore.card.entity.CardCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardCredentialCommandRepository
        extends JpaRepository<CardCredential, UUID> {
}