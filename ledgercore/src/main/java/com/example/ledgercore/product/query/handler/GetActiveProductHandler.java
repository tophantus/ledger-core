package com.example.ledgercore.product.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.product.entity.Product;
import com.example.ledgercore.product.enums.ProductStatus;
import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductUseCase;
import com.example.ledgercore.product.query.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetActiveProductHandler
        implements GetActiveProductUseCase {

    private final ProductQueryRepository productQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public GetActiveProductResult execute(
            GetActiveProductQuery query
    ) {
        if (query == null || query.productId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Product product =
                productQueryRepository
                        .findById(query.productId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.PRODUCT_NOT_FOUND
                                )
                        );

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_ACTIVE
            );
        }

        return new GetActiveProductResult(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getType()
        );
    }
}