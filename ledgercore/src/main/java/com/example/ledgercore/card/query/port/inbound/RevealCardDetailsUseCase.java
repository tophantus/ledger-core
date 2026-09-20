package com.example.ledgercore.card.query.port.inbound;

import com.example.ledgercore.card.query.dto.RevealCardDetailsQuery;
import com.example.ledgercore.card.query.dto.RevealedCardDetails;

public interface RevealCardDetailsUseCase {

    RevealedCardDetails execute(
            RevealCardDetailsQuery query
    );
}