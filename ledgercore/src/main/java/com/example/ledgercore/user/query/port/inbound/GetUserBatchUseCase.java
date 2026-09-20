package com.example.ledgercore.user.query.port.inbound;

import com.example.ledgercore.user.query.dto.GetUserBatchQuery;
import com.example.ledgercore.user.query.dto.GetUserBatchResult;

public interface GetUserBatchUseCase {

    GetUserBatchResult execute(
            GetUserBatchQuery query
    );
}