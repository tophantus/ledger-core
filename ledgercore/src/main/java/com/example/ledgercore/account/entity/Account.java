package com.example.ledgercore.account.entity;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_accounts_account_no",
                        columnNames = "account_no"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_accounts_user")
    )
    private User user;

    @Column(name = "account_no", nullable = false, length = 30)
    private String accountNo;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ledger_account_id",
            foreignKey = @ForeignKey(
                    name = "fk_accounts_ledger_account"
            )
    )
    private LedgerAccount ledgerAccount;

    @PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}