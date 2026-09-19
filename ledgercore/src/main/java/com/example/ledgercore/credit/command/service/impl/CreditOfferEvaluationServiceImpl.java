package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.credit.command.service.CreditOfferEvaluationService;
import com.example.ledgercore.credit.command.service.dto.CreditOfferEvaluationResult;
import com.example.ledgercore.credit.command.service.dto.EvaluateCreditOfferCommand;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreditOfferEvaluationServiceImpl
        implements CreditOfferEvaluationService {

    private static final String DEFAULT_PRODUCT_CODE =
            "CREDIT_STANDARD";

    private static final BigDecimal DEFAULT_APPROVED_LIMIT =
            new BigDecimal("10000000");

    private static final BigDecimal MINIMUM_LIMIT_INCREASE_RATIO =
            new BigDecimal("0.15");

    private final ActiveCreditProductsPort activeCreditProductsPort;

    private final CreditFacilityQueryRepository
            creditFacilityQueryRepository;

    @Override
    public Optional<CreditOfferEvaluationResult> evaluate(
            EvaluateCreditOfferCommand command
    ) {
        ActiveCreditProductInfo product =
                findDefaultProduct();

        if (product == null) {
            return Optional.empty();
        }

        Optional<CreditFacility> facility =
                creditFacilityQueryRepository
                        .findByCustomerIdAndStatus(
                                command.customerId(),
                                CreditFacilityStatus.ACTIVE
                        );

        if (facility.isEmpty()) {
            return Optional.of(
                    createNewFacilityEvaluation(
                            command,
                            product
                    )
            );
        }

        return evaluateExistingFacility(
                command,
                product,
                facility.get()
        );
    }

    private ActiveCreditProductInfo findDefaultProduct() {
        List<ActiveCreditProductInfo> products =
                activeCreditProductsPort.getActiveCreditProducts();

        return products.stream()
                .filter(product ->
                        DEFAULT_PRODUCT_CODE.equals(product.code())
                )
                .findFirst()
                .orElse(null);
    }

    private CreditOfferEvaluationResult createNewFacilityEvaluation(
            EvaluateCreditOfferCommand command,
            ActiveCreditProductInfo product
    ) {
        return new CreditOfferEvaluationResult(
                command.customerId(),
                null,
                product.id(),
                DEFAULT_APPROVED_LIMIT,
                Currency.VND
        );
    }

    private Optional<CreditOfferEvaluationResult> evaluateExistingFacility(
            EvaluateCreditOfferCommand command,
            ActiveCreditProductInfo product,
            CreditFacility facility
    ) {
        BigDecimal currentLimit =
                facility.getCreditLimit();

        BigDecimal approvedLimit =
                DEFAULT_APPROVED_LIMIT;

        BigDecimal limitIncrease =
                approvedLimit.subtract(currentLimit);

        if (limitIncrease.signum() <= 0) {
            return Optional.empty();
        }

        BigDecimal increaseRatio =
                limitIncrease.divide(
                        currentLimit,
                        10,
                        RoundingMode.HALF_UP
                );

        if (increaseRatio.compareTo(
                MINIMUM_LIMIT_INCREASE_RATIO
        ) < 0) {
            return Optional.empty();
        }

        return Optional.of(
                new CreditOfferEvaluationResult(
                        command.customerId(),
                        facility.getId(),
                        product.id(),
                        approvedLimit,
                        Currency.VND
                )
        );
    }
}