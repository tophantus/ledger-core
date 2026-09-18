package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.CreateCreditFacilityCommand;
import com.example.ledgercore.credit.command.dto.CreateCreditFacilityResult;
import com.example.ledgercore.credit.command.port.inbound.CreateCreditFacilityUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditProductPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditProductInfo;
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
public class CreateCreditFacilityHandler
        implements CreateCreditFacilityUseCase {

    private final CreditProductPort creditProductPort;
    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    @Override
    @Transactional
    public CreateCreditFacilityResult execute(
            CreateCreditFacilityCommand command
    ) {
        validateCommand(command);

        CreditProductInfo product =
                creditProductPort.getActiveProduct(
                        command.productId()
                );

        validateCreditProduct(product);

        Instant now = Instant.now();

        CreditFacility facility = CreditFacility.builder()
                .customerId(command.customerId())
                .productId(product.id())
                .creditLimit(command.creditLimit())
                .outstandingBalance(BigDecimal.ZERO)
                .currency(command.currency())
                .status(CreditFacilityStatus.ACTIVE)
                .openedAt(now)
                .closedAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        CreditFacility saved =
                creditFacilityCommandRepository.save(facility);

        return new CreateCreditFacilityResult(
                saved.getId(),
                saved.getCustomerId(),
                saved.getProductId(),
                saved.getCreditLimit(),
                saved.getOutstandingBalance(),
                saved.getCurrency(),
                saved.getStatus(),
                saved.getOpenedAt()
        );
    }

    private void validateCommand(
            CreateCreditFacilityCommand command
    ) {
        if (command == null
                || command.customerId() == null
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

    private void validateCreditProduct(
            CreditProductInfo product
    ) {
        if (product.type() != ProductType.CREDIT) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_PRODUCT_TYPE_INVALID
            );
        }
    }
}