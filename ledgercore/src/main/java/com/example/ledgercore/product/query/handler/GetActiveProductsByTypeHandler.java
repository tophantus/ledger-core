package com.example.ledgercore.product.query.handler;

import com.example.ledgercore.product.enums.ProductStatus;
import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeResult;
import com.example.ledgercore.product.query.dto.ProductResponse;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsByTypeUseCase;
import com.example.ledgercore.product.query.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetActiveProductsByTypeHandler
        implements GetActiveProductsByTypeUseCase {

    private final ProductQueryRepository productQueryRepository;

    @Override
    public GetActiveProductsByTypeResult execute(
            GetActiveProductsByTypeQuery query
    ) {
        return new GetActiveProductsByTypeResult(
                productQueryRepository
                        .findAllByTypeAndStatus(
                                query.type(),
                                ProductStatus.ACTIVE
                        )
                        .stream()
                        .map(ProductResponse::from)
                        .toList()
        );
    }
}