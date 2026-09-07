package com.example.ledgercore.product.query.handler;

import com.example.ledgercore.product.entity.ProductStatus;
import com.example.ledgercore.product.query.dto.ProductResponse;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsUseCase;
import com.example.ledgercore.product.query.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetActiveProductsHandler
        implements GetActiveProductsUseCase {

    private final ProductQueryRepository
            productQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> execute() {

        return productQueryRepository
                .findByStatusOrderByCodeAsc(
                        ProductStatus.ACTIVE
                )
                .stream()
                .map(ProductResponse::from)
                .toList();
    }
}