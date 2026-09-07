package com.example.ledgercore.product.query.port.inbound;

import com.example.ledgercore.product.query.dto.ProductResponse;

import java.util.List;

public interface GetActiveProductsUseCase {

    List<ProductResponse> execute();
}