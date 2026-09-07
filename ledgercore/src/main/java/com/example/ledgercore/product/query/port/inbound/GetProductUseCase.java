package com.example.ledgercore.product.query.port.inbound;

import com.example.ledgercore.product.query.dto.ProductResponse;

import java.util.UUID;

public interface GetProductUseCase {

    ProductResponse execute(UUID productId);
}