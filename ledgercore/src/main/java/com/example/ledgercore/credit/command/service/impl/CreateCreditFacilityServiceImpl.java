package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.port.outbound.CreditLedgerAccountPort;
import com.example.ledgercore.credit.command.service.CreateCreditFacilityService;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityResult;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCreditFacilityServiceImpl
        implements CreateCreditFacilityService {

    private final ActiveCreditProductPort activeCreditProductPort;
    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    private final CreditLedgerAccountPort creditLedgerAccountPort;

    @Override
    @Transactional
    public CreateCreditFacilityResult create(
            CreateCreditFacilityCommand command
    ) {
        validateCommand(command);

        ActiveCreditProductInfo product =
                activeCreditProductPort.getActiveProduct(
                        command.productId()
                );

        validateCreditProduct(product);

        Instant now = Instant.now();
        UUID creditFacilityId = UUID.randomUUID();

        CreditFacility facility = CreditFacility.builder()
                .id(creditFacilityId)
                .customerId(command.customerId())
                .productId(product.id())
                .creditLimit(command.creditLimit())
                .outstandingBalance(BigDecimal.ZERO)
                .currency(command.currency())
                .status(CreditFacilityStatus.ACTIVE)
                .openedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UUID ledgerAccountId =
                creditLedgerAccountPort.createCreditLedgerAccount(
                        facility.getId(),
                        facility.getCurrency()
                );

        facility.setLedgerAccountId(ledgerAccountId);

        CreditFacility saved = creditFacilityCommandRepository.save(facility);

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
            ActiveCreditProductInfo product
    ) {
        if (product.type() != ProductType.CREDIT) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_PRODUCT_TYPE_INVALID
            );
        }
    }
}