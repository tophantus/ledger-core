package com.example.ledgercore.card.entity;

import com.example.ledgercore.card.enums.CardForm;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.enums.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "cards",
        indexes = {
                @Index(
                        name = "idx_cards_customer_id",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_cards_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_cards_credit_facility_id",
                        columnList = "credit_facility_id"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "form", nullable = false, length = 20)
    private CardForm form;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CardStatus status;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "credit_facility_id")
    private UUID creditFacilityId;

    @Column(
            name = "pan_hash",
            nullable = false,
            length = 64
    )
    private String panHash;

    @Column(name = "pan_last4", nullable = false, length = 4)
    private String panLast4;

    @Column(name = "expiry_month", nullable = false)
    private Short expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Short expiryYear;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}