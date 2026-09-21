package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.AcceptCreditOfferCommand;
import com.example.ledgercore.credit.command.dto.AcceptCreditOfferResult;
import com.example.ledgercore.credit.command.port.inbound.AcceptCreditOfferUseCase;
import com.example.ledgercore.credit.command.repository.CreditOfferCommandRepository;
import com.example.ledgercore.credit.command.service.CreateCreditFacilityService;
import com.example.ledgercore.credit.command.service.UpdateCreditFacilityService;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityResult;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityResult;
import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AcceptCreditOfferHandler
        implements AcceptCreditOfferUseCase {

    private final CreditOfferCommandRepository creditOfferCommandRepository;
    private final CreateCreditFacilityService createCreditFacilityService;
    private final UpdateCreditFacilityService updateCreditFacilityService;

    @Override
    @Transactional
    public AcceptCreditOfferResult execute(
            AcceptCreditOfferCommand command
    ) {
        CreditOffer offer =
                creditOfferCommandRepository
                        .findByIdAndCustomerId(
                                command.offerId(),
                                command.customerId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_OFFER_NOT_FOUND
                                )
                        );

        validateOffer(offer);

        Instant acceptedAt = Instant.now();

        if (offer.getCreditFacilityId() == null) {
            return acceptAndCreateFacility(
                    offer,
                    acceptedAt
            );
        }

        return acceptAndUpdateFacility(
                offer,
                acceptedAt
        );
    }

    private AcceptCreditOfferResult acceptAndCreateFacility(
            CreditOffer offer,
            Instant acceptedAt
    ) {
        CreateCreditFacilityResult facility =
                createCreditFacilityService.create(
                        new CreateCreditFacilityCommand(
                                offer.getCustomerId(),
                                offer.getProductId(),
                                offer.getApprovedLimit(),
                                offer.getCurrency()
                        )
                );

        offer.accept(
                facility.facilityId(),
                acceptedAt
        );

        return new AcceptCreditOfferResult(
                offer.getId(),
                facility.facilityId(),
                offer.getCustomerId(),
                facility.productId(),
                facility.creditLimit().toPlainString(),
                facility.currency(),
                offer.getStatus(),
                acceptedAt
        );
    }

    private AcceptCreditOfferResult acceptAndUpdateFacility(
            CreditOffer offer,
            Instant acceptedAt
    ) {
        UpdateCreditFacilityResult facility =
                updateCreditFacilityService.update(
                        new UpdateCreditFacilityCommand(
                                offer.getCustomerId(),
                                offer.getCreditFacilityId(),
                                offer.getProductId(),
                                offer.getApprovedLimit(),
                                offer.getCurrency()
                        )
                );

        offer.accept(
                facility.facilityId(),
                acceptedAt
        );

        return new AcceptCreditOfferResult(
                offer.getId(),
                facility.facilityId(),
                offer.getCustomerId(),
                facility.productId(),
                facility.creditLimit().toPlainString(),
                facility.currency(),
                offer.getStatus(),
                acceptedAt
        );
    }

    private void validateOffer(CreditOffer offer) {
        if (offer.getStatus() != CreditOfferStatus.OFFERED) {
            throw new BusinessException(
                    ErrorCode.CREDIT_OFFER_NOT_AVAILABLE
            );
        }

        if (!offer.getExpiresAt().isAfter(Instant.now())) {
            throw new BusinessException(
                    ErrorCode.CREDIT_OFFER_EXPIRED
            );
        }
    }
}