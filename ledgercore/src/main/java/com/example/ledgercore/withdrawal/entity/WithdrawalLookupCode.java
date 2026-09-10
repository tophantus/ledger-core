package com.example.ledgercore.withdrawal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "withdrawal_lookup_codes"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalLookupCode {

    @Id
    @Column(name = "withdrawal_intent_id", nullable = false)
    private UUID withdrawalIntentId;

    @Column(
            name = "lookup_code",
            nullable = false,
            length = 8,
            unique = true
    )
    private String lookupCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}