package com.example.ledgercore.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "card_credentials")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCredential {

    @Id
    private UUID id;

    @Column(name = "card_id", nullable = false, unique = true)
    private UUID cardId;

    @Column(name = "pin_verifier")
    private String pinVerifier;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}