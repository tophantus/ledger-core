package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.RejectCreditOfferCommand;
import com.example.ledgercore.credit.command.dto.RejectCreditOfferResult;
import com.example.ledgercore.credit.command.repository.CreditOfferCommandRepository;
import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class RejectCreditOfferHandlerTest {

    @Mock
    private CreditOfferCommandRepository creditOfferCommandRepository;

    @Mock
    private CreditOffer offer;

    private RejectCreditOfferHandler handler;

    private UUID customerId;
    private UUID offerId;

    @BeforeEach
    void setUp() {
        handler = new RejectCreditOfferHandler(
                creditOfferCommandRepository
        );

        customerId = UUID.randomUUID();
        offerId = UUID.randomUUID();
    }

    @Test
    void shouldRejectOfferSuccessfully() {
        givenOfferedOffer();

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.REJECTED
                );

        RejectCreditOfferResult result =
                handler.execute(
                        new RejectCreditOfferCommand(
                                offerId,
                                customerId
                        )
                );

        assertNotNull(result);
        assertEquals(offerId, result.offerId());
        assertEquals(customerId, result.customerId());
        assertEquals(
                CreditOfferStatus.REJECTED,
                result.status()
        );
        assertNotNull(result.rejectedAt());

        verify(offer).reject(result.rejectedAt());
        verify(creditOfferCommandRepository).save(offer);
    }

    @Test
    void shouldFindOfferByOfferIdAndCustomerId() {
        givenOfferedOffer();

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.REJECTED
                );

        handler.execute(
                new RejectCreditOfferCommand(
                        offerId,
                        customerId
                )
        );

        verify(creditOfferCommandRepository)
                .findByIdAndCustomerId(
                        offerId,
                        customerId
                );
    }

    @Test
    void shouldSaveRejectedOffer() {
        givenOfferedOffer();

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.REJECTED
                );

        handler.execute(
                new RejectCreditOfferCommand(
                        offerId,
                        customerId
                )
        );

        verify(creditOfferCommandRepository)
                .save(offer);
    }

    @Test
    void shouldPassRejectedAtToOffer() {
        givenOfferedOffer();

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.REJECTED
                );

        RejectCreditOfferResult result =
                handler.execute(
                        new RejectCreditOfferCommand(
                                offerId,
                                customerId
                        )
                );

        verify(offer)
                .reject(result.rejectedAt());
    }

    @Test
    void shouldThrowWhenOfferDoesNotExist() {
        when(
                creditOfferCommandRepository
                        .findByIdAndCustomerId(
                                offerId,
                                customerId
                        )
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RejectCreditOfferCommand(
                                        offerId,
                                        customerId
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_OFFER_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(offer, never()).reject(any(Instant.class));
        verify(creditOfferCommandRepository, never())
                .save(any(CreditOffer.class));
    }

    @Test
    void shouldThrowWhenOfferIsNotOffered() {
        when(
                creditOfferCommandRepository
                        .findByIdAndCustomerId(
                                offerId,
                                customerId
                        )
        ).thenReturn(Optional.of(offer));

        when(offer.getStatus())
                .thenReturn(CreditOfferStatus.ACCEPTED);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RejectCreditOfferCommand(
                                        offerId,
                                        customerId
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_OFFER_NOT_AVAILABLE,
                exception.getErrorCode()
        );

        verify(offer, never()).reject(any(Instant.class));
        verify(creditOfferCommandRepository, never())
                .save(any(CreditOffer.class));
    }

    @Test
    void shouldThrowWhenOfferIsExpired() {
        when(
                creditOfferCommandRepository
                        .findByIdAndCustomerId(
                                offerId,
                                customerId
                        )
        ).thenReturn(Optional.of(offer));

        when(offer.getStatus())
                .thenReturn(CreditOfferStatus.OFFERED);

        when(offer.getExpiresAt())
                .thenReturn(
                        Instant.now().minusSeconds(1)
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RejectCreditOfferCommand(
                                        offerId,
                                        customerId
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_OFFER_EXPIRED,
                exception.getErrorCode()
        );

        verify(offer, never()).reject(any(Instant.class));
        verify(creditOfferCommandRepository, never())
                .save(any(CreditOffer.class));
    }

    @Test
    void shouldReturnOfferIdAndCustomerIdFromOffer() {
        givenOfferedOffer();

        when(offer.getStatus())
                .thenReturn(
                        CreditOfferStatus.OFFERED,
                        CreditOfferStatus.REJECTED
                );

        RejectCreditOfferResult result =
                handler.execute(
                        new RejectCreditOfferCommand(
                                offerId,
                                customerId
                        )
                );

        assertEquals(offerId, result.offerId());
        assertEquals(customerId, result.customerId());
    }

    private void givenOfferedOffer() {
        when(
                creditOfferCommandRepository
                        .findByIdAndCustomerId(
                                offerId,
                                customerId
                        )
        ).thenReturn(Optional.of(offer));

        when(offer.getExpiresAt())
                .thenReturn(
                        Instant.now().plusSeconds(3600)
                );

        when(offer.getId())
                .thenReturn(offerId);

        when(offer.getCustomerId())
                .thenReturn(customerId);
    }
}