package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.credit.query.dto.GetCreditStatementResponse;
import com.example.ledgercore.credit.query.dto.GetCreditStatementsQuery;

public interface GetCreditStatementsUseCase {

    PageResponse<GetCreditStatementResponse> execute(
            GetCreditStatementsQuery query
    );
}