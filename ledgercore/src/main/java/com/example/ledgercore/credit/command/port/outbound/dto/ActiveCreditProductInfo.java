package com.example.ledgercore.credit.command.port.outbound.dto;

import com.example.ledgercore.product.enums.ProductType;

import java.util.UUID;

public record ActiveCreditProductInfo(
        UUID id,
        String code,
        ProductType type
) {
}