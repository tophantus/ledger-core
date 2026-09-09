package com.example.ledgercore.atm.entity;

import com.example.ledgercore.atm.enums.AtmTerminalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "atm_terminals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_atm_terminals_terminal_code",
                        columnNames = "terminal_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_atm_terminals_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtmTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "terminal_code",
            nullable = false,
            length = 50
    )
    private String terminalCode;

    @Column(
            name = "credential_hash",
            nullable = false,
            length = 255
    )
    private String credentialHash;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private AtmTerminalStatus status =
            AtmTerminalStatus.ACTIVE;

    @Column(length = 255)
    private String location;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}