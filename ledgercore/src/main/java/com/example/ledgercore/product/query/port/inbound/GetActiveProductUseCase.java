package com.example.ledgercore.product.query.port.inbound;

import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;

public interface GetActiveProductUseCase {

    GetActiveProductResult execute(
            GetActiveProductQuery query
    );
}