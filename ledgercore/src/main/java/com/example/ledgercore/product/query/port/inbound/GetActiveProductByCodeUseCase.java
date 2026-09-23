package com.example.ledgercore.product.query.port.inbound;

import com.example.ledgercore.product.query.dto.GetActiveProductByCodeQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;

public interface GetActiveProductByCodeUseCase {

    GetActiveProductResult execute(
            GetActiveProductByCodeQuery query
    );
}