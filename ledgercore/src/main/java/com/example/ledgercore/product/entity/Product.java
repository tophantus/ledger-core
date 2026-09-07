package com.example.ledgercore.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_products_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_products_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            length = 50
    )
    private String code;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private ProductStatus status;

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