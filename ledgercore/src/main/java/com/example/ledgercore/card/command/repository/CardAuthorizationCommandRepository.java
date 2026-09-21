package com.example.ledgercore.card.command.repository;

import com.example.ledgercore.card.entity.CardAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardAuthorizationCommandRepository
        extends JpaRepository<CardAuthorization, UUID> {

    boolean existsByReference(String reference);
}