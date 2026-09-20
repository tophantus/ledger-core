package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import com.example.ledgercore.credit.query.dto.CreditOfferInfo;
import com.example.ledgercore.credit.query.dto.GetLatestCreditOfferQuery;
import com.example.ledgercore.credit.query.port.inbound.GetLatestCreditOfferUseCase;
import com.example.ledgercore.credit.query.repository.CreditOfferQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetLatestCreditOfferHandler
        implements GetLatestCreditOfferUseCase {

    private final CreditOfferQueryRepository
            creditOfferQueryRepository;

    @Override
    public Optional<CreditOfferInfo> execute(
            GetLatestCreditOfferQuery query
    ) {
        return creditOfferQueryRepository
                .findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                        query.customerId(),
                        CreditOfferStatus.OFFERED,
                        Instant.now()
                )
                .map(this::toInfo);
    }

    private CreditOfferInfo toInfo(CreditOffer offer) {
        return new CreditOfferInfo(
                offer.getId(),
                offer.getCustomerId(),
                offer.getCreditFacilityId(),
                offer.getProductId(),
                offer.getApprovedLimit().toPlainString(),
                offer.getCurrency(),
                offer.getStatus(),
                offer.getExpiresAt(),
                offer.getCreatedAt()
        );
    }
}