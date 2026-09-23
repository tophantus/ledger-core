package com.example.ledgercore.product.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.product.entity.Product;
import com.example.ledgercore.product.enums.ProductStatus;
import com.example.ledgercore.product.query.dto.GetActiveProductByCodeQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductByCodeUseCase;
import com.example.ledgercore.product.query.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetActiveProductByCodeHandler
        implements GetActiveProductByCodeUseCase {

    private final ProductQueryRepository productQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public GetActiveProductResult execute(
            GetActiveProductByCodeQuery query
    ) {
        if (query == null
                || query.code() == null
                || query.code().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String code = query.code().trim();

        Product product =
                productQueryRepository
                        .findByCodeAndStatus(
                                code,
                                ProductStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.PRODUCT_NOT_FOUND
                                )
                        );

        return new GetActiveProductResult(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getType()
        );
    }
}