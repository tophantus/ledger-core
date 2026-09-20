package com.example.ledgercore.product.query.dto;

import com.example.ledgercore.product.enums.ProductType;

import java.util.List;
import java.util.UUID;

public record GetActiveProductsByTypeResult(
        List<ProductInfo> products
) {

    public record ProductInfo(
            UUID id,
            String code,
            ProductType type
    ) {
    }
}