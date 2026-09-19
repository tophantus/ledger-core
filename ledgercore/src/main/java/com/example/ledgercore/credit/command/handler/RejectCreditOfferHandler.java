package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.RejectCreditOfferCommand;
import com.example.ledgercore.credit.command.dto.RejectCreditOfferResult;
import com.example.ledgercore.credit.command.port.inbound.RejectCreditOfferUseCase;
import com.example.ledgercore.credit.command.repository.CreditOfferCommandRepository;
import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RejectCreditOfferHandler
        implements RejectCreditOfferUseCase {

    private final CreditOfferCommandRepository
            creditOfferCommandRepository;

    @Override
    @Transactional
    public RejectCreditOfferResult execute(
            RejectCreditOfferCommand command
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

        Instant rejectedAt = Instant.now();

        offer.reject(rejectedAt);

        creditOfferCommandRepository.save(offer);

        return new RejectCreditOfferResult(
                offer.getId(),
                offer.getCustomerId(),
                offer.getStatus(),
                rejectedAt
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