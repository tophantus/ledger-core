package com.example.ledgercore.cardtoken.command.repository;

import com.example.ledgercore.cardtoken.entity.CardToken;
import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardTokenCommandRepository
        extends JpaRepository<CardToken, UUID> {

    boolean existsByToken(String token);

    boolean existsByCardIdAndProviderIdAndProviderCustomerReferenceAndStatus(
            UUID cardId,
            UUID providerId,
            String providerCustomerReference,
            CardTokenStatus status
    );
}