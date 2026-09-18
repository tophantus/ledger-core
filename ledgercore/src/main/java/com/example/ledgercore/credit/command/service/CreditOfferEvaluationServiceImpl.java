package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.credit.command.service.dto.CreditOfferEvaluationResult;
import com.example.ledgercore.credit.command.service.dto.EvaluateCreditOfferCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditOfferEvaluationServiceImpl
        implements CreditOfferEvaluationService {

    private static final String DEFAULT_PRODUCT_CODE =
            "CREDIT_STANDARD";

    private static final BigDecimal DEFAULT_APPROVED_LIMIT =
            new BigDecimal("10000000");

    private final ActiveCreditProductsPort activeCreditProductsPort;

    @Override
    public CreditOfferEvaluationResult evaluate(
            EvaluateCreditOfferCommand command
    ) {
        List<ActiveCreditProductInfo> products =
                activeCreditProductsPort.getActiveCreditProducts();

        ActiveCreditProductInfo product = products.stream()
                .filter(p -> DEFAULT_PRODUCT_CODE.equals(p.code()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Default credit product is not available"
                        )
                );

        return new CreditOfferEvaluationResult(
                command.customerId(),
                product.id(),
                DEFAULT_APPROVED_LIMIT,
                Currency.VND
        );
    }
}