package com.example.ledgercore.account.adapter.outbound.product;

import com.example.ledgercore.account.port.outbound.AccountProductPort;
import com.example.ledgercore.account.port.outbound.dto.ProductAccountInfo;
import com.example.ledgercore.product.query.dto.GetActiveProductByCodeQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductByCodeUseCase;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountProductAdapter
        implements AccountProductPort {

    private static final String PROVIDER_PRODUCT_CODE =
            "PROVIDER_SETTLEMENT";

    private final GetActiveProductUseCase getActiveProductUseCase;

    private final GetActiveProductByCodeUseCase
            getActiveProductByCodeUseCase;

    @Override
    public ProductAccountInfo getActiveProduct(
            UUID productId
    ) {
        GetActiveProductResult result =
                getActiveProductUseCase.execute(
                        new GetActiveProductQuery(productId)
                );

        return toProductAccountInfo(result);
    }

    @Override
    public ProductAccountInfo getActiveProviderProduct() {
        GetActiveProductResult result =
                getActiveProductByCodeUseCase.execute(
                        new GetActiveProductByCodeQuery(
                                PROVIDER_PRODUCT_CODE
                        )
                );

        return toProductAccountInfo(result);
    }

    private ProductAccountInfo toProductAccountInfo(
            GetActiveProductResult result
    ) {
        return new ProductAccountInfo(
                result.id(),
                result.code(),
                result.type()
        );
    }
}