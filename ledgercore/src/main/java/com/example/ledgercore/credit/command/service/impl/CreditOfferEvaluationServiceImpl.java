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
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CreditOfferEvaluationServiceImpl
        implements CreditOfferEvaluationService {

    private static final BigDecimal STANDARD_MIN_LIMIT =
            new BigDecimal("10000000");

    private static final BigDecimal STANDARD_MAX_LIMIT =
            new BigDecimal("15000000");

    private static final BigDecimal GOLD_MIN_LIMIT =
            new BigDecimal("15000000");

    private static final BigDecimal GOLD_MAX_LIMIT =
            new BigDecimal("25000000");

    private static final BigDecimal PLATINUM_MIN_LIMIT =
            new BigDecimal("25000000");

    private static final BigDecimal PLATINUM_MAX_LIMIT =
            new BigDecimal("50000000");

    private static final BigDecimal MINIMUM_LIMIT_INCREASE_RATIO =
            new BigDecimal("0.15");

    private static final BigDecimal LIMIT_STEP =
            new BigDecimal("1000000");

    private final ActiveCreditProductsPort activeCreditProductsPort;

    private final CreditFacilityQueryRepository
            creditFacilityQueryRepository;

    @Override
    public Optional<CreditOfferEvaluationResult> evaluate(
            EvaluateCreditOfferCommand command
    ) {
        ActiveCreditProductInfo product =
                findRandomProduct();

        if (product == null) {
            return Optional.empty();
        }

        BigDecimal approvedLimit =
                generateApprovedLimit(product);

        Optional<CreditFacility> facility =
                creditFacilityQueryRepository
                        .findFirstByCustomerIdAndStatus(
                                command.customerId(),
                                CreditFacilityStatus.ACTIVE
                        );

        if (facility.isEmpty()) {
            return Optional.of(
                    createNewFacilityEvaluation(
                            command,
                            product,
                            approvedLimit
                    )
            );
        }

        return evaluateExistingFacility(
                command,
                product,
                facility.get(),
                approvedLimit
        );
    }

    private ActiveCreditProductInfo findRandomProduct() {
        List<ActiveCreditProductInfo> products =
                activeCreditProductsPort.getActiveCreditProducts();

        if (products.isEmpty()) {
            return null;
        }

        return products.get(
                ThreadLocalRandom.current().nextInt(
                        products.size()
                )
        );
    }

    private BigDecimal generateApprovedLimit(
            ActiveCreditProductInfo product
    ) {
        BigDecimal minimum;
        BigDecimal maximum;

        switch (product.code()) {
            case "CREDIT_STANDARD" -> {
                minimum = STANDARD_MIN_LIMIT;
                maximum = STANDARD_MAX_LIMIT;
            }
            case "CREDIT_GOLD" -> {
                minimum = GOLD_MIN_LIMIT;
                maximum = GOLD_MAX_LIMIT;
            }
            case "CREDIT_PLATINUM" -> {
                minimum = PLATINUM_MIN_LIMIT;
                maximum = PLATINUM_MAX_LIMIT;
            }
            default -> {
                return STANDARD_MIN_LIMIT;
            }
        }

        long minimumUnits =
                minimum.divide(
                        LIMIT_STEP,
                        0,
                        RoundingMode.UNNECESSARY
                ).longValue();

        long maximumUnits =
                maximum.divide(
                        LIMIT_STEP,
                        0,
                        RoundingMode.UNNECESSARY
                ).longValue();

        long approvedUnits =
                ThreadLocalRandom.current().nextLong(
                        minimumUnits,
                        maximumUnits + 1
                );

        return LIMIT_STEP.multiply(
                BigDecimal.valueOf(approvedUnits)
        );
    }

    private CreditOfferEvaluationResult createNewFacilityEvaluation(
            EvaluateCreditOfferCommand command,
            ActiveCreditProductInfo product,
            BigDecimal approvedLimit
    ) {
        return new CreditOfferEvaluationResult(
                command.customerId(),
                null,
                product.id(),
                approvedLimit,
                Currency.VND
        );
    }

    private Optional<CreditOfferEvaluationResult> evaluateExistingFacility(
            EvaluateCreditOfferCommand command,
            ActiveCreditProductInfo product,
            CreditFacility facility,
            BigDecimal approvedLimit
    ) {
        BigDecimal currentLimit = facility.getCreditLimit();

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