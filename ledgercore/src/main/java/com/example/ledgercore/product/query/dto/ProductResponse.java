package com.example.ledgercore.product.query.dto;

import com.example.ledgercore.product.entity.Product;
import com.example.ledgercore.product.entity.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String code,
        String name,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductResponse from(
            Product product
    ) {
        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}