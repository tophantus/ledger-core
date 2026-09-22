package com.example.ledgercore.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "card_vault_secrets")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardVaultSecret {

    @Id
    private UUID id;

    @Column(name = "card_id", nullable = false, unique = true)
    private UUID cardId;

    @Column(name = "encrypted_pan", nullable = false)
    private String encryptedPan;

    @Column(name = "encrypted_cvv", nullable = false)
    private String encryptedCvv;

    @Column(name = "encryption_version", nullable = false, length = 20)
    private String encryptionVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}