package com.example.ledgercore.product.query.dto;

import com.example.ledgercore.product.enums.ProductType;

public record GetActiveProductsByTypeQuery(
        ProductType type
) {
}