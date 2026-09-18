package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.credit.command.repository.CreditOfferCommandRepository;
import com.example.ledgercore.credit.command.service.CreateCreditOfferService;
import com.example.ledgercore.credit.command.service.dto.CreateCreditOfferCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditOfferResult;
import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreateCreditOfferServiceImpl
        implements CreateCreditOfferService {

    private final CreditOfferCommandRepository creditOfferCommandRepository;

    @Override
    @Transactional
    public CreateCreditOfferResult create(
            CreateCreditOfferCommand command
    ) {
        Instant now = Instant.now();

        CreditOffer offer = CreditOffer.builder()
                .customerId(command.customerId())
                .creditFacilityId(command.creditFacilityId())
                .productId(command.productId())
                .approvedLimit(command.approvedLimit())
                .currency(command.currency())
                .status(CreditOfferStatus.OFFERED)
                .expiresAt(command.expiresAt())
                .createdAt(now)
                .updatedAt(now)
                .build();

        CreditOffer savedOffer =
                creditOfferCommandRepository.save(offer);

        return new CreateCreditOfferResult(
                savedOffer.getId(),
                savedOffer.getCustomerId(),
                savedOffer.getCreditFacilityId(),
                savedOffer.getProductId(),
                savedOffer.getApprovedLimit(),
                savedOffer.getCurrency(),
                savedOffer.getStatus(),
                savedOffer.getExpiresAt(),
                savedOffer.getCreatedAt()
        );
    }
}