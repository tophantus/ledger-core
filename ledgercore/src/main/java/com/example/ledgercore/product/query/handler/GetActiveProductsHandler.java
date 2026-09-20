package com.example.ledgercore.product.query.handler;

import com.example.ledgercore.product.enums.ProductStatus;
import com.example.ledgercore.product.query.dto.GetActiveProductsQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsResult;
import com.example.ledgercore.product.query.dto.ProductResponse;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsUseCase;
import com.example.ledgercore.product.query.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetActiveProductsHandler
        implements GetActiveProductsUseCase {

    private final ProductQueryRepository productQueryRepository;

    @Override
    public GetActiveProductsResult execute(
            GetActiveProductsQuery query
    ) {
        var products = query.type() == null
                ? productQueryRepository.findAllByStatus(ProductStatus.ACTIVE)
                : productQueryRepository.findAllByTypeAndStatus(
                query.type(),
                ProductStatus.ACTIVE
        );

        return new GetActiveProductsResult(
                products.stream()
                        .map(ProductResponse::from)
                        .toList()
        );
    }
}