package com.example.ledgercore.card.adapter.outbound;

import com.example.ledgercore.card.command.port.outbound.ProductPort;
import com.example.ledgercore.card.command.port.outbound.dto.ProductInfo;
import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductPortAdapter
        implements ProductPort {

    private final GetActiveProductUseCase
            getActiveProductUseCase;

    @Override
    public ProductInfo getActiveProduct(
            UUID productId
    ) {
        GetActiveProductResult result =
                getActiveProductUseCase.execute(
                        new GetActiveProductQuery(productId)
                );

        return new ProductInfo(
                result.id(),
                result.code(),
                result.type()
        );
    }
}