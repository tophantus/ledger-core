package com.example.ledgercore.credit.adapter.outbound.product;

import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActiveCreditProductAdapter
        implements ActiveCreditProductPort {

    private final GetActiveProductUseCase
            getActiveProductUseCase;

    @Override
    public ActiveCreditProductInfo getActiveProduct(UUID productId) {
        GetActiveProductResult result =
                getActiveProductUseCase.execute(
                        new GetActiveProductQuery(productId)
                );

        return new ActiveCreditProductInfo(
                result.id(),
                result.code(),
                result.type()
        );
    }
}
