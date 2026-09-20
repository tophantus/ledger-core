package com.example.ledgercore.account.adapter.outbound.product;

import com.example.ledgercore.account.port.outbound.dto.ProductAccountInfo;
import com.example.ledgercore.account.port.outbound.ProductAccountPort;
import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductAccountAdapter
        implements ProductAccountPort {

    private final GetActiveProductUseCase
            getActiveProductUseCase;

    @Override
    public ProductAccountInfo getActiveProduct(
            UUID productId
    ) {
        GetActiveProductResult result =
                getActiveProductUseCase.execute(
                        new GetActiveProductQuery(productId)
                );

        return new ProductAccountInfo(
                result.id(),
                result.code(),
                result.type()

        );
    }
}