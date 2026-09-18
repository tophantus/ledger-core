package com.example.ledgercore.product.query.dto;

import com.example.ledgercore.product.enums.ProductType;

import java.util.UUID;

public record GetActiveProductQuery(
        UUID productId
) {
}