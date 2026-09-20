package com.example.ledgercore.credit.adapter.outbound.product;

import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.product.enums.ProductType;
import com.example.ledgercore.product.query.dto.GetActiveProductsQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ActiveCreditProductsAdapter
        implements ActiveCreditProductsPort {

    private final GetActiveProductsUseCase
            getActiveProductsUseCase;

    @Override
    public List<ActiveCreditProductInfo> getActiveCreditProducts() {

        GetActiveProductsResult result =
                getActiveProductsUseCase.execute(
                        new GetActiveProductsQuery(
                                ProductType.CREDIT
                        )
                );

        return result.products()
                .stream()
                .map(product ->
                        new ActiveCreditProductInfo(
                                product.id(),
                                product.code(),
                                product.type()
                        )
                )
                .toList();
    }
}
