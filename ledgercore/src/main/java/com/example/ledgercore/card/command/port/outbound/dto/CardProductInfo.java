package com.example.ledgercore.card.command.port.outbound.dto;

import com.example.ledgercore.product.enums.ProductType;

import java.util.UUID;

public record CardProductInfo(
        UUID id,
        String code,
        ProductType type
) {
}