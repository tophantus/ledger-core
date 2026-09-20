package com.example.ledgercore.product.query.port.inbound;

import com.example.ledgercore.product.query.dto.GetActiveProductsQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsResult;

public interface GetActiveProductsUseCase {

    GetActiveProductsResult execute(
            GetActiveProductsQuery query
    );
}