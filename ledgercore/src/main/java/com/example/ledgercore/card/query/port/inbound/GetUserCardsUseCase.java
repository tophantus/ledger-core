package com.example.ledgercore.card.query.port.inbound;

import com.example.ledgercore.card.query.dto.CardInfo;
import com.example.ledgercore.card.query.dto.GetUserCardsQuery;
import com.example.ledgercore.common.dto.PageResponse;

public interface GetUserCardsUseCase {

    PageResponse<CardInfo> execute(
            GetUserCardsQuery query
    );
}