package com.example.ledgercore.withdrawal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "withdrawal_executions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_withdrawal_executions_intent_id",
                        columnNames = "withdrawal_intent_id"
                ),
                @UniqueConstraint(
                        name = "uk_withdrawal_executions_transaction_id",
                        columnNames = "transaction_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_withdrawal_executions_atm_terminal_id",
                        columnList = "atm_terminal_id"
                ),
                @Index(
                        name = "idx_withdrawal_executions_executed_at",
                        columnList = "executed_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "withdrawal_intent_id",
            nullable = false
    )
    private UUID withdrawalIntentId;

    @Column(
            name = "atm_terminal_id",
            nullable = false
    )
    private UUID atmTerminalId;

    @Column(
            name = "transaction_id",
            nullable = false
    )
    private UUID transactionId;

    @Column(
            name = "executed_at",
            nullable = false
    )
    private Instant executedAt;

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