package com.example.ledgercore.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "provider_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_accounts_account_id",
                        columnNames = "account_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_provider_accounts_provider_id",
                        columnList = "provider_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "provider_id",
            nullable = false
    )
    private UUID providerId;

    @Column(
            name = "account_id",
            nullable = false
    )
    private UUID accountId;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}