package com.example.ledgercore.credit.adpater.outbound.product;

import com.example.ledgercore.credit.command.port.outbound.CreditProductPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditProductInfo;
import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditProductAdapter
        implements CreditProductPort {

    private final GetActiveProductUseCase
            getActiveProductUseCase;

    @Override
    public CreditProductInfo getActiveProduct(UUID productId) {
        GetActiveProductResult result =
                getActiveProductUseCase.execute(
                        new GetActiveProductQuery(productId)
                );

        return new CreditProductInfo(
                result.id(),
                result.code(),
                result.type()
        );
    }
}
