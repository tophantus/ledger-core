package com.example.ledgercore.card.query.port.inbound;

import com.example.ledgercore.card.query.dto.CardInfo;
import com.example.ledgercore.card.query.dto.GetCardByIdQuery;

public interface GetCardByIdUseCase {

    CardInfo execute(GetCardByIdQuery query);
}