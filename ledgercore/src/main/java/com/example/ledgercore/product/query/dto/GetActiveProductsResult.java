package com.example.ledgercore.product.query.dto;

import java.util.List;

public record GetActiveProductsResult(
        List<ProductResponse> products
) {
}