package com.example.ledgercore.account.adapter.outbound.product;

import com.example.ledgercore.account.port.outbound.ProductAccountInfo;
import com.example.ledgercore.account.port.outbound.ProductAccountPort;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.product.entity.ProductStatus;
import com.example.ledgercore.product.query.dto.ProductResponse;
import com.example.ledgercore.product.query.port.inbound.GetProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductAccountAdapter
        implements ProductAccountPort {

    private final GetProductUseCase getProductUseCase;

    @Override
    public ProductAccountInfo getActiveProduct(
            UUID productId
    ) {
        ProductResponse product =
                getProductUseCase.execute(productId);

        if (product.status() != ProductStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_ACTIVE
            );
        }

        return new ProductAccountInfo(
                product.id(),
                product.code()
        );
    }
}