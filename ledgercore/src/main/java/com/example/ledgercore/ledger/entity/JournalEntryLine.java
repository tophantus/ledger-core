package com.example.ledgercore.ledger.entity;

import com.example.ledgercore.ledger.enums.EntryType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "journal_entry_lines",
        indexes = {
                @Index(
                        name = "idx_journal_entry_lines_journal_entry_id",
                        columnList = "journal_entry_id"
                ),
                @Index(
                        name = "idx_journal_entry_lines_ledger_account_id",
                        columnList = "ledger_account_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "journal_entry_id",
            nullable = false
    )
    private UUID journalEntryId;

    @Column(
            name = "ledger_account_id",
            nullable = false
    )
    private UUID ledgerAccountId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "entry_type",
            nullable = false,
            length = 10
    )
    private EntryType entryType;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

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