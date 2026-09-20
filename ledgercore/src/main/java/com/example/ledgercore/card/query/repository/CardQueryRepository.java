package com.example.ledgercore.card.query.repository;

import com.example.ledgercore.card.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardQueryRepository
        extends JpaRepository<Card, UUID> {

    Page<Card> findByCustomerId(
            UUID customerId,
            Pageable pageable
    );
}