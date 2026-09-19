package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.CreditOfferInfo;
import com.example.ledgercore.credit.query.dto.GetLatestCreditOfferQuery;

import java.util.Optional;

public interface GetLatestCreditOfferUseCase {

    Optional<CreditOfferInfo> execute(
            GetLatestCreditOfferQuery query
    );
}