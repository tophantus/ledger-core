package com.example.ledgercore.product.query.port.inbound;

import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeResult;

public interface GetActiveProductsByTypeUseCase {

    GetActiveProductsByTypeResult execute(
            GetActiveProductsByTypeQuery query
    );
}