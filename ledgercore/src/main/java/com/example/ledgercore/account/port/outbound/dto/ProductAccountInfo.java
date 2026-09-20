package com.example.ledgercore.account.port.outbound.dto;

import com.example.ledgercore.product.enums.ProductType;

import java.util.UUID;

public record ProductAccountInfo(
        UUID productId,
        String code,
        ProductType type
) {
}