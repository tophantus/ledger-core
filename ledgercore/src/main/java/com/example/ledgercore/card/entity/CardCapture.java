package com.example.ledgercore.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "card_captures",
        indexes = {
                @Index(
                        name = "idx_card_captures_authorization_id",
                        columnList = "authorization_id"
                ),
                @Index(
                        name = "idx_card_captures_transaction_id",
                        columnList = "transaction_id"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCapture {

    @Id
    private UUID id;

    @Column(name = "authorization_id", nullable = false, unique = true)
    private UUID authorizationId;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}