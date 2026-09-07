package com.example.ledgercore.ledger.entity;

import com.example.ledgercore.ledger.enums.JournalSourceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "journal_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_journal_entries_source",
                        columnNames = {
                                "source_type",
                                "source_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_journal_entries_business_date",
                        columnList = "business_date"
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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "source_type",
            nullable = false,
            length = 50
    )
    private JournalSourceType sourceType;

    @Column(
            name = "source_id",
            nullable = false
    )
    private UUID sourceId;

    @Column(
            name = "business_date",
            nullable = false
    )
    private LocalDate businessDate;

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