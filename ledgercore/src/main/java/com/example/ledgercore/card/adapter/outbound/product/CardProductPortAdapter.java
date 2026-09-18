package com.example.ledgercore.card.adapter.outbound.product;

import com.example.ledgercore.card.command.port.outbound.CardProductPort;
import com.example.ledgercore.card.command.port.outbound.dto.CardProductInfo;
import com.example.ledgercore.product.query.dto.GetActiveProductQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardProductPortAdapter
        implements CardProductPort {

    private final GetActiveProductUseCase
            getActiveProductUseCase;

    @Override
    public CardProductInfo getActiveProduct(
            UUID productId
    ) {
        GetActiveProductResult result =
                getActiveProductUseCase.execute(
                        new GetActiveProductQuery(productId)
                );

        return new CardProductInfo(
                result.id(),
                result.code(),
                result.type()
        );
    }
}