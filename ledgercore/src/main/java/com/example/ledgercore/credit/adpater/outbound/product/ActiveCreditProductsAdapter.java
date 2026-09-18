package com.example.ledgercore.credit.adpater.outbound.product;

import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.product.enums.ProductType;
import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsByTypeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ActiveCreditProductsAdapter
        implements ActiveCreditProductsPort {

    private final GetActiveProductsByTypeUseCase
            getActiveProductsByTypeUseCase;

    @Override
    public List<ActiveCreditProductInfo> getActiveCreditProducts() {

        GetActiveProductsByTypeResult result =
                getActiveProductsByTypeUseCase.execute(
                        new GetActiveProductsByTypeQuery(
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
