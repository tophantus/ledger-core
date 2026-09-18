package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.credit.command.port.outbound.dto.ProductInfo;

import java.util.UUID;

public interface ProductPort {

    ProductInfo getActiveProduct(UUID productId);
}