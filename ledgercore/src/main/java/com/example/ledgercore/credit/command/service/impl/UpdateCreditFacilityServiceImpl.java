package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.service.UpdateCreditFacilityService;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityResult;
import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.product.enums.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UpdateCreditFacilityServiceImpl
        implements UpdateCreditFacilityService {

    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    private final ActiveCreditProductPort activeCreditProductPort;

    @Override
    @Transactional
    public UpdateCreditFacilityResult update(
            UpdateCreditFacilityCommand command
    ) {
        validateCommand(command);

        CreditFacility facility =
                creditFacilityCommandRepository
                        .findByIdAndCustomerId(
                                command.facilityId(),
                                command.customerId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                                )
                        );

        validateFacility(facility);

        ActiveCreditProductInfo product =
                activeCreditProductPort.getActiveProduct(
                        command.productId()
                );

        validateCreditProduct(product);

        Instant now = Instant.now();

        facility.updateCreditTerms(
                product.id(),
                command.creditLimit(),
                command.currency(),
                now
        );

        CreditFacility saved =
                creditFacilityCommandRepository.save(facility);

        return new UpdateCreditFacilityResult(
                saved.getId(),
                saved.getCustomerId(),
                saved.getProductId(),
                saved.getCreditLimit(),
                saved.getOutstandingBalance(),
                saved.getCurrency(),
                saved.getStatus()
        );
    }

    private void validateCommand(
            UpdateCreditFacilityCommand command
    ) {
        if (command == null
                || command.customerId() == null
                || command.facilityId() == null
                || command.productId() == null
                || command.creditLimit() == null
                || command.currency() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.creditLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_LIMIT_INVALID
            );
        }
    }

    private void validateFacility(
            CreditFacility facility
    ) {
        if (facility.getStatus() != CreditFacilityStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_ACTIVE
            );
        }
    }

    private void validateCreditProduct(
            ActiveCreditProductInfo product
    ) {
        if (product.type() != ProductType.CREDIT) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_PRODUCT_TYPE_INVALID
            );
        }
    }
}