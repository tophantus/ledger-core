package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.repository.CreditOfferCommandRepository;
import com.example.ledgercore.credit.command.service.dto.CreateCreditOfferCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditOfferResult;
import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCreditOfferServiceImplTest {

    @Mock
    private CreditOfferCommandRepository creditOfferCommandRepository;

    private CreateCreditOfferServiceImpl service;

    private UUID customerId;
    private UUID runId;
    private UUID creditFacilityId;
    private UUID productId;

    private BigDecimal approvedLimit;
    private Currency currency;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        service = new CreateCreditOfferServiceImpl(
                creditOfferCommandRepository
        );

        customerId = UUID.randomUUID();
        runId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        productId = UUID.randomUUID();

        approvedLimit = new BigDecimal("100000000");
        currency = Currency.VND;
        expiresAt = Instant.parse("2026-09-28T10:00:00Z");
    }

    @Test
    void shouldCreateCreditOfferSuccessfully() {
        CreateCreditOfferCommand command =
                new CreateCreditOfferCommand(
                        customerId,
                        runId,
                        creditFacilityId,
                        productId,
                        approvedLimit,
                        currency,
                        expiresAt
                );

        UUID offerId = UUID.randomUUID();
        Instant createdAt =
                Instant.parse("2026-09-21T10:00:00Z");

        CreditOffer savedOffer =
                CreditOffer.builder()
                        .id(offerId)
                        .customerId(customerId)
                        .runId(runId)
                        .creditFacilityId(creditFacilityId)
                        .productId(productId)
                        .approvedLimit(approvedLimit)
                        .currency(currency)
                        .status(CreditOfferStatus.OFFERED)
                        .expiresAt(expiresAt)
                        .createdAt(createdAt)
                        .updatedAt(createdAt)
                        .build();

        when(
                creditOfferCommandRepository.save(
                        any(CreditOffer.class)
                )
        ).thenReturn(savedOffer);

        CreateCreditOfferResult result =
                service.create(command);

        assertNotNull(result);
        assertEquals(offerId, result.offerId());
        assertEquals(customerId, result.customerId());
        assertEquals(runId, result.runId());
        assertEquals(
                creditFacilityId,
                result.creditFacilityId()
        );
        assertEquals(productId, result.productId());
        assertEquals(approvedLimit, result.approvedLimit());
        assertEquals(currency, result.currency());
        assertEquals(
                CreditOfferStatus.OFFERED,
                result.status()
        );
        assertEquals(expiresAt, result.expiresAt());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void shouldSaveCreditOfferWithCorrectValues() {
        CreateCreditOfferCommand command =
                new CreateCreditOfferCommand(
                        customerId,
                        runId,
                        creditFacilityId,
                        productId,
                        approvedLimit,
                        currency,
                        expiresAt
                );

        CreditOffer savedOffer =
                CreditOffer.builder()
                        .id(UUID.randomUUID())
                        .customerId(customerId)
                        .runId(runId)
                        .creditFacilityId(creditFacilityId)
                        .productId(productId)
                        .approvedLimit(approvedLimit)
                        .currency(currency)
                        .status(CreditOfferStatus.OFFERED)
                        .expiresAt(expiresAt)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        when(
                creditOfferCommandRepository.save(
                        any(CreditOffer.class)
                )
        ).thenReturn(savedOffer);

        service.create(command);

        ArgumentCaptor<CreditOffer> captor =
                ArgumentCaptor.forClass(CreditOffer.class);

        verify(creditOfferCommandRepository)
                .save(captor.capture());

        CreditOffer offer = captor.getValue();

        assertEquals(customerId, offer.getCustomerId());
        assertEquals(runId, offer.getRunId());
        assertEquals(
                creditFacilityId,
                offer.getCreditFacilityId()
        );
        assertEquals(productId, offer.getProductId());
        assertEquals(
                approvedLimit,
                offer.getApprovedLimit()
        );
        assertEquals(currency, offer.getCurrency());
        assertEquals(
                CreditOfferStatus.OFFERED,
                offer.getStatus()
        );
        assertEquals(expiresAt, offer.getExpiresAt());
        assertNotNull(offer.getCreatedAt());
        assertNotNull(offer.getUpdatedAt());
    }

    @Test
    void shouldCreateOfferWithoutExistingCreditFacility() {
        CreateCreditOfferCommand command =
                new CreateCreditOfferCommand(
                        customerId,
                        runId,
                        null,
                        productId,
                        approvedLimit,
                        currency,
                        expiresAt
                );

        CreditOffer savedOffer =
                CreditOffer.builder()
                        .id(UUID.randomUUID())
                        .customerId(customerId)
                        .runId(runId)
                        .creditFacilityId(null)
                        .productId(productId)
                        .approvedLimit(approvedLimit)
                        .currency(currency)
                        .status(CreditOfferStatus.OFFERED)
                        .expiresAt(expiresAt)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        when(
                creditOfferCommandRepository.save(
                        any(CreditOffer.class)
                )
        ).thenReturn(savedOffer);

        CreateCreditOfferResult result =
                service.create(command);

        assertEquals(
                null,
                result.creditFacilityId()
        );
    }

    @Test
    void shouldReturnSavedOfferValues() {
        UUID offerId = UUID.randomUUID();
        Instant createdAt =
                Instant.parse("2026-09-21T12:00:00Z");

        CreateCreditOfferCommand command =
                new CreateCreditOfferCommand(
                        customerId,
                        runId,
                        creditFacilityId,
                        productId,
                        approvedLimit,
                        currency,
                        expiresAt
                );

        CreditOffer savedOffer =
                CreditOffer.builder()
                        .id(offerId)
                        .customerId(customerId)
                        .runId(runId)
                        .creditFacilityId(creditFacilityId)
                        .productId(productId)
                        .approvedLimit(approvedLimit)
                        .currency(currency)
                        .status(CreditOfferStatus.OFFERED)
                        .expiresAt(expiresAt)
                        .createdAt(createdAt)
                        .updatedAt(createdAt)
                        .build();

        when(
                creditOfferCommandRepository.save(
                        any(CreditOffer.class)
                )
        ).thenReturn(savedOffer);

        CreateCreditOfferResult result =
                service.create(command);

        assertEquals(offerId, result.offerId());
        assertEquals(customerId, result.customerId());
        assertEquals(runId, result.runId());
        assertEquals(
                creditFacilityId,
                result.creditFacilityId()
        );
        assertEquals(productId, result.productId());
        assertEquals(approvedLimit, result.approvedLimit());
        assertEquals(currency, result.currency());
        assertEquals(
                CreditOfferStatus.OFFERED,
                result.status()
        );
        assertEquals(expiresAt, result.expiresAt());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void shouldSetOfferStatusToOffered() {
        CreateCreditOfferCommand command =
                new CreateCreditOfferCommand(
                        customerId,
                        runId,
                        creditFacilityId,
                        productId,
                        approvedLimit,
                        currency,
                        expiresAt
                );

        CreditOffer savedOffer =
                CreditOffer.builder()
                        .id(UUID.randomUUID())
                        .customerId(customerId)
                        .runId(runId)
                        .creditFacilityId(creditFacilityId)
                        .productId(productId)
                        .approvedLimit(approvedLimit)
                        .currency(currency)
                        .status(CreditOfferStatus.OFFERED)
                        .expiresAt(expiresAt)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        when(
                creditOfferCommandRepository.save(
                        any(CreditOffer.class)
                )
        ).thenReturn(savedOffer);

        service.create(command);

        ArgumentCaptor<CreditOffer> captor =
                ArgumentCaptor.forClass(CreditOffer.class);

        verify(creditOfferCommandRepository)
                .save(captor.capture());

        assertEquals(
                CreditOfferStatus.OFFERED,
                captor.getValue().getStatus()
        );
    }
}