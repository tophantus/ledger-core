package com.example.ledgercore.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "journal_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_journal_entries_transaction_id",
                        columnNames = "transaction_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "transaction_id",
            nullable = false
    )
    private UUID transactionId;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void prePersist() {
        createdAt = Instant.now();
    }
}