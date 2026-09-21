package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.AcceptCreditOfferCommand;
import com.example.ledgercore.credit.command.dto.AcceptCreditOfferResult;
import com.example.ledgercore.credit.command.repository.CreditOfferCommandRepository;
import com.example.ledgercore.credit.command.service.CreateCreditFacilityService;
import com.example.ledgercore.credit.command.service.UpdateCreditFacilityService;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityResult;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityResult;
import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptCreditOfferHandlerTest {

    @Mock
    private CreditOfferCommandRepository creditOfferCommandRepository;

    @Mock
    private CreateCreditFacilityService createCreditFacilityService;

    @Mock
    private UpdateCreditFacilityService updateCreditFacilityService;

    @Mock
    private CreditOffer offer;

    private AcceptCreditOfferHandler handler;

    private UUID customerId;
    private UUID offerId;
    private UUID productId;
    private UUID existingFacilityId;
    private UUID createdFacilityId;

    private BigDecimal approvedLimit;
    private BigDecimal facilityCreditLimit;
    private BigDecimal outstandingBalance;

    private Currency currency;

    @BeforeEach
    void setUp() {
        handler = new AcceptCreditOfferHandler(
                creditOfferCommandRepository,
                createCreditFacilityService,
                updateCreditFacilityService
        );

        customerId = UUID.randomUUID();
        offerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        existingFacilityId = UUID.randomUUID();
        createdFacilityId = UUID.randomUUID();

        approvedLimit = new BigDecimal("100000000");
        facilityCreditLimit = new BigDecimal("100000000");
        outstandingBalance = BigDecimal.ZERO;

        currency = Currency.VND;
    }

    @Test
    void shouldAcceptOfferAndCreateFacilitySuccessfully() {
        givenOffer(null);

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.ACCEPTED
                );

        CreateCreditFacilityResult facilityResult =
                new CreateCreditFacilityResult(
                        createdFacilityId,
                        customerId,
                        productId,
                        facilityCreditLimit,
                        outstandingBalance,
                        currency,
                        CreditFacilityStatus.ACTIVE,
                        Instant.parse("2026-09-21T10:00:00Z")
                );

        when(createCreditFacilityService.create(
                any(CreateCreditFacilityCommand.class)
        )).thenReturn(facilityResult);

        AcceptCreditOfferResult result =
                handler.execute(
                        new AcceptCreditOfferCommand(
                                customerId,
                                offerId
                        )
                );

        assertNotNull(result);
        assertEquals(offerId, result.offerId());
        assertEquals(createdFacilityId, result.creditFacilityId());
        assertEquals(customerId, result.customerId());
        assertEquals(productId, result.productId());
        assertEquals(facilityCreditLimit.toPlainString(), result.creditLimit());
        assertEquals(currency, result.currency());
        assertEquals(
                CreditOfferStatus.ACCEPTED,
                result.offerStatus()
        );
        assertNotNull(result.acceptedAt());

        ArgumentCaptor<CreateCreditFacilityCommand> captor =
                ArgumentCaptor.forClass(CreateCreditFacilityCommand.class);

        verify(createCreditFacilityService)
                .create(captor.capture());

        CreateCreditFacilityCommand command =
                captor.getValue();

        assertEquals(customerId, command.customerId());
        assertEquals(productId, command.productId());
        assertEquals(approvedLimit, command.creditLimit());
        assertEquals(currency, command.currency());

        verify(offer)
                .accept(
                        createdFacilityId,
                        result.acceptedAt()
                );

        verify(updateCreditFacilityService, never())
                .update(any(UpdateCreditFacilityCommand.class));
    }

    @Test
    void shouldAcceptOfferAndUpdateExistingFacilitySuccessfully() {
        givenOffer(existingFacilityId);

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.ACCEPTED
                );

        UpdateCreditFacilityResult facilityResult =
                new UpdateCreditFacilityResult(
                        existingFacilityId,
                        customerId,
                        productId,
                        facilityCreditLimit,
                        outstandingBalance,
                        currency,
                        CreditFacilityStatus.ACTIVE
                );

        when(updateCreditFacilityService.update(
                any(UpdateCreditFacilityCommand.class)
        )).thenReturn(facilityResult);

        AcceptCreditOfferResult result =
                handler.execute(
                        new AcceptCreditOfferCommand(
                                customerId,
                                offerId
                        )
                );

        assertNotNull(result);
        assertEquals(offerId, result.offerId());
        assertEquals(existingFacilityId, result.creditFacilityId());
        assertEquals(customerId, result.customerId());
        assertEquals(productId, result.productId());
        assertEquals(facilityCreditLimit.toPlainString(), result.creditLimit());
        assertEquals(currency, result.currency());
        assertEquals(
                CreditOfferStatus.ACCEPTED,
                result.offerStatus()
        );
        assertNotNull(result.acceptedAt());

        ArgumentCaptor<UpdateCreditFacilityCommand> captor =
                ArgumentCaptor.forClass(UpdateCreditFacilityCommand.class);

        verify(updateCreditFacilityService)
                .update(captor.capture());

        UpdateCreditFacilityCommand command =
                captor.getValue();

        assertEquals(customerId, command.customerId());
        assertEquals(
                existingFacilityId,
                command.facilityId()
        );
        assertEquals(productId, command.productId());
        assertEquals(approvedLimit, command.creditLimit());
        assertEquals(currency, command.currency());

        verify(offer)
                .accept(
                        existingFacilityId,
                        result.acceptedAt()
                );

        verify(createCreditFacilityService, never())
                .create(any(CreateCreditFacilityCommand.class));
    }

    @Test
    void shouldThrowWhenOfferDoesNotExist() {
        when(creditOfferCommandRepository.findByIdAndCustomerId(
                offerId,
                customerId
        )).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new AcceptCreditOfferCommand(
                                        customerId,
                                        offerId
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_OFFER_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(createCreditFacilityService, never())
                .create(any(CreateCreditFacilityCommand.class));

        verify(updateCreditFacilityService, never())
                .update(any(UpdateCreditFacilityCommand.class));
    }

    @Test
    void shouldThrowWhenOfferIsNotOffered() {
        when(creditOfferCommandRepository.findByIdAndCustomerId(
                offerId,
                customerId
        )).thenReturn(Optional.of(offer));

        when(offer.getStatus())
                .thenReturn(CreditOfferStatus.ACCEPTED);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new AcceptCreditOfferCommand(
                                        customerId,
                                        offerId
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_OFFER_NOT_AVAILABLE,
                exception.getErrorCode()
        );

        verify(createCreditFacilityService, never())
                .create(any(CreateCreditFacilityCommand.class));

        verify(updateCreditFacilityService, never())
                .update(any(UpdateCreditFacilityCommand.class));
    }

    @Test
    void shouldThrowWhenOfferIsExpired() {
        when(creditOfferCommandRepository.findByIdAndCustomerId(
                offerId,
                customerId
        )).thenReturn(Optional.of(offer));

        when(offer.getStatus())
                .thenReturn(CreditOfferStatus.OFFERED);

        when(offer.getExpiresAt())
                .thenReturn(Instant.now().minusSeconds(1));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new AcceptCreditOfferCommand(
                                        customerId,
                                        offerId
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_OFFER_EXPIRED,
                exception.getErrorCode()
        );

        verify(createCreditFacilityService, never())
                .create(any(CreateCreditFacilityCommand.class));

        verify(updateCreditFacilityService, never())
                .update(any(UpdateCreditFacilityCommand.class));
    }

    @Test
    void shouldCreateFacilityWhenOfferHasNoExistingFacility() {
        givenOffer(null);

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.ACCEPTED
                );

        CreateCreditFacilityResult facilityResult =
                new CreateCreditFacilityResult(
                        createdFacilityId,
                        customerId,
                        productId,
                        facilityCreditLimit,
                        outstandingBalance,
                        currency,
                        CreditFacilityStatus.ACTIVE,
                        Instant.parse("2026-09-21T10:00:00Z")
                );

        when(createCreditFacilityService.create(
                any(CreateCreditFacilityCommand.class)
        )).thenReturn(facilityResult);

        handler.execute(
                new AcceptCreditOfferCommand(
                        customerId,
                        offerId
                )
        );

        verify(createCreditFacilityService)
                .create(any(CreateCreditFacilityCommand.class));

        verify(updateCreditFacilityService, never())
                .update(any(UpdateCreditFacilityCommand.class));
    }

    @Test
    void shouldUpdateFacilityWhenOfferHasExistingFacility() {
        givenOffer(existingFacilityId);

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.ACCEPTED
                );

        UpdateCreditFacilityResult facilityResult =
                new UpdateCreditFacilityResult(
                        existingFacilityId,
                        customerId,
                        productId,
                        facilityCreditLimit,
                        outstandingBalance,
                        currency,
                        CreditFacilityStatus.ACTIVE
                );

        when(updateCreditFacilityService.update(
                any(UpdateCreditFacilityCommand.class)
        )).thenReturn(facilityResult);

        handler.execute(
                new AcceptCreditOfferCommand(
                        customerId,
                        offerId
                )
        );

        verify(updateCreditFacilityService)
                .update(any(UpdateCreditFacilityCommand.class));

        verify(createCreditFacilityService, never())
                .create(any(CreateCreditFacilityCommand.class));
    }

    @Test
    void shouldPassCreatedFacilityIdToOfferWhenCreatingFacility() {
        givenOffer(null);

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.ACCEPTED
                );

        CreateCreditFacilityResult facilityResult =
                new CreateCreditFacilityResult(
                        createdFacilityId,
                        customerId,
                        productId,
                        facilityCreditLimit,
                        outstandingBalance,
                        currency,
                        CreditFacilityStatus.ACTIVE,
                        Instant.parse("2026-09-21T10:00:00Z")
                );

        when(createCreditFacilityService.create(
                any(CreateCreditFacilityCommand.class)
        )).thenReturn(facilityResult);

        AcceptCreditOfferResult result =
                handler.execute(
                        new AcceptCreditOfferCommand(
                                customerId,
                                offerId
                        )
                );

        verify(offer)
                .accept(
                        createdFacilityId,
                        result.acceptedAt()
                );
    }

    @Test
    void shouldPassExistingFacilityIdToOfferWhenUpdatingFacility() {
        givenOffer(existingFacilityId);

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.ACCEPTED
                );

        UpdateCreditFacilityResult facilityResult =
                new UpdateCreditFacilityResult(
                        existingFacilityId,
                        customerId,
                        productId,
                        facilityCreditLimit,
                        outstandingBalance,
                        currency,
                        CreditFacilityStatus.ACTIVE
                );

        when(updateCreditFacilityService.update(
                any(UpdateCreditFacilityCommand.class)
        )).thenReturn(facilityResult);

        AcceptCreditOfferResult result =
                handler.execute(
                        new AcceptCreditOfferCommand(
                                customerId,
                                offerId
                        )
                );

        verify(offer)
                .accept(
                        existingFacilityId,
                        result.acceptedAt()
                );
    }

    @Test
    void shouldFindOfferByOfferIdAndCustomerId() {
        givenOffer(null);

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.ACCEPTED
                );

        CreateCreditFacilityResult facilityResult =
                new CreateCreditFacilityResult(
                        createdFacilityId,
                        customerId,
                        productId,
                        facilityCreditLimit,
                        outstandingBalance,
                        currency,
                        CreditFacilityStatus.ACTIVE,
                        Instant.parse("2026-09-21T10:00:00Z")
                );

        when(createCreditFacilityService.create(
                any(CreateCreditFacilityCommand.class)
        )).thenReturn(facilityResult);

        handler.execute(
                new AcceptCreditOfferCommand(
                        customerId,
                        offerId
                )
        );

        verify(creditOfferCommandRepository)
                .findByIdAndCustomerId(
                        offerId,
                        customerId
                );
    }

    private void givenOffer(UUID creditFacilityId) {
        when(creditOfferCommandRepository.findByIdAndCustomerId(
                offerId,
                customerId
        )).thenReturn(Optional.of(offer));

        when(offer.getExpiresAt())
                .thenReturn(Instant.now().plusSeconds(3600));

        when(offer.getCreditFacilityId())
                .thenReturn(creditFacilityId);

        when(offer.getId())
                .thenReturn(offerId);

        when(offer.getCustomerId())
                .thenReturn(customerId);

        when(offer.getProductId())
                .thenReturn(productId);

        when(offer.getApprovedLimit())
                .thenReturn(approvedLimit);

        when(offer.getCurrency())
                .thenReturn(currency);
    }
}