package com.example.ledgercore.atm.query.port.inbound;

import com.example.ledgercore.atm.query.dto.AtmTerminalQuery;
import com.example.ledgercore.atm.query.dto.AtmTerminalResponse;
import com.example.ledgercore.common.dto.PageResponse;

public interface GetAtmTerminalsUseCase {

    PageResponse<AtmTerminalResponse> execute(
            AtmTerminalQuery query
    );
}