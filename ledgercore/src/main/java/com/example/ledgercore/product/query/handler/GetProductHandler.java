package com.example.ledgercore.product.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.product.entity.Product;
import com.example.ledgercore.product.query.dto.ProductResponse;
import com.example.ledgercore.product.query.port.inbound.GetProductUseCase;
import com.example.ledgercore.product.query.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProductHandler
        implements GetProductUseCase {

    private final ProductQueryRepository
            productQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse execute(
            UUID productId
    ) {
        if (productId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Product product =
                productQueryRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.PRODUCT_NOT_FOUND
                                )
                        );

        return ProductResponse.from(product);
    }
}