package com.example.ledgercore.product.query.dto;

import java.util.List;

public record GetActiveProductsByTypeResult(
        List<ProductResponse> products
) {
}